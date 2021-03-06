/* 
 * File:   servo2.h
 * Author: Pras Velagapudi
 *
 * Created on December 21, 2012, 10:17 AM
 * This code is adapted from the sample code at:
 * http://bradsprojects.wordpress.com/2010/05/03/servo-control-with-an-xmega/
 */

#ifndef SERVO2_H
#define	SERVO2_H

#include "portability.h"
#include <inttypes.h>
#include <avr/io.h>

#if (F_CPU != 32000000UL)
#error Servo timer settings do not match CPU frequency!
#endif

#define MIN_PULSE_WIDTH_US      1000L    // the shortest pulse sent to a servo 
#define MAX_PULSE_WIDTH_US      2000L    // the longest pulse sent to a servo 
#define DEFAULT_PULSE_WIDTH_US  ((MAX_PULSE_WIDTH_US + MIN_PULSE_WIDTH_US) / 2)
#define REFRESH_INTERVAL_US     20000L   // minimum time to refresh servos
#define SERVO_SCALE_FACTOR      (65535L / (MAX_PULSE_WIDTH_US - MIN_PULSE_WIDTH_US))

struct Servo2Config0
{
  TC0_t *timer;
  PORT_t *port;
  int pin1;
  int pin2;
};

class Servo2
{
protected:
  Servo2();
  virtual ~Servo2() = 0;
  
  volatile int _position1, _position2;
  virtual void update(uint16_t pos1, uint16_t pos2) = 0;
  
public:
  void set1(int16_t position);
  void set2(int16_t position);
  int16_t get1(void);
  int16_t get2(void);
  void setRaw1(uint16_t position);
  void setRaw2(uint16_t position);
  uint16_t getRaw1(void);
  uint16_t getRaw2(void);
};

template<const Servo2Config0 &_config>
class Servo2HW0 : public Servo2
{
public:
  Servo2HW0() : Servo2() {
    // Initialize the timer to the default position
    update(_position1, _position2);
    
    // Set up the timer pin as an output
    _config.port->OUTCLR = _BV(_config.pin1);
    _config.port->DIRSET = _BV(_config.pin1);

    _config.port->OUTCLR = _BV(_config.pin2);
    _config.port->DIRSET = _BV(_config.pin2);
    
    // Set up the timer to a 0.5MHz tick resolution, so we
    // can convert timings easily (1 tick = 2uS)
    _config.timer->PER = REFRESH_INTERVAL_US >> 1; // Set the PWM resolution
    _config.timer->CTRLB = TC0_CCAEN_bm | TC0_CCBEN_bm | TC_WGMODE_SS_gc; // Use compare channel B and C
    
    // Start the timer running
    _config.timer->CTRLA = TC_CLKSEL_DIV64_gc; // 32MHz / 64 = 0.5Mhz
  }
  
  ~Servo2HW0() {
    // Disable the timer
    _config.timer->CTRLA = 0;

    // Set up the timer pin as an input
    _config.port->OUTCLR = _BV(_config.pin1);
    _config.port->DIRCLR = _BV(_config.pin1);
    _config.port->OUTCLR = _BV(_config.pin2);
    _config.port->DIRCLR = _BV(_config.pin2);
  }
  
 private:
  void update(uint16_t pos1, uint16_t pos2)
  {
    // Configure the timer period to match the servo setting
    _config.timer->CCABUF = pos1 >> 1; // 2 uS = 1 ticks @ 32MHz/64
    _config.timer->CCBBUF = pos2 >> 1; // 2 uS = 1 ticks @ 32MHz/64
  }
};

#endif	/* SERVO2_H */

