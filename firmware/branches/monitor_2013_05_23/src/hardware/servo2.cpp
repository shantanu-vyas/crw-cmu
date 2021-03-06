#include "servo2.h"

Servo2::Servo2()
{
  _position1 = DEFAULT_PULSE_WIDTH_US;
  _position2 = DEFAULT_PULSE_WIDTH_US;
}

Servo2::~Servo2() { }

void Servo2::set1(int16_t position)
{
  _position1 = (position / SERVO_SCALE_FACTOR) + DEFAULT_PULSE_WIDTH_US;
  update(_position1, _position2);
}

void Servo2::set2(int16_t position)
{
  _position2 = (position / SERVO_SCALE_FACTOR) + DEFAULT_PULSE_WIDTH_US;
  update(_position1, _position2);
}

int16_t Servo2::get1()
{
  return (_position1 - DEFAULT_PULSE_WIDTH_US) * SERVO_SCALE_FACTOR;
}

int16_t Servo2::get2()
{
  return (_position2 - DEFAULT_PULSE_WIDTH_US) * SERVO_SCALE_FACTOR;
}

void Servo2::setRaw1(uint16_t position)
{
  // Do valid bounds checking/clipping                                               
  if (position > MAX_PULSE_WIDTH_US)
    position = MAX_PULSE_WIDTH_US;
  if (position < MIN_PULSE_WIDTH_US)
    position = MIN_PULSE_WIDTH_US;

  // Set to unscaled position in pulse width                                         
  _position1 = position;
  update(_position1, _position2);
}

void Servo2::setRaw2(uint16_t position)
{
  // Do valid bounds checking/clipping                                               
  if (position > MAX_PULSE_WIDTH_US)
    position = MAX_PULSE_WIDTH_US;
  if (position < MIN_PULSE_WIDTH_US)
    position = MIN_PULSE_WIDTH_US;

  // Set to unscaled position in pulse width                                         
  _position2 = position;
  update(_position1, _position2);
}

uint16_t Servo2::getRaw1()
{
  return _position1;
}

uint16_t Servo2::getRaw2()
{
  return _position2;
}
