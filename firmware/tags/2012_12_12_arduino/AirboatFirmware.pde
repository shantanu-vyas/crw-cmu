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
#include <EEPROM.h>
#include "EEPROMHelper.h"
#include "TimedAction.h"
#include "MeetAndroid.h"

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
#define UPDATE_INTERVAL 10

// Arrays to store the actual and desired velocity of the vehicle in 6D
float desiredVelocity[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
float actualVelocity[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

// Structure storing PID constants for each axis
struct pidConstants_t { float Kp[6], Ki[6], Kd[6]; } pid;

// Communication structure for Amarino
MeetAndroid amarino;

// Watchdog timer - must be reset() periodically
TimedAction watchdogTimer = TimedAction(500, watchdog);

// Control loop timer
TimedAction controlTimer = TimedAction(UPDATE_INTERVAL, update);

/**
 * The main setup function for the vehicle.  Initalized the Amarino communications,
 * then calls the various setup functions for the various modules.
 */
void setup() 
{ 
  // Load PID constants in from EEPROM
  EEPROM_readAnything(PID_ADDRESS, pid);

  // Set up serial communications
  Serial.begin(115200);
  amarino.registerFunction(setVelocity, SET_VELOCITY_FN);
  amarino.registerFunction(setPID, SET_PID_FN);
  amarino.registerFunction(getPID, GET_PID_FN);
  amarino.registerFunction(setSampler, SET_SAMPLER_FN);

  // Initialize device modules
  initGyro();
  initRudder();
  initThruster();
  initSampler();
  initTE();
  initDepth();
  //initWaterCanary();
  initDO();
} 

/**
 * The main event update loop for the vehicle.  Within this function we check for
 * events from Amarino and the process timers.
 */
void loop() 
{     
  // Get any incoming messages and process them
  amarino.receive();

  // Perform psuedothreaded updates in various modules
  processTE();
  
  // Check if either the watchdog or the main loop is scheduled
  watchdogTimer.check();
  controlTimer.check();
}

/**
 * The main control loop for the vehicle.  Within this function we mainly call the 
 * periodic update functions for the various modules.
 */
void update()
{
  updateGyro();
  updateRudder();
  updateThruster();
  updateSampler();
  updateTE();
  updateDepth();
  //updateWaterCanary();
  updateDO();
}

/**
 * This callback is only reached when there has been no communication from the serial
 * port for the specified timeout interval.  This function should attempt to gradually
 * transition the boat to a safe state.
 */
void watchdog()
{
  // Slow the vehicle down by reducing velocity in every direction
  for (int i = 0; i < 6; ++i)
    desiredVelocity[i] *= 0.75;
}

/**
 * Receives a 6D desired velocity command from Amarino.
 */
void setVelocity(byte flag, byte numOfValues)
{
  // Ignore if wrong number of arguments
  if (numOfValues != 6) return;

  // Load these values into array of desired velocities  
  amarino.getFloatValues(desiredVelocity);
  
  // Reset the watchdog timer
  watchdogTimer.reset();
}

/**
 * Receives PID constants for a particular axis.
 */
void setPID(byte flag, byte numOfValues)
{
  // Ignore if wrong number of arguments
  if (numOfValues != 4) return;
  
  // Load all the arguments into memory
  float args[numOfValues];
  amarino.getFloatValues(args);
  
  // Get the axis that is being set
  int axis = (int)args[0];
  if (axis < 0 || axis >= 6) return;
  
  // Set these values and save them to the EEPROM
  pid.Kp[axis] = args[1];
  pid.Ki[axis] = args[2];
  pid.Kd[axis] = args[3];
  EEPROM_writeAnything(PID_ADDRESS, pid);
  
  // Reset the watchdog timer
  watchdogTimer.reset();
}

/**
 * Sends the PID constants of a particular axis to Amarino.
 */
void getPID(byte flag, byte numOfValues)
{
  // Ignore if wrong number of arguments
  if (numOfValues != 1) return;
  
  // Load the argument into memory
  float axisRaw = amarino.getFloat();
  
  // Get the axis that is being set
  int axis = (int)axisRaw;
  if (axis < 0 || axis >=6) return;
  
  // Return the appropriate values to Amarino
  amarino.send(GET_PID_FN);
  amarino.send((float)axis);
  amarino.send(pid.Kp[axis]);
  amarino.send(pid.Ki[axis]);
  amarino.send(pid.Kd[axis]);
  amarino.sendln();
}
