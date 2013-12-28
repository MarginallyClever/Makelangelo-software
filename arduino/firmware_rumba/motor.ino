//------------------------------------------------------------------------------
// Draw robot - supports raprapdiscount RUMBA controller
// dan@marginallycelver.com 2013-12-26
// RUMBA should be treated like a MEGA 2560 Arduino.
//------------------------------------------------------------------------------
// Copyright at end of file.  Please see
// http://www.github.com/MarginallyClever/Makelangelo for more information.


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
Axis a[NUM_AXIES];  // for line()
Axis atemp;  // for line()
Motor motors[NUM_AXIES];

Segment line_segments[MAX_SEGMENTS];
volatile int current_segment;
volatile int last_segment;

int led_state=HIGH;

//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------
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


/**
 * set up the pins for each motor
 */
void motor_setup() {
  motors[0].step_pin=17;
  motors[0].dir_pin=16;
  motors[0].enable_pin=48;
  motors[0].limit_switch_pin=37;

  motors[1].step_pin=54;
  motors[1].dir_pin=47;
  motors[1].enable_pin=55;
  motors[1].limit_switch_pin=36;
  
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
  
  current_segment=0;
  last_segment=0;
  
  motor_set_step_count(0,0,0);
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


int get_next_segment(int i) {
  return ( i + 1 ) % MAX_SEGMENTS;
}


int get_prev_segment(int i) {
  return ( i + MAX_SEGMENTS - 1 ) % MAX_SEGMENTS;
}


/**
 * Set the clock 2 timer frequency.
 * @input desired_freq_hz the desired frequency
 */
void timer_set_frequency(long desired_freq_hz) {
  // Source: http://letsmakerobots.com/node/28278
  // Different clock sources can be selected for each timer independently. 
  // To calculate the timer frequency (for example 2Hz using timer1) you will need:
  
  //  CPU frequency 16Mhz for Arduino
  //  maximum timer counter value (256 for 8bit, 65536 for 16bit timer)
  int prescaler_index=-1;
  int prescalers[] = {1,8,64,256,1024};
  long counter_value;
  do {
    ++prescaler_index;
    //  Divide CPU frequency through the choosen prescaler (16000000 / 256 = 62500)
    counter_value = CLOCK_FREQ / prescalers[prescaler_index];
    //  Divide result through the desired frequency (62500 / 2Hz = 31250)
    counter_value /= desired_freq_hz;
    //  Verify counter_value < maximum timer. if fail, choose bigger prescaler.
  } while(counter_value > MAX_COUNTER && prescaler_index<4);
  
  if( prescaler_index>=5 ) {
    Serial.println(F("Timer could not be set: Desired frequency out of bounds."));
    return;
  }

#ifdef VERBOSE
  Serial.print(F("counter_value  ="));  Serial.print(counter_value);
  Serial.print(F(" prescaler_index="));  Serial.print(prescaler_index);
  Serial.print(F(" = "));  Serial.print(((prescaler_index&0x1)   ));
  Serial.print(F("/"));  Serial.print(((prescaler_index&0x2)>>1));
  Serial.print(F("/"));  Serial.println(((prescaler_index&0x4)>>2));
#endif

  // disable global interrupts
  noInterrupts();
  
  // set entire TCCR1A register to 0
  TCCR2A = 0;
  // set entire TCCR1B register to 0
  TCCR2B = 0;
  // set the overflow clock to 0
  TCNT2  = 0;
  // set compare match register to desired timer count
  OCR2A = counter_value;
  // turn on CTC mode
  TCCR2B |= (1 << WGM21);
  // Set CS10, CS11, and CS12 bits for prescaler
  TCCR2B |= ( (( prescaler_index&0x1 )   ) << CS20);
  TCCR2B |= ( (( prescaler_index&0x2 )>>1) << CS21);
  TCCR2B |= ( (( prescaler_index&0x4 )>>2) << CS22);
  // enable timer compare interrupt
  TIMSK2 |= (1 << OCIE2A);

  interrupts();  // enable global interrupts
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
 * Process all line segments in the ring buffer.  Uses bresenham's line algorithm to move all motors.
 */
ISR(TIMER2_COMPA_vect) {
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
      laststep[j]+= a.dir;
      digitalWrite(motors[j].step_pin,LOW);
      a.over -= seg.steps;
      digitalWrite(motors[j].step_pin,HIGH);
    }
  }
  
  led_state = led_state==HIGH? LOW:HIGH;
  digitalWrite(13, led_state);
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
    new_seg.a[i].dir = (new_seg.a[i].delta > 0 ? LOW:HIGH);
    new_seg.a[i].absdelta = abs(new_seg.a[i].delta);
    Serial.print(i);
    Serial.print(F(" IS "));  Serial.print(new_seg.a[i].step_count);
    Serial.print(F("-"));  Serial.print(old_seg.a[i].step_count);
    Serial.print(F("="));  Serial.println(new_seg.a[i].delta);
    if( new_seg.steps < new_seg.a[i].absdelta ) {
      new_seg.steps = new_seg.a[i].absdelta;
    }
  }

  if( new_seg.steps==0 ) return;

  new_seg.steps_left = new_seg.steps;
  
  if( current_segment==last_segment ) {
    timer_set_frequency(new_feed_rate);
  }
  
//#ifdef VERBOSE
  Serial.print(F("At "));  Serial.println(current_segment);
  Serial.print(F("Adding "));  Serial.println(next_segment);
  Serial.print(F("Steps= "));  Serial.println(new_seg.steps_left);
//#endif
  last_segment = next_segment;
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
