/**
 * Airboat Control Firmware - Rudder
 *
 * Contains control and update code interfacing with a servo-driven
 * mechanical rudder.  This code runs a PID loop attempting to match
 * a desired yaw velocity using feedback from a gyro.
 */

#include <Servo.h> 

#define RBUFSIZE  100
#define RMIN  -1000
#define RMAX  1000
#define SERVO_MIN 150
#define SERVO_MAX 30

#define RECV_RUDDER_POS 'r'

Servo rudder;

float rBuffer[RBUFSIZE];
float rprevError = 0;
float rBufferSum = 0;
int rIndx = 0;

int send_pos_cnt = 3;

void initRudder()
{
  rudder.attach(41);
  
  for (int i = 0; i < 100; i++)
       rBuffer[i] = 0;
}

void updateRudder()
{
  /*
  float rError = desiredVelocity[5] - actualVelocity[5];
  
  rIndx++;
  if (rIndx == RBUFSIZE)
      rIndx = 0;
  
  rBufferSum -= rBuffer[rIndx];
  rBufferSum += rError;
  rBuffer[rIndx] = rError;
  
  float rPID = (pid.Kp[5] * rError) + (pid.Kd[5] * ((rError - rprevError)/(UPDATE_INTERVAL))) + (pid.Ki[5] * rBufferSum);
  rprevError = rError;
  
  if (rPID < RMIN)
      rPID = RMIN;
  else
  if (rPID > RMAX)
      rPID = RMAX;
      
  pos = map((int)rPID,RMIN, RMAX, 150, 30);
  
  rudder.write(pos);
  
  send_pos_cnt++;
  
  if (send_pos_cnt > 10)
  {
     amarino.send(RECV_RUDDER_POS);
     amarino.send(pos);
     amarino.sendln();
     
     send_pos_cnt = 0;
  }
  */
  
  // changed so that input at desiredVelocity[5] from android is sent as a command directly to the rudder servo
  double input = desiredVelocity[5];

  
  // check to ensure input values are within the bounds
  if (input < SERVO_MAX)
      input = SERVO_MAX;
  else
  if (input > SERVO_MIN)
      input = SERVO_MIN;
  
  rudder.write((int)input);
  
  send_pos_cnt++;
  
  if (send_pos_cnt > 10)
  {
     amarino.send(RECV_RUDDER_POS);
     amarino.send(input);
     amarino.sendln();
     
     send_pos_cnt = 0;
  }
}
