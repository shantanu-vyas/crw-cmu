/**
 * Airboat Control Firmware - Gyro
 *
 * Contains control and update code interfacing with an ITG-3200
 * digital gyro.  Updates the yaw velocity in the main code module.
 */

#include <Wire.h>

// Define the char codes for the Amarino callback
#define RECV_GYRO_FN 'g'

// Definitions to help communicate with the gyro
#define GYRO_ADDR 0x69 // binary = 11101001
#define SMPLRT_DIV 0x15
#define DLPF_FS 0x16
#define INT_CFG 0x17
#define PWR_MGM 0x3E
#define TO_READ 8 // 2 bytes for each gyro axis x, y, z & temp

//gyro average
int gyro_cnt = 0;
double x_avg = 0;
double y_avg = 0;
double z_avg = 0;

void writeTo(int device, byte address, byte val) {
  Wire.beginTransmission(device);
  Wire.send(address);
  Wire.send(val);
  Wire.endTransmission();
}


void readFrom(int device, byte address, int num, byte buff[]) {
  Wire.beginTransmission(device); 
  Wire.send(address);
  Wire.endTransmission();

  Wire.beginTransmission(device);
  Wire.requestFrom(device, num);   

  int i = 0;
  while(Wire.available())
  { 
    buff[i] = Wire.receive();
    i++;
  }
  Wire.endTransmission();
}

void estimateGyroBias(double gyroBias[])
{
  int regAddress = 0x1B;
  long int xSamples = 0, ySamples = 0, zSamples = 0;
  double bias[3];
  byte buff[TO_READ];
  
  delay(30000);             // Lets wait for sometime to settle down before calibrating
  
   for(int i = 0; i < 1000; i++)
   {
       readFrom(GYRO_ADDR, regAddress, TO_READ, buff);
       xSamples += ((buff[2] << 8) | buff[3]);
       ySamples += ((buff[4] << 8) | buff[5]);
       zSamples += ((buff[6] << 8) | buff[7]);
   }
   
   gyroBias[0] = xSamples / 1000;
   gyroBias[1] = ySamples / 1000;
   gyroBias[2] = zSamples / 1000;
}

void initGyro()
{
  Wire.begin();
  
  /*****************************************
  * ITG 3200
  * power management set to:
  * clock select = internal oscillator
  * no reset, no sleep mode
  * no standby mode
  * sample rate to = 125Hz
  * parameter to +/- 2000 degrees/sec
  * low pass filter = 5Hz
  * no interrupt
  ******************************************/
  
  writeTo(GYRO_ADDR, PWR_MGM, 0x00);
  writeTo(GYRO_ADDR, SMPLRT_DIV, 0x07); // EB, 50, 80, 7F, DE, 23, 20, FF
  writeTo(GYRO_ADDR, DLPF_FS, 0x1E); // +/- 2000 dgrs/sec, 1KHz, 1E, 19
  writeTo(GYRO_ADDR, INT_CFG, 0x00);
  
  estimateGyroBias(gyroBias);
}


void updateGyro()
{
  /**************************************
  Gyro ITG-3200 I2C registers:
  temp MSB = 1B, temp LSB = 1C
  x axis MSB = 1D, x axis LSB = 1E
  y axis MSB = 1F, y axis LSB = 20
  z axis MSB = 21, z axis LSB = 22
  *************************************/

  int regAddress = 0x1B;
  float temp, x, y, z;
  float x_decoded, y_decoded, z_decoded, temp_decoded;
  byte buff[TO_READ];
  
  readFrom(GYRO_ADDR, regAddress, TO_READ, buff);
  
  temp = (buff[0] << 8) | buff[1];
  x = ((buff[2] << 8) | buff[3]) - gyroBias[0];
  y = ((buff[4] << 8) | buff[5]) - gyroBias[1];
  z = ((buff[6] << 8) | buff[7]) - gyroBias[2];
  
  // Convert raw data to SI units
  temp_decoded = 35.0 + (((float)temp + 13200.0) / 280.0);
  x_decoded = -((double(x) / 14.375) * 0.017453);
  y_decoded = -((double(y) / 14.375) * 0.017453);
  z_decoded = -((double(z) / 14.375) * 0.017453);

  // Set the roll, pitch & yaw from the gyro 
  actualVelocity[3] = x_decoded;
  actualVelocity[4] = y_decoded;
  actualVelocity[5] = z_decoded;
  
  // Return the appropriate values to Amarino
  if (gyro_cnt < 10)
  {
    x_avg += x_decoded;
    y_avg += y_decoded;
    z_avg += z_decoded;
    gyro_cnt++;
  } 
  else 
  {
    amarino.send(RECV_GYRO_FN);
    amarino.send(x_avg/10.0);
    amarino.send(y_avg/10.0);
    amarino.send(z_avg/10.0);
    amarino.sendln();

    gyro_cnt = 0;      
    x_avg = 0;
    y_avg = 0;
    z_avg = 0;
  }
}
