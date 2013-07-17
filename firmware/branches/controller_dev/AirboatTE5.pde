/**
 * 5TE Sensor - EC, Water, Temperature
 *
 * Contains control and update code interfacing with an 5TE sensor
 *
 */

#include "pt.h"
#include <stdio.h>

// Define the char code for the Amarino callback
#define RECV_TE_FN 's'

// Define the pins used to interface with the sensor
#define SENSOR_RX_PIN 19
#define SENSOR_PWR_PIN 45

// Set the sensor reading interval (in 10s of ms)
#define SENSOR_INTERVAL 100

// Set the interval after which the sensor is forced off (if a timeout occurs)
#define RESET_INTERVAL 50

// Stack variables for use in protothreads
static int d, c, t;
static char type;
static int crc = 0;
static int output = 0;
static byte val;

// Protothread state structures
static struct pt teUpdatePt, teReadPt;

// Sensor reading status variables
int teCount = 0;
boolean teIsReading = false;

void initTE()  {
  
  // Turn off sensor
  pinMode(SENSOR_RX_PIN, INPUT);
  pinMode(SENSOR_PWR_PIN, OUTPUT);
  digitalWrite(SENSOR_PWR_PIN, LOW);
  
  // Enable serial port
  Serial1.begin(1200);
  
  // Start counter to do reading every SENSOR_INTERVAL counts
  teCount = 0;
}

// Converts from raw 5TE sensor value to a floating point Celsius temperature.
float toTemp(const int &rawTemp) {
  return (float)(rawTemp - 400)/10.0;
}

// Converts from raw 5TE sensor value to a floating point mS/cm (dS/m) conductivity.
float toConductivity(const int &rawCond) {
  return ((float)rawCond) / 100.0;
}

// Converts from raw 5TE sensor value to dielectric, as specified in datasheet.
float toDielectric(const int &rawDielectric) {
   return ((float) rawDielectric) / 50.0;
}

// Reads a single ASCII plaintext integer from the serial stream.
// (Note: also reads the first terminating character after integer)
static 
PT_THREAD(readInt(struct pt *pt)) {
  PT_BEGIN(pt);

  // Clear last integer that was read
  output = 0;
  
  // Read until a non-digit is reached
  while(1) {
    
    // Wait for incoming data
    PT_WAIT_UNTIL(pt, Serial1.available() > 0);
    val = Serial1.read();

    // Increment checksum, exit on termination character
    crc += val;    
    if (val < '0' || val > '9') break;

    // Shift data and add new digit
    output *= 10;
    output += (val - '0');
  }
  
  PT_END(pt);
}

// Powers up and reads the sensor values from a 5TE environmental sensor, 
// and checks the resulting checksum for validity.  If the data is invalid,
// all values will return zero.
static 
PT_THREAD(teUpdateThread(struct pt *pt))
{
  PT_BEGIN(pt);

  // Zero out checksum
  crc = 0;
    
  // Turn on sensor
  digitalWrite(SENSOR_PWR_PIN, HIGH);
  
  // Wait for power-up sequence (15ms high)  
  delay(1);
  PT_WAIT_UNTIL(pt, !digitalRead(SENSOR_RX_PIN)); // Wait for HIGH.                     
  PT_WAIT_UNTIL(pt, digitalRead(SENSOR_RX_PIN));  // Wait for LOW.

  // Clear any spurious data (from while the sensor was off)  
  Serial1.flush();
  
  // Read data values
  PT_SPAWN(pt, &teReadPt, readInt(&teReadPt));
  d = output;
  PT_SPAWN(pt, &teReadPt, readInt(&teReadPt));
  c = output;
  PT_SPAWN(pt, &teReadPt, readInt(&teReadPt));
  t = output;
    
  // Wait for data, then read sensor type ('z' or 'x')
  PT_WAIT_UNTIL(pt, Serial1.available() > 0);
  type = Serial1.read();
  crc += type;
    
  // Verify sensor checksum
  PT_WAIT_UNTIL(pt, Serial1.available() > 0);
  byte checksum = Serial1.read();
  if (checksum != (crc % 64 + 32)) {
    d = 0;
    c = 0;
    t = 0;
    type = '/0';
  }
  
  // Turn off sensor
  digitalWrite(SENSOR_PWR_PIN, LOW);
  
  // Convert and output the returned values
  amarino.send(RECV_TE_FN);
  amarino.send(toDielectric(d));
  amarino.send(toConductivity(c));
  amarino.send(toTemp(t));
  amarino.sendln();
  
  PT_END(pt)
}

// Handles the actual processing associated with sensor
void processTE() {
  
  // If the sensor is being read, reschedule the thread and update status
  if (teIsReading) {
    teIsReading = PT_SCHEDULE(teUpdateThread(&teUpdatePt));
  }
}

// Wrapper function that will start a sensor reading
void updateTE() {

  ++teCount;
  if (teCount >= RESET_INTERVAL) {

    // Turn off sensor
    digitalWrite(SENSOR_PWR_PIN, LOW);
    
  } if (teCount >= SENSOR_INTERVAL) {
    
    // Start the sensor reading thread and reset counter
    PT_INIT(&teUpdatePt);
    teIsReading = true;
    teCount = 0;
    
  }
}

