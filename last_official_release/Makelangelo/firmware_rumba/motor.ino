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
Segment *working_seg = NULL;
volatile int current_segment=0;
volatile int last_segment=0;
int step_multiplier, nominal_step_multiplier;
unsigned short nominal_OCR1A;

Servo servos[NUM_SERVOS];

// used by timer1 to optimize interrupt inner loop
int delta_x,delta_y;
int over_x,over_y;
int steps_total;
int steps_taken;
int accel_until,decel_after;
long current_feed_rate;
long old_feed_rate=0;
long start_feed_rate,end_feed_rate;
long time_accelerating,time_decelerating;

/*
long prescalers[] = {CLOCK_FREQ /   1,
                     CLOCK_FREQ /   8,
                     CLOCK_FREQ /  64,
                     CLOCK_FREQ / 256,
                     CLOCK_FREQ /1024};
*/

//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------


// for reasons I don't understand... if i put this method in the .ino file i get compile errors.
// so I put it here, which forces the externs.
FORCE_INLINE Segment *segment_get_working() {
  if(current_segment == last_segment ) return NULL;
  working_seg = &line_segments[current_segment];
  working_seg->busy=true;
  return working_seg;
}


int get_next_segment(int i) {
  return ( i + 1 ) & ( MAX_SEGMENTS - 1 );
}


int get_prev_segment(int i) {
  return ( i + MAX_SEGMENTS - 1 ) & ( MAX_SEGMENTS - 1 );
}


float max_speed_allowed(float acceleration, float target_velocity, float distance) {
  //return sqrt(target_velocity*target_velocity - 2*acceleration*distance);
  return target_velocity - acceleration * distance;
}


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

  current_segment=0;
  last_segment=0;
  Segment &old_seg = line_segments[get_prev_segment(last_segment)];
  old_seg.a[0].step_count=0;
  old_seg.a[1].step_count=0;
  old_seg.a[2].step_count=0;
  working_seg = NULL;

  // disable global interrupts
  noInterrupts();
  // set entire TCCR1A register to 0
  TCCR1A = 0;
  // set the overflow clock to 0
  TCNT1  = 0;
  // set compare match register to desired timer count
  OCR1A = 2000;  // 1ms
  // turn on CTC mode
  TCCR1B = (1 << WGM12);
  // Set 8x prescaler
  TCCR1B = (TCCR1B & ~(0x07<<CS10)) | (2<<CS10);
  // enable timer compare interrupt
  TIMSK1 |= (1 << OCIE1A);

  interrupts();  // enable global interrupts
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
  }
}




void recalculate_reverse2(Segment *prev,Segment *current,Segment *next) {
  if(current==NULL) return;
  if(next==NULL) return;

  if (current->feed_rate_start != current->feed_rate_start_max) {
    // If nominal length true, max junction speed is guaranteed to be reached. Only compute
    // for max allowable speed if block is decelerating and nominal length is false.
    if ((!current->nominal_length_flag) && (current->feed_rate_start_max > next->feed_rate_start)) {
      float v = min( current->feed_rate_start_max,
                     max_speed_allowed(-acceleration,next->feed_rate_start,current->steps_total));
      current->feed_rate_start = v;
    } else {
      current->feed_rate_start = current->feed_rate_start_max;
    }
    current->recalculate_flag = true;
  }
}


void recalculate_reverse() {
  int s = last_segment;
  Segment *blocks[3] = {NULL,NULL,NULL};

  while(s != current_segment) {
    s=get_prev_segment(s);
    blocks[2]=blocks[1];
    blocks[1]=blocks[0];
    blocks[0]=&line_segments[s];
    recalculate_reverse2(blocks[0],blocks[1],blocks[2]);
  }
}


void recalculate_forward2(Segment *prev,Segment *current,Segment *next) {
  if(prev==NULL) return;

  // If the previous block is an acceleration block, but it is not long enough to complete the
  // full speed change within the block, we need to adjust the entry speed accordingly. Entry
  // speeds have already been reset, maximized, and reverse planned by reverse planner.
  // If nominal length is true, max junction speed is guaranteed to be reached. No need to recheck.
  if (!prev->nominal_length_flag) {
    if (prev->feed_rate_start < current->feed_rate_start) {
      double feed_rate_start = min( current->feed_rate_start,
                                    max_speed_allowed(-acceleration,prev->feed_rate_start,prev->steps_total) );

      // Check for junction speed change
      if (current->feed_rate_start != feed_rate_start) {
        current->feed_rate_start = feed_rate_start;
        current->recalculate_flag = true;
      }
    }
  }
}


void recalculate_forward() {
  int s = current_segment;
  Segment *blocks[3] = {NULL,NULL,NULL};

  while(s != last_segment) {
    s=get_next_segment(s);
    blocks[0]=blocks[1];
    blocks[1]=blocks[2];
    blocks[2]=&line_segments[s];
    recalculate_forward2(blocks[0],blocks[1],blocks[2]);
  }
  recalculate_forward2(blocks[1],blocks[2],NULL);
}


int intersection_time(float acceleration,float distance,float start_speed,float end_speed) {
#if 0
  return ( ( 2.0*acceleration*distance - start_speed*start_speed + end_speed*end_speed ) / (4.0*acceleration) );
#else
  float t2 = ( start_speed - end_speed + acceleration * distance ) / ( 2.0 * acceleration );
  return distance - t2;
#endif
}


void segment_update_trapezoid(Segment *s,float start_speed,float end_speed) {
  if(start_speed<MIN_FEEDRATE) start_speed=MIN_FEEDRATE;
  if(end_speed<MIN_FEEDRATE) end_speed=MIN_FEEDRATE;

  //int steps_to_accel =  ceil( (s->feed_rate_max*s->feed_rate_max - start_speed*start_speed )/ (2.0*acceleration) );
  //int steps_to_decel = floor( (end_speed*end_speed - s->feed_rate_max*s->feed_rate_max )/ -(2.0*acceleration) );
  int steps_to_accel =  ceil( ( s->feed_rate_max - start_speed ) / acceleration );
  int steps_to_decel = floor( ( end_speed - s->feed_rate_max ) / -acceleration );

  int steps_at_top_speed = s->steps_total - steps_to_accel - steps_to_decel;

  if(steps_at_top_speed<=0) {
    steps_to_accel = ceil( intersection_time(acceleration,s->steps_total,start_speed,end_speed) );
    steps_at_top_speed=0;
    steps_to_accel = max(steps_to_accel,0);
    steps_to_accel = min(steps_to_accel,s->steps_total);
  }
/*
  Serial.print("M");  Serial.println(s->feed_rate_max);
  Serial.print("E");  Serial.println(end_speed);
  Serial.print("S");  Serial.println(start_speed);
  Serial.print("@");  Serial.println(acceleration);
  Serial.print("A");  Serial.println(steps_to_accel);
  Serial.print("D");  Serial.println(steps_to_decel);
*/
CRITICAL_SECTION_START
  if(s->busy==false) {
    s->accel_until = steps_to_accel;
    s->decel_after = steps_to_accel+steps_at_top_speed;
    s->feed_rate_start = start_speed;
    s->feed_rate_end = end_speed;
  }
CRITICAL_SECTION_END
}


void recalculate_trapezoids() {
  int s = current_segment;
  Segment *current;
  Segment *next = NULL;

  while(s != last_segment) {
    current = next;
    next = &line_segments[s];
    if (current) {
      // Recalculate if current block entry or exit junction speed has changed.
      if (current->recalculate_flag || next->recalculate_flag)
      {
        // NOTE: Entry and exit factors always > 0 by all previous logic operations.
        segment_update_trapezoid(current,current->feed_rate_start, next->feed_rate_start);
        current->recalculate_flag = false; // Reset current only to ensure next trapezoid is computed
      }
    }
    s=get_next_segment(s);
  }
  // Last/newest block in buffer. Make sure the last block always ends motion.
  if(next != NULL) {
    segment_update_trapezoid(next, next->feed_rate_start, MIN_FEEDRATE);
    next->recalculate_flag = false;
  }
}


void recalculate_acceleration() {
  recalculate_reverse();
  recalculate_forward();
  recalculate_trapezoids();

#if VERBOSE > 1
  //Serial.println("\nstart max,max,start,end,rate,total,up steps,cruise,down steps,nominal?");
  Serial.println("---------------");
  int s = current_segment;

  while(s != last_segment) {
    Segment *next = &line_segments[s];
    s=get_next_segment(s);
//                             Serial.print(next->feed_rate_start_max);
//    Serial.print(F("\t"));   Serial.print(next->feed_rate_max);
//    Serial.print(F("\t"));   Serial.print(acceleration);
    Serial.print(F("\tS"));  Serial.print(next->feed_rate_start);
//    Serial.print(F("\tE"));  Serial.print(next->feed_rate_end);
    Serial.print(F("\t*"));  Serial.print(next->steps_total);
    Serial.print(F("\tA"));  Serial.print(next->accel_until);
    int after = (next->steps_total - next->decel_after);
    int total = next->steps_total - after - next->accel_until;
    Serial.print(F("\tT"));  Serial.print(total);
    Serial.print(F("\tD"));  Serial.print(after);
    Serial.print(F("\t"));   Serial.println(next->nominal_length_flag==1?'*':' ');
  }
#endif
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


/**
 * Set the clock 2 timer frequency.
 * @input desired_freq_hz the desired frequency
 * Different clock sources can be selected for each timer independently.
 * To calculate the timer frequency (for example 2Hz using timer1) you will need:
 */
FORCE_INLINE unsigned short calc_timer(unsigned short desired_freq_hz) {
  if( desired_freq_hz > MAX_FEEDRATE ) desired_freq_hz = MAX_FEEDRATE;
  if( desired_freq_hz < MIN_FEEDRATE ) desired_freq_hz = MIN_FEEDRATE;
  old_feed_rate = desired_freq_hz;

  if( desired_freq_hz > 20000 ) {
    step_multiplier = 4;
    desired_freq_hz = (desired_freq_hz>>2)&0x3fff;
  } else if( desired_freq_hz > 10000 ) {
    step_multiplier = 2;
    desired_freq_hz = (desired_freq_hz>>1)&0x7fff;
  } else {
    step_multiplier = 1;
  }

  long counter_value = ( CLOCK_FREQ / 8 ) / desired_freq_hz;
  if( counter_value >= MAX_COUNTER ) {
    //Serial.print("this breaks the timer and crashes the arduino");
    //Serial.flush();
    counter_value = MAX_COUNTER - 1;
  } else if( counter_value < 100 ) {
    counter_value = 100;
  }

  return counter_value;
}


/**
 * Process all line segments in the ring buffer.  Uses bresenham's line algorithm to move all motors.
 */
ISR(TIMER1_COMPA_vect) {
  // segment buffer empty? do nothing
  if( working_seg == NULL ) {
    working_seg = segment_get_working();

    if( working_seg != NULL ) {
      // New segment!
      // set the direction pins
      digitalWrite( MOTOR_0_DIR_PIN, working_seg->a[0].dir );
      digitalWrite( MOTOR_1_DIR_PIN, working_seg->a[1].dir );

      //move the z axis
      servos[0].write(working_seg->a[2].step_count);

      // set frequency to segment feed rate
      nominal_OCR1A = calc_timer(working_seg->feed_rate_max);
      nominal_step_multiplier = step_multiplier;

      start_feed_rate = working_seg->feed_rate_start;
      end_feed_rate = working_seg->feed_rate_end;
      current_feed_rate = start_feed_rate;
      time_decelerating = 0;
      time_accelerating = calc_timer(start_feed_rate);
      OCR1A = time_accelerating;

      // defererencing some data so the loop runs faster.
      steps_total=working_seg->steps_total;
      steps_taken=0;
      delta_x = working_seg->a[0].absdelta;
      delta_y = working_seg->a[1].absdelta;
      over_x = -steps_total;
      over_y = -steps_total;
      accel_until=working_seg->accel_until;
      decel_after=working_seg->decel_after;
      return;
    } else {
      OCR1A = 2000; // wait 1ms
      return;
    }
  }

  if( working_seg != NULL ) {
    // move each axis
    for(uint8_t i=0;i<step_multiplier;++i) {
      // M0
      over_x += delta_x;
      if(over_x > 0) {
        digitalWrite(MOTOR_0_STEP_PIN,LOW);
        over_x -= steps_total;
        digitalWrite(MOTOR_0_STEP_PIN,HIGH);
      }
      // M1
      over_y += delta_y;
      if(over_y > 0) {
        digitalWrite(MOTOR_1_STEP_PIN,LOW);
        over_y -= steps_total;
        digitalWrite(MOTOR_1_STEP_PIN,HIGH);
      }
      // make a step
      steps_taken++;
      if(steps_taken>=steps_total) break;
    }

    // accel
    unsigned short t;
    if( steps_taken <= accel_until ) {
      current_feed_rate = (acceleration * time_accelerating / 1000000);
      current_feed_rate += start_feed_rate;
      t = calc_timer(current_feed_rate);
      OCR1A = t;
      time_accelerating+=t;
    } else if( steps_taken > decel_after ) {
      unsigned short step_time = (acceleration * time_decelerating / 1000000);
      long end_feed_rate = current_feed_rate - step_time;
      t = calc_timer(end_feed_rate);
      OCR1A = t;
      time_decelerating+=t;
    } else {
      OCR1A = nominal_OCR1A;
      step_multiplier = nominal_step_multiplier;
    }

    // Is this segment done?
    if( steps_taken >= steps_total ) {
      // Move on to next segment without wasting an interrupt tick.
      working_seg = NULL;
      current_segment = get_next_segment(current_segment);
    }
  }
}


/**
 * @return 1 if buffer is full, 0 if it is not.
 */
char segment_buffer_full() {
  int next_segment = get_next_segment(last_segment);
  return (next_segment == current_segment);
}


/**
 * Uses bresenham's line algorithm to move both motors
 **/
void motor_line(long n0,long n1,long n2,float new_feed_rate) {
  // get the next available spot in the segment buffer
  int next_segment = get_next_segment(last_segment);
  while( next_segment == current_segment ) {
    // the buffer is full, we are way ahead of the motion system
    delay(1);
  }

  // use LCD to adjust speed while drawing
  new_feed_rate *= (float)speed_adjust * 0.01f;

  int prev_segment = get_prev_segment(last_segment);
  Segment &new_seg = line_segments[last_segment];
  Segment &old_seg = line_segments[prev_segment];

  new_seg.a[0].step_count = n0;
  new_seg.a[1].step_count = n1;
  new_seg.a[2].step_count = n2;
  new_seg.a[0].delta = n0 - old_seg.a[0].step_count;
  new_seg.a[1].delta = n1 - old_seg.a[1].step_count;
  new_seg.a[2].delta = n2 - old_seg.a[2].step_count;
  new_seg.feed_rate_max = new_feed_rate;

  // the axis that has the most steps will control the overall acceleration
  new_seg.steps_total = 0;
  float len=0;
  int i;
  for(i=0;i<NUM_AXIES;++i) {
    new_seg.a[i].dir = (new_seg.a[i].delta < 0 ? motors[i].reel_in:motors[i].reel_out);
    new_seg.a[i].absdelta = abs(new_seg.a[i].delta);
    len += new_seg.a[i].absdelta * new_seg.a[i].absdelta;
    if( new_seg.steps_total < new_seg.a[i].absdelta ) {
      new_seg.steps_total = new_seg.a[i].absdelta;
    }
  }

  // No steps?  No work!  Stop now.
  if( new_seg.steps_total == 0 ) return;

  len = sqrt( len );
  float ilen = 1.0f / len;
  for(i=0;i<NUM_AXIES;++i) {
    new_seg.a[i].delta_normalized = new_seg.a[i].delta * ilen;
  }
  new_seg.steps_taken = 0;

  // what is the maximum starting speed for this segment?
  float feed_rate_start_max = MIN_FEEDRATE;
    // is the robot changing direction sharply?
      // aka is there a previous segment with a wildly different delta_normalized?
    if(last_segment != current_segment) {
      float cos_theta = 0;
      for(i=0;i<NUM_AXIES;++i) {
        cos_theta += new_seg.a[i].delta_normalized * old_seg.a[i].delta_normalized;
      }

      feed_rate_start_max = min( new_seg.feed_rate_max, old_seg.feed_rate_max );
      if(cos_theta<0.95) {
        if(cos_theta<0) cos_theta = 0;
        feed_rate_start_max *= cos_theta;
      }
    }

  float allowable_speed = max_speed_allowed(-acceleration, MIN_FEEDRATE, new_seg.steps_total);

  // come to a stop for entering or exiting a Z move
  if( new_seg.a[2].delta != 0 || old_seg.a[2].delta != 0 ) allowable_speed = MIN_FEEDRATE;

  //Serial.print("max = ");  Serial.println(feed_rate_start_max);
//  Serial.print("allowed = ");  Serial.println(allowable_speed);
  new_seg.feed_rate_start_max = feed_rate_start_max;
  new_seg.feed_rate_start = min(feed_rate_start_max, allowable_speed);

  new_seg.nominal_length_flag = ( allowable_speed >= new_seg.feed_rate_max );
  new_seg.recalculate_flag = true;
  new_seg.busy=false;

  // when should we accelerate and decelerate in this segment?
  segment_update_trapezoid(&new_seg,new_seg.feed_rate_start,MIN_FEEDRATE);

  recalculate_acceleration();

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
