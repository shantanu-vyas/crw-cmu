/**
 * Airboat Control Firmware - Thruster
 *
 * Contains control and update code interfacing with the main thrust
 * motor of the vehicle.  This code runs a PWL open-loop attempting to 
 * reach a desired forward velocity.
 */
#ifndef THRUSTER2_H
#define THRUSTER2_H

#include "servo2.h"
#include "meet_android.h"
#include <util/delay.h>

#define TBUFSIZE  100
#define TMIN  0
#define TMAX  24000

#define RECV_THRUSTER_DEG 't'
#define RECV_RUDDER_POS 'r'

#define THRUSTER_UPDATE_INTERVAL_MS (100)
#define THRUSTER_UPDATE_COUNT (10)

class Thruster2
{
 public:
  Thruster2(MeetAndroid * const a, Servo2 * const s);  
  ~Thruster2();

  void arm(void);  
  void update(void);
  
 private:
  Servo2 * const servo;
  MeetAndroid * const amarino;

  float prevError;
  float bufferSum;
  float buffer[TBUFSIZE];
  int bufferIdx;
  
  int sendCounter;
};

#endif /* THRUSTER2_H */
