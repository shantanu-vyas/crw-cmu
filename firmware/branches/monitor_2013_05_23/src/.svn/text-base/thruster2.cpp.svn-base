#include "thruster2.h"

extern float desiredVelocity[];
extern float actualVelocity[];

Thruster2::Thruster2(MeetAndroid * const a, Servo2 * const s)
  : servo(s), amarino(a)
{
}

Thruster2::~Thruster2() { }

void Thruster2::arm(void)
{
  servo->set1(32000);
  servo->set2(32000);
  _delay_ms(3000);

  servo->set1(0);
  servo->set2(0);
  _delay_ms(5000);

  servo->set1(0);
  servo->set2(0);
  _delay_ms(2000);
}

void Thruster2::update(void)
{
  float output1, output2;

  output1 = (desiredVelocity[0] + desiredVelocity[5]) * 20000;
  output2 = (desiredVelocity[0] - desiredVelocity[5]) * 20000;

  if (output1 < TMIN)
    output1 = TMIN;
  if (output1 > TMAX)
    output1 = TMAX;

  if (output2 < TMIN)
    output2 = TMIN;
  if (output2 > TMAX)
    output2 = TMAX;

  servo->set1(output1);
  servo->set2(output2);

  sendCounter++;
  if (sendCounter > THRUSTER_UPDATE_COUNT) {
    amarino->send(RECV_THRUSTER_DEG);
    amarino->send((output1 + output2)/2);
    amarino->sendln();

    amarino->send(RECV_RUDDER_POS);
    amarino->send((output1 - output2));
    amarino->sendln();

    sendCounter = 0;
  }
}

