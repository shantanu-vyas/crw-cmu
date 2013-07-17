/**
 * Airboat Control Firmware - Thruster
 *
 * Contains control and update code interfacing with the main thrust
 * motor of the vehicle.  This code runs a PWL open-loop attempting to 
 * reach a desired forward velocity.
 */
 
#include <Servo.h> 

#define TBUFSIZE  100
#define TMIN  1000
#define TMAX  2200

#define RECV_THRUSTER_DEG 't'

Servo thruster;

float tBuffer[TBUFSIZE];
float tprevError = 0;
float tBufferSum = 0;
int tIndx = 0;

int send_thruster_cnt = 6;

void armThruster()
{  
  thruster.write(1000);  
  delay(1200);

  thruster.write(2000); 
  delay(1200);

  thruster.write(1000);  
  delay(1200);
}

void initThruster()
{
  thruster.attach(40);
  armThruster();
  
  for (int i = 0; i < 100; i++)
       tBuffer[i] = 0;
}

void updateThruster()
{
   /*
   float tError = desiredVelocity[0] - actualVelocity[0];
   
   tIndx = (tIndx >= 100)?0 : tIndx++;
   tBufferSum -= tBuffer[tIndx];
   tBufferSum += tError;
   tBuffer[tIndx] = tError;
  
   float tPID = (pid.Kp[0] * tError) + (pid.Kd[0] * ((tError - tprevError)/(UPDATE_INTERVAL))) + (pid.Ki[0] * tBufferSum);
   tprevError = tError;
   
   if (tPID < TMIN) 
       tPID = TMIN;
   if (tPID > TMAX)
       tPID = TMAX;

   int deg = map((int)tPID, 0, 1000, 1000, 2200); //32767
   thruster.write(deg);
   
   send_thruster_cnt++;
  
   if (send_thruster_cnt > 10)
   {
      amarino.send(RECV_THRUSTER_DEG);
      amarino.send(deg);
      amarino.sendln();
     
      send_thruster_cnt = 0;
   }
   */
   // take command directly from Android and push to servo
   int deg = desiredVelocity[0];
   
   if (deg < TMIN) 
       deg = TMIN;
   if (deg > TMAX)
       deg = TMAX;

   thruster.write(deg);
   
      send_thruster_cnt++;
  
   if (send_thruster_cnt > 10)
   {
      amarino.send(RECV_THRUSTER_DEG);
      amarino.send(deg);
      amarino.sendln();
     
      send_thruster_cnt = 0;
   }
   
 
}
