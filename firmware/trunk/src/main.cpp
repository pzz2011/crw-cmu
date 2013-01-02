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

// Structure storing PID constants for each axis
// TODO: This placement is currently a hack to share with rudder and thruster
struct pidConstants_t { float Kp[6], Ki[6], Kd[6]; } pid;

// Core functionality
#include "board.h"
#include "meet_android.h"
#include "eeprom.h"
#include <util/delay.h>

// Core modules
#include "rudder.h"
#include "thruster.h"

// Sensor modules
#include "depth_sensor.h"
#include "do_sensor.h"
#include "te5_sensor.h"

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

// Hardware configuration
LedHW<UserLed> led;
SerialHW<SerialBluetooth> bluetooth(BAUD_115200);

// Communication structure for Amarino
MeetAndroid amarino(&bluetooth);

// Module configuration
ServoHW0<Motor> motor;
Thruster thruster(&amarino, &motor);

ServoHW1<Servo1> servo1;
Rudder rudder(&amarino, &servo1);

#warning Be careful powering the DepthSensor!
DepthConfig depthConfig = { &PORTK, PIN4 };
DepthSensor<depthConfig, Serial2> depthSensor(&amarino);

DOSensor<Serial3> doSensor(&amarino);

#warning TE5 pinout needs to be verified!
TE5Config teConfig = { &PORTD, PIN1, &PORTD, PIN0};
TE5Sensor<teConfig, Serial4> teSensor(&amarino);

// Watchdog timer - must be reset() periodically
//TimedAction watchdogTimer = TimedAction(500, watchdog);

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
void setVelocity(uint8_t flag, uint8_t numOfValues)
{
  // Ignore if wrong number of arguments
  if (numOfValues != 6) return;

  // Load these values into array of desired velocities  
  amarino.getFloatValues(desiredVelocity);
  
  // Reset the watchdog timer
  //watchdogTimer.reset();
}

/**
 * Receives PID constants for a particular axis.
 */
void setPID(uint8_t flag, uint8_t numOfValues)
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
  eeprom_write(PID_ADDRESS, pid);
  
  // Reset the watchdog timer
  //watchdogTimer.reset();
}

/**
 * Sends the PID constants of a particular axis to Amarino.
 */
void getPID(uint8_t flag, uint8_t numOfValues)
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

/**
 * The main setup function for the vehicle.  Initalized the Amarino communications,
 * then calls the various setup functions for the various modules.
 */
void setup() 
{ 
  // Core board initialization
  initBoard();

  // Load PID constants in from EEPROM
  eeprom_read(PID_ADDRESS, pid);

  // Set up serial communications
  amarino.registerFunction(setVelocity, SET_VELOCITY_FN);
  amarino.registerFunction(setPID, SET_PID_FN);
  amarino.registerFunction(getPID, GET_PID_FN);
  //  amarino.registerFunction(setSampler, SET_SAMPLER_FN);

  // Initialize device modules
  //initRudder(rudder, amarino);
  //initThruster();
  //initSampler();
  //initTE();
  //initDepth();
  //initWaterCanary();
  //initDO();
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
  depthSensor.loop();
  doSensor.loop();

  // Perform psuedothreaded updates in various modules
  //processTE();
  
  // Check if either the watchdog or the main loop is scheduled
  //watchdogTimer.check();
  //controlTimer.check();
}

/**
 * The main control loop for the vehicle.  Within this function we mainly call the 
 * periodic update functions for the various modules.
 */
void update(void *)
{
  // Toggle LED just to let us know things are working
  led.toggle();
  printf("Test\r\n");

  rudder.update();
  thruster.update();
  //updateSampler();
  //updateTE();
  //updateDepth();
  //updateWaterCanary();
  //updateDO();
}

int main(void)
{
  // Initial setup for boat
  setup();
  
  // Schedule periodic updates
  Task<UserTask> task(update, NULL, 500);
  
  // Start main tight loop
  while(true) { loop(); }
}
