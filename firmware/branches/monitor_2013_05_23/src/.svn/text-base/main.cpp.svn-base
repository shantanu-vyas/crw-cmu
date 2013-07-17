/**
 * Airboat Control Firmware
 *
 * Provides low-level functionality for interacting with vehicle hardware such as
 * fans, servos, gyros, and simple sensors.  Communication is achieved via the 
 * Amarino library.
 *
 * Functionality is broken down into separate modules for each device/actuator that 
 * is being controlled.  Separate Amarino functions are used to isolate IO between
 * modules.  Each module is assumed to be called serially, so thread safety is not 
 * an issue except in callback functions.
 */

#include <stdlib.h>

// Core functionality
#include "board.h"
#include "meet_android.h"
#include <util/delay.h>

// Core modules
#include "thruster2.h"

// Sensor modules
#include "depth_sensor.h"
#include "do_sensor.h"
#include "te5_sensor.h"
#include "es2_sensor.h"
#include "monitor_sensor.h"

// Define indices for specific coordinates
// Assumes X is forward, Y is left, Z is up, frame is right-handed
#define DX 0
#define DY 1
#define DZ 2
#define DRX 3
#define DRY 4
#define DRZ 5

// Define the location of the PID constants in EEPROM memory
#define PID_ADDRESS 0

// Define the char codes for the main Amarino callbacks
#define SET_VELOCITY_FN 'v'
#define SET_PID_FN 'k'
#define GET_PID_FN 'l'
#define SET_SAMPLER_FN 'q'

// Defines update interval in milliseconds
#define UPDATE_INTERVAL 100

// Arrays to store the actual and desired velocity of the vehicle in 6D
float desiredVelocity[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
float actualVelocity[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

// Hardware configuration
LedHW<UserLed> led;

// Communication structure for Amarino
SerialHW<SerialBluetooth> bluetooth(BAUD_115200);
MeetAndroid amarino(&bluetooth);

// Module configuration
Servo2HW0<Motor> motor;
Thruster2 thruster(&amarino, &motor);

MonitorConfig monitorConfig = { &PORTK, PIN6 };
MonitorSensor<monitorConfig, Serial1> monitorSensor(&amarino);

//DepthConfig depthConfig = { &PORTK, PIN4 };
//DepthSensor<depthConfig, Serial2> depthSensor(&amarino);

//DOSensor<Serial3> doSensor(&amarino);

TE5Config teConfig = { &PORTD, PIN1 };
TE5Sensor<teConfig, Serial4> teSensor(&amarino);

//ES2Config esConfig = { &PORTD, PIN1 };
//ES2Sensor<esConfig, Serial4> esSensor(&amarino);

/**
 * Gradually transition the boat to a safe state.
 */
void decayVelocity()
{
  // Slow the vehicle down by reducing velocity in every direction
  for (int i = 0; i < 6; ++i)
    desiredVelocity[i] *= 0.95;
}

/**
 * Receives a 6D desired velocity command from Amarino.
 */
void setVelocity(uint8_t flag, uint8_t numOfValues)
{
  // Ignore if wrong number of arguments
  if (numOfValues != 6) return;

  // Load these values into array of desired velocities  
  amarino.getFloatValues(desiredVelocity);
}

/**
 * The main setup function for the vehicle.  Initalized the Amarino communications,
 * then calls the various setup functions for the various modules.
 */
void setup()
{
  // Core board initialization
  initBoard();

  // Arm thruster
  thruster.arm();

  // Set up serial communications
  amarino.registerFunction(setVelocity, SET_VELOCITY_FN);
}

/**
 * The main event update loop for the vehicle.  Within this function we check for
 * events from Amarino and the process timers.
 */
void loop()
{
  // Get any incoming messages and process them
  amarino.receive();

  // Process the sensors
  monitorSensor.loop();
  // depthSensor.loop();
  // doSensor.loop();
     teSensor.loop();
  // esSensor.loop();
}

/**
 * The main control loop for the vehicle.  Within this function we mainly call the 
 * periodic update functions for the various modules.
 */
void update(void *)
{
  // Toggle LED just to let us know things are working
  led.toggle();

  // Update the thrust and rudder control loops
  thruster.update();

  // Perform periodic updates for sensors
     teSensor.update();
  // doSensor.update();
  //  depthSensor.update();
  // esSensor.update();
  monitorSensor.update();

  // Decay the desired velocities slightly
  decayVelocity();
}

int main(void)
{
  // Initial setup for boat
  setup();

  // Schedule periodic updates
  Task<UserTask> task(update, NULL, UPDATE_INTERVAL);

  // Start main tight loop
  while(true) { loop(); }
}
