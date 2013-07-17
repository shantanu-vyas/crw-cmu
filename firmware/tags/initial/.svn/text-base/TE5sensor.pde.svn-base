/**
 * 5TE Sensor - EC, Water, Temperature
 *
 * Contains control and update code interfacing with an 5TE sensor
 *
 */

#include <SoftwareSerial.h>

// Define the char codes for the Amarino callback
#define RECV_TE_FN 's'

#define SENSOR_RX_PIN 8
#define SENSOR_PWR_PIN 12

SoftwareSerial sensorSerial =  SoftwareSerial(SENSOR_RX_PIN, 0);
int crc = 0;

void initTE()  {
  pinMode(SENSOR_RX_PIN, INPUT);
  pinMode(SENSOR_PWR_PIN, OUTPUT);
  
  sensorSerial.begin(1200);
}

// Reads a single ASCII plaintext integer from the serial stream.
// (Note: also reads the first terminating character after integer)
void readInt(int &output) {
  byte val;
  
  output = 0;
  while( (val = sensorSerial.read()) >= '0' && val <= '9') {
    output *= 10;
    output += (val - '0');
    crc += val; // Increment checksum
  }
  
  crc += val; // Increment checksum for termination character
}

// Powers up and reads the sensor values from a 5TE environmental sensor, 
// and checks the resulting checksum for validity.  If the data is invalid,
// all values will return zero.
void read(int &dielectric, int &conductivity, int &temp, char &type) { 
  
  // Zero out checksum
  crc = 0;
  
  // Turn on sensor
  digitalWrite(SENSOR_PWR_PIN, HIGH);

  // Wait for power-up sequence (15ms high)  
  delay(20);
  while (!digitalRead(SENSOR_RX_PIN)); // Wait for HIGH.                      
  while (digitalRead(SENSOR_RX_PIN));  // Wait for LOW.

  // Read data values
  readInt(dielectric);
  readInt(conductivity);
  readInt(temp);
  
  // Read sensor type ('z' or 'x')
  type = sensorSerial.read();
  crc += type;
  
  // Verify sensor checksum
  byte checksum = sensorSerial.read();
  if (checksum != (crc % 64 + 32)) {
    dielectric = 0;
    conductivity = 0;
    temp = 0;
    type = '/0';
  }

  // Turn off sensor
  digitalWrite(SENSOR_PWR_PIN, LOW);
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

void updateTE() {
   int d, c, t;
   char type;
   
   // Read from sensor
   read(d, c, t, type);
   
   // Convert and output the returned values
   amarino.send(RECV_TE_FN);
   amarino.send(toDielectric(d));
   amarino.send(toConductivity(c));
   amarino.send(toTemp(t));
   amarino.sendln();
   
   delay(1000);
}

