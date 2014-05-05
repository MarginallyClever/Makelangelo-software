//------------------------------------------------------------------------------
// Makelangelo - supports raprapdiscount RUMBA controller
// dan@marginallycelver.com 2013-12-26
// RUMBA should be treated like a MEGA 2560 Arduino.
//------------------------------------------------------------------------------
// Copyright at end of file.  Please see
// http://www.github.com/MarginallyClever/Makelangelo for more information.


//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
#include "MServo.h"


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
Axis a[NUM_AXIES];  // for line()
Axis atemp;  // for line()
Motor motors[NUM_AXIES];

Segment line_segments[MAX_SEGMENTS];
volatile int current_segment=0;
volatile int last_segment=0;
long old_feed_rate=0;
int flipFlop=HIGH;

Servo servos[NUM_SERVOS];


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------


/**
 * set up the pins for each motor
 */
void motor_setup() {
  motors[0].step_pin=17;
  motors[0].dir_pin=16;
  motors[0].enable_pin=48;
  motors[0].limit_switch_pin=37;
  motors[0].reel_in  = HIGH;
  motors[0].reel_out = LOW;

  motors[1].step_pin=54;
  motors[1].dir_pin=47;
  motors[1].enable_pin=55;
  motors[1].limit_switch_pin=36;
  motors[1].reel_in  = HIGH;
  motors[1].reel_out = LOW;
  
  int i;
  for(i=0;i<NUM_AXIES;++i) {  
    // set the motor pin & scale
    pinMode(motors[i].step_pin,OUTPUT);
    pinMode(motors[i].dir_pin,OUTPUT);
    pinMode(motors[i].enable_pin,OUTPUT);
    
    // set the switch pin
    motors[i].limit_switch_state=HIGH;
    pinMode(motors[i].limit_switch_pin,INPUT);
    digitalWrite(motors[i].limit_switch_pin,HIGH);
  }
  
  motor_set_step_count(0,0,0);

  // setup servos  
#if NUM_SERVOS>0
  servos[0].attach(SERVO0_PIN);
#endif
#if NUM_SERVOS>1
  servos[1].attach(SERVO1_PIN);
#endif
#if NUM_SERVOS>2
  servos[2].attach(SERVO2_PIN);
#endif
#if NUM_SERVOS>3
  servos[3].attach(SERVO3_PIN);
#endif
#if NUM_SERVOS>4
  servos[4].attach(SERVO4_PIN);
#endif

  pinMode(13,OUTPUT);
  digitalWrite(13,flipFlop);
  flipFlop=HIGH-flipFlop;
}


// turn on power to the motors (make them immobile)
void motor_enable() {
  int i;
  for(i=0;i<NUM_AXIES;++i) {  
    digitalWrite(motors[i].enable_pin,LOW);
  }
}


// turn off power to the motors (make them move freely)
void motor_disable() {
  int i;
  for(i=0;i<NUM_AXIES;++i) {  
    digitalWrite(motors[i].enable_pin,HIGH);
  }
}


// Change pen state.
void setPenAngle(int pen_angle) {
  if(posz!=pen_angle) {
    posz=pen_angle;
    
    if(posz<PEN_DOWN_ANGLE) posz=PEN_DOWN_ANGLE;
    if(posz>PEN_UP_ANGLE  ) posz=PEN_UP_ANGLE;

    servos[0].write(posz);
    delay(PEN_DELAY);
  }
}


void motor_set_step_count(long a0,long a1,long a2) {  
  if( current_segment==last_segment ) {
    Segment &old_seg = line_segments[get_prev_segment(last_segment)];
    old_seg.a[0].step_count=a0;
    old_seg.a[1].step_count=a1;
    old_seg.a[2].step_count=a2;
    laststep[0]=a0;
    laststep[1]=a1;
    laststep[2]=a2;
  }
}


/**
 * Supports movement with both styles of Motor Shield
 * @input newx the destination x position
 * @input newy the destination y position
 **/
void motor_onestep(int motor) {
#ifdef VERBOSE
  char *letter="XYZUVW";
  Serial.print(letter[motor]);
#endif
  
  digitalWrite(motors[motor].step_pin,HIGH);
  digitalWrite(motors[motor].step_pin,LOW);
}


int get_next_segment(int i) {
  return ( i + 1 ) % MAX_SEGMENTS;
}


int get_prev_segment(int i) {
  return ( i + MAX_SEGMENTS - 1 ) % MAX_SEGMENTS;
}


/**
 * Set the clock 2 timer frequency.
 * @input desired_freq_hz the desired frequency
 * Source: http://letsmakerobots.com/node/28278
 * Different clock sources can be selected for each timer independently. 
 * To calculate the timer frequency (for example 2Hz using timer1) you will need:
 */
void timer_set_frequency(long desired_freq_hz) {
  if(old_feed_rate==desired_freq_hz) return;
  old_feed_rate=desired_freq_hz;
  
  // interrupt frequency (Hz) = (Arduino clock speed 16,000,000Hz) / (prescaler * (compare match register + 1))
  // compare match register = [ 16,000,000Hz/ (prescaler * desired interrupt frequency) ] - 1
  
  // CPU frequency 16Mhz for Arduino
  // maximum timer counter value (256 for 8bit, 65536 for 16bit timer)
  int prescaler_index=-1;
  long prescalers[] = {CLOCK_FREQ / 1,
                       CLOCK_FREQ / 8,
                       CLOCK_FREQ / 64,
                       CLOCK_FREQ / 256,
                       CLOCK_FREQ / 1024};
  long counter_value;
  
  do {
    ++prescaler_index;
    //  Divide CPU frequency through the choosen prescaler (16000000 / 256 = 62500)
    //  Divide result through the desired frequency (62500 / 2Hz = 31250)
    counter_value = prescalers[prescaler_index] / desired_freq_hz - 1;
    //  Verify counter_value < maximum timer. if fail, choose bigger prescaler.
  } while(counter_value >= MAX_COUNTER && prescaler_index < TIMER_PRESCALER_COUNT );
  
  if( prescaler_index == TIMER_PRESCALER_COUNT ) {
    // @TODO: Serial.print() from inside the timer interrupt will probably crash the board.
    Serial.println(F("Timer could not be set: Desired frequency out of bounds."));
    return;
  }
  
  prescaler_index++;

#ifdef VERBOSE
  Serial.print(F("desired_freq_hz="));  Serial.println(desired_freq_hz);
  Serial.print(F("counter_value="));  Serial.println(counter_value);
  Serial.print(F(" prescaler_index="));  Serial.print(prescaler_index);
  Serial.print(F(" ("));
  Serial.print((prescaler_index&0x4)>>2);
  Serial.print((prescaler_index&0x2)>>1);
  Serial.print((prescaler_index&0x1)   );
  Serial.println(F(")"));
#endif

  // disable global interrupts
  noInterrupts();
  // set entire TCCR1A register to 0
  TCCR1A = 0;
  // set entire TCCR1B register to 0
  TCCR1B = 0;
  // set the overflow clock to 0
  TCNT1  = 0;
  // set compare match register to desired timer count
  OCR1A = counter_value;
  // turn on CTC mode
  TCCR1B |= (1 << WGM12);
  // Set CS10, CS11, and CS12 bits for prescaler
  TCCR1B |= ( (( prescaler_index&0x1 )   ) << CS10);
  TCCR1B |= ( (( prescaler_index&0x2 )>>1) << CS11);
  TCCR1B |= ( (( prescaler_index&0x4 )>>2) << CS12);
  // enable timer compare interrupt
  TIMSK1 |= (1 << OCIE1A);
  // enable global interrupts

  interrupts();  // enable global interrupts
}

 
/**
 * Process all line segments in the ring buffer.  Uses bresenham's line algorithm to move all motors.
 */
ISR(TIMER1_COMPA_vect) {
  // segment buffer empty? do nothing
  if( current_segment == last_segment ) return;
  
  // Is this segment done?
  if( line_segments[current_segment].steps_left <= 0 ) {
    // Move on to next segment without wasting an interrupt tick.
    current_segment = get_next_segment(current_segment);
    if( current_segment == last_segment ) return;
  }
  
  int j;
  Segment &seg = line_segments[current_segment];
  // is this a fresh new segment?
  if( seg.steps == seg.steps_left ) {
    // set the direction pins
    for(j=0;j<NUM_AXIES;++j) {
      digitalWrite( motors[j].dir_pin, line_segments[current_segment].a[j].dir );
    }
    //move the z axis
    servos[0].write(seg.a[2].step_count);
    digitalWrite(13,flipFlop);
    flipFlop = HIGH - flipFlop;
    
    // set frequency to segment feed rate
    timer_set_frequency(seg.feed_rate);
    
  }

  // make a step
  --seg.steps_left;

  // move each axis
  for(j=0;j<NUM_AXIES;++j) {
    Axis &a = seg.a[j];
    
    a.over += a.absdelta;
    if(a.over >= seg.steps) {
      laststep[j] += (a.dir==motors[j].reel_in?-1:1);
      digitalWrite(motors[j].step_pin,HIGH);
      a.over -= seg.steps;
      digitalWrite(motors[j].step_pin,LOW);
    }
  }
}


char segment_buffer_full() {
  int next_segment = get_next_segment(last_segment);
  return (next_segment == current_segment);
}


/**
 * Uses bresenham's line algorithm to move both motors
 **/
void motor_line(long n0,long n1,long n2,float new_feed_rate) {
  int next_segment = get_next_segment(last_segment);
  while( next_segment == current_segment ) {
    // the buffer is full, we are way ahead of the motion system
    delay(1);
  }

  Segment &new_seg = line_segments[last_segment];
  new_seg.a[0].step_count = n0;
  new_seg.a[1].step_count = n1;
  new_seg.a[2].step_count = n2;
  new_seg.steps=0;
  new_seg.feed_rate=new_feed_rate;
  
  Segment &old_seg = line_segments[get_prev_segment(last_segment)];
  new_seg.a[0].delta = n0 - old_seg.a[0].step_count;
  new_seg.a[1].delta = n1 - old_seg.a[1].step_count;
  new_seg.a[2].delta = n2 - old_seg.a[2].step_count;


  int i;
  for(i=0;i<NUM_AXIES;++i) {
    new_seg.a[i].over = 0;
    new_seg.a[i].dir = (new_seg.a[i].delta < 0 ? motors[i].reel_in:motors[i].reel_out);
    new_seg.a[i].absdelta = abs(new_seg.a[i].delta);
#ifdef VERBOSE
    Serial.print(i);
    Serial.print(F(" IS "));  Serial.print(new_seg.a[i].step_count);
    Serial.print(F("-"));  Serial.print(old_seg.a[i].step_count);
    Serial.print(F("="));  Serial.println(new_seg.a[i].delta);
    Serial.print(F("F"));  Serial.println(new_feed_rate);
#endif
    if( new_seg.steps < new_seg.a[i].absdelta ) {
      new_seg.steps = new_seg.a[i].absdelta;
    }
  }

  if( new_seg.steps==0 ) return;

  new_seg.steps_left = new_seg.steps;
  
  if( current_segment==last_segment ) {
    timer_set_frequency(new_feed_rate);
  }
  
#ifdef VERBOSE
  Serial.print(F("At "));  Serial.println(current_segment);
  Serial.print(F("Adding "));  Serial.println(next_segment);
  Serial.print(F("Steps= "));  Serial.println(new_seg.steps_left);
#endif
  last_segment = next_segment;
}


void wait_for_empty_segment_buffer() {
  while( current_segment != last_segment );
}


/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
