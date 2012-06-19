//------------------------------------------------------------------------------
// Draw robot
// dan@marginallycelver.com 2012 feb 11
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/i-make-robots/Drawbot for more information.


//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
// Adafruit motor driver library
#include <AFMotorDrawbot.h>

// Default servo library
#include <Servo.h> 

// Timer interrupt libraries
#include <FrequencyTimer2.h>

// For sleep_mode().  See http://www.engblaze.com/hush-little-microprocessor-avr-and-arduino-sleep-mode-basics/
#include <avr/interrupt.h>
#include <avr/power.h>
#include <avr/sleep.h>

// Saving config
#include <EEPROM.h>
#include <Arduino.h>  // for type definitions



//------------------------------------------------------------------------------
// CONSTANTS
//------------------------------------------------------------------------------
// Comment out this line to silence most serial output.
//#define VERBOSE         (1)

// which motor is on which pin?
#define M1_PIN          (1)
#define M2_PIN          (2)

// which limit switch is on which pin?
#define S1_PIN          (A3)
#define S2_PIN          (A5)

// which way are the spools wound, relative to motor movement?
#define REEL_IN         FORWARD
#define REEL_OUT        BACKWARD

// NEMA17 are 200 steps (1.8 degrees) per turn.  If a spool is 0.8 diameter
// then it is 2.5132741228718345 circumference, and
// 2.5132741228718345 / 200 = 0.0125663706 thread moved each step.
// NEMA17 are rated up to 3000RPM.  Adafruit can handle >1000RPM.
// These numbers directly affect the maximum velocity.
#define STEPS_PER_TURN  (200.0)
#define SPOOL_DIAMETER  (1.102)
#define MAX_RATED_RPM   (3000.0)
#define MAX_RPM         (200.0)

// *****************************************************************************
// *** Don't change the constants below unless you know what you're doing.   ***
// *****************************************************************************

// switch sensitivity
#define SWITCH_HALF     (512)

// servo angles for pen control
#define PEN_UP_ANGLE    (90)
#define PEN_DOWN_ANGLE  (10)  // Some steppers don't like 0 degrees
#define PEN_DELAY       (150)  // in ms

// calculate some numbers to help us find feed_rate
#define SPOOL_CIRC      (SPOOL_DIAMETER*PI)  // circumference
#define THREADPERSTEP   (SPOOL_CIRC/STEPS_PER_TURN)  // thread per step

// Speed of the timer interrupt
#define STEPS_S         (STEPS_PER_TURN*MAX_RPM/60.0)  // steps/s
#define TIMER_FREQUENCY (1000000.0/STEPS_S)  // microseconds/step

// The interrupt makes calls to move the stepper.
// the maximum speed cannot be faster than the timer interrupt.
#define MAX_VEL         (STEPS_S * THREADPERSTEP)  // cm/s
#define MIN_VEL         (0.001) // cm/s
#define DEFAULT_VEL     (MAX_VEL/2)  // cm/s

// How fast can the plotter accelerate?
#define ACCELERATION    (DEFAULT_VEL)  // cm/s/s

// for arc directions
#define ARC_CW          (1)
#define ARC_CCW         (-1)
// Arcs are split into many line segments.  How long are the segments?
#define CM_PER_SEGMENT   (0.1)

// Serial communication bitrate
#define BAUD            (57600)
// Maximum length of serial input message.
#define MAX_BUF         (64)

// look-ahead planning
#ifdef __AVR_ATmega2560__
#define MAX_BLOCKS       (16)
#else
#define MAX_BLOCKS       (5)
#endif
#define NEXT_BLOCK(x)    ((x+1)%MAX_BLOCKS)
#define PREV_BLOCK(x)    ((x+MAX_BLOCKS-1)%MAX_BLOCKS)

// servo pin differs based on device
#ifdef __AVR_ATmega2560__
#define SERVO_PIN        50
#else
#define SERVO_PIN        9
#endif



//------------------------------------------------------------------------------
// STRUCTURES
//------------------------------------------------------------------------------

// based on https://github.com/grbl/grbl/ planner
typedef struct {
  long m1s, m1e, m1total, m1o;
  long m2s, m2e, m2total, m2o;
  long step_count;
  int m1dir,m2dir;

  float sz, ez;
  float t1, t2, time, tsum, tstart;
  float startv,endv,topv,maxstartv;
  float len;
  char touched;
  char nominal_length;
} block;



//------------------------------------------------------------------------------
// VARIABLES
//------------------------------------------------------------------------------
// Initialize Adafruit stepper controller
static AF_Stepper m1((int)STEPS_PER_TURN, M2_PIN);
static AF_Stepper m2((int)STEPS_PER_TURN, M1_PIN);

static Servo s1;

// plotter limits
// all distances are relative to the calibration point of the plotter.
// (normally this is the center of the drawing area)
static float limit_top = 21.5;  // distance to top of drawing area.
static float limit_bottom =-30.0;  // Distance to bottom of drawing area.
static float limit_right = 14.0;  // Distance to right of drawing area.
static float limit_left =-14.0;  // Distance to left of drawing area.

// plotter position.
static float posx, velx, accelx;
static float posy, vely, accely;
static float posz;  // pen state

// old pen position
static int old_pen_angle=-1;

// switch state
static char switch1;
static char switch2;

// motor position
static volatile long laststep1, laststep2;

// speeds, feeds, and delta-vs.
static float accel=ACCELERATION;
static float feed_rate=DEFAULT_VEL;

static char absolute_mode=1;  // absolute or incremental programming mode?
static float mode_scale;   // mm or inches?
static char mode_name[3];

// time values
static long  t_millis;
static float t;   // since board power on
static float dt;  // since last tick

// Diameter of line made by plotter
static float tool_diameter=0.05;

// Serial comm reception
static char buffer[MAX_BUF];  // Serial buffer
static int sofar;             // Serial buffer progress

// look-ahead ring buffer
static block blocks[MAX_BLOCKS];
static volatile int block_head, block_tail;
static block *current_block=NULL;
static float previous_topv;

// timer interrupt blocking
static char planner_busy=0;
static char planner_awake=0;


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------



//------------------------------------------------------------------------------
// increment internal clock
static void tick() {
  long nt_millis=millis();
  long dt_millis=nt_millis-t_millis;

  t_millis=nt_millis;

  dt=(float)dt_millis*0.001;  // time since last tick, in seconds
  t=(float)nt_millis*0.001;
}


//------------------------------------------------------------------------------
// returns angle of dy/dx as a value from 0...2PI
static float atan3(float dy,float dx) {
  float a=atan2(dy,dx);
  if(a<0) a=(PI*2.0)+a;
  return a;
}


//------------------------------------------------------------------------------
static void readSwitches() {
  // get the current switch state
  switch1=analogRead(S1_PIN) > SWITCH_HALF;
  switch2=analogRead(S2_PIN) > SWITCH_HALF;
}


//------------------------------------------------------------------------------
void planner_setup() {
  FrequencyTimer2::disable();
  block_head=0;
  block_tail=0;
  previous_topv=0;
  current_block=NULL;
  planner_idle();
}


//------------------------------------------------------------------------------
void planner_idle() {
  if(planner_awake) {
    FrequencyTimer2::setPeriod(TIMER_FREQUENCY);  // microseconds
    FrequencyTimer2::setOnOverflow(plan_step);
    FrequencyTimer2::disable();
    m1.release();
    m2.release();
    planner_awake=0;
  }
}


//------------------------------------------------------------------------------
void planner_wakeup() {
  if(!planner_awake) {
    FrequencyTimer2::setPeriod(TIMER_FREQUENCY);  // microseconds
    FrequencyTimer2::setOnOverflow(plan_step);
    FrequencyTimer2::enable();
    planner_awake=1;
  }
}


//------------------------------------------------------------------------------
void setup_jogger() {
  FrequencyTimer2::disable();
  block_head=0;
  block_tail=0;
  current_block=NULL;
  FrequencyTimer2::setPeriod(TIMER_FREQUENCY);  // microseconds
  FrequencyTimer2::setOnOverflow(jog_step);
  FrequencyTimer2::enable();
}


//------------------------------------------------------------------------------
// feed rate is given in units/min and converted to cm/s
static void setFeedRate(float v) {
  v *= mode_scale/60.0;
  if( feed_rate != v ) {
    feed_rate=v;
    if(feed_rate>MAX_VEL) feed_rate=MAX_VEL;
    if(feed_rate<MIN_VEL) feed_rate=MIN_VEL;
  }
}


//------------------------------------------------------------------------------
static void printFeedRate() {
  Serial.print(feed_rate*60.0/mode_scale);
  Serial.print(mode_name);
  Serial.print("/min");
}


//------------------------------------------------------------------------------
// Change pen state.
static void setPenAngle(int pen_angle) {
  if(old_pen_angle!=pen_angle) {
    old_pen_angle=pen_angle;
    
    if(old_pen_angle<PEN_DOWN_ANGLE) old_pen_angle=PEN_DOWN_ANGLE;
    if(old_pen_angle>PEN_UP_ANGLE  ) old_pen_angle=PEN_UP_ANGLE;

//    Serial.println( old_pen_angle );
    
    s1.write( old_pen_angle );
  }
}


//------------------------------------------------------------------------------
// Inverse Kinematics - turns XY coordinates into lengths L1,L2
static void IK(float x, float y, long &l1, long &l2) {
  // find length to M1
  float dy = y - limit_top;
  float dx = x - limit_left;
  l1 = floor( sqrt(dx*dx+dy*dy) / THREADPERSTEP );
  // find length to M2
  dx = limit_right - x;
  l2 = floor( sqrt(dx*dx+dy*dy) / THREADPERSTEP );
}


//------------------------------------------------------------------------------
// Forward Kinematics - turns L1,L2 lengths into XY coordinates
// use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
// to find angle between M1M2 and M1P where P is the plotter position.
static void FK(float l1, float l2,float &x,float &y) {
  float a = l1 * THREADPERSTEP;
  float b = (limit_right-limit_left);
  float c = l2 * THREADPERSTEP;
  
  // slow, uses trig
  //float theta = acos((a*a+b*b-c*c)/(2.0*a*b));
  //x = cos(theta)*l1 + limit_left;
  //y = sin(theta)*l1 + limit_top;
  // but we know that cos(acos(i)) = i
  // and we know that sin(acos(i)) = sqrt(1-i*i)
  float i=(a*a+b*b-c*c)/(2.0*a*b);
  x = i * l1 + limit_left;
  y = sqrt(1.0 - i*i)*l1 + limit_top;
}


//------------------------------------------------------------------------------
// Turns the motors so that the real robot strings match our simulation.
// len1 & len2 are the current string lengths.
// nlen1 & nlen2 are the new string lengths.
static void adjustStringLengths(long nlen1,long nlen2) {
  // is the change in length >= one step?
  long steps=nlen1-laststep1;
//  if(steps<0)      m1.onestep(REEL_IN );  // it is shorter.
//  else if(steps>0) m1.onestep(REEL_OUT);  // it is longer.
  if(steps<0)      m1.step(-steps,REEL_IN );  // it is shorter.
  else if(steps>0) m1.step( steps,REEL_OUT);  // it is longer.
  
  // is the change in length >= one step?
  steps=nlen2-laststep2;
//  if(steps<0)      m2.onestep(REEL_IN );  // it is shorter.
//  else if(steps>0) m2.onestep(REEL_OUT);  // it is longer.
  if(steps<0)      m2.step(-steps,REEL_IN );  // it is shorter.
  else if(steps>0) m2.step( steps,REEL_OUT);  // it is longer.
  
  laststep1=nlen1;
  laststep2=nlen2;
}


//------------------------------------------------------------------------------
static void travelTime(float len,float vstart,float vend,float vtop,float &t1,float &t2,float &t3) {
  t1 = (vtop-vstart) / accel;
  t2 = (vtop-vend) / accel;
  float d1 = vstart * t1 + 0.5 * accel * t1*t1;
  float d2 = vend * t2 + 0.5 * accel * t2*t2;
  float a = d1+d2;
  
  if(len>a) {
    t3 = t1+t2 + (len-a) / vtop;
    t2 = t3-t2;
  } else {
    // http://wikipedia.org/wiki/Classical_mechanics#1-Dimensional_Kinematics
    float brake_distance=(2.0*accel*len-vstart*vstart+vend*vend) / (4.0*accel);
    // and we also know d=v0*t + att/2
    // so 
    t2 = t1 = ( -vstart + sqrt( 2.0*accel*brake_distance       + vstart*vstart ) ) / accel;
    t3 = t1 + ( -vend   + sqrt( 2.0*accel*(len-brake_distance) + vend*vend     ) ) / accel;
  }
/*
  Serial.print("\tlen=");  Serial.print(len);
  Serial.print("\taccel=");  Serial.print(accel);
  Serial.print("\tvstart=");  Serial.print(vstart);
  Serial.print("\tvend=");  Serial.print(vend);
  Serial.print("\tt1=");  Serial.print(t1);
  Serial.print("\tt2=");  Serial.print(t2);
  Serial.print("\tt3=");  Serial.println(t3);
*/
}


//------------------------------------------------------------------------------
static float interpolate(float p0,float p3,float t,float t1,float t2,float t3,float vstart,float vtop,float vend,float len) {
  if(t<=0) return p0;
 
  float s = (p3-p0)/len;
  
  if(t<t1) {
    float d1 = vstart * t + 0.5 * accel * t * t;
    return p0 + (d1)*s;
  } else if(t < t2) {
    float d1 = vstart * t1 + 0.5 * accel * t1 * t1;
    float d2 = vtop * ( t - t1 );
    return p0 + (d1+d2)*s;
  } else if(t < t3) {
    float d1 = vstart * t1 + 0.5 * accel * t1 * t1;
    float d2 = vtop * ( t2 - t1 );
    float t4 = t - t2;
    float d3 = vtop * t4 - 0.5 * accel * t4 * t4;
    return p0 + (d1+d2+d3)*s;
  }
  
  return p3;
}


//------------------------------------------------------------------------------
// called by the timer interrupt when in jog mode
static void jog_step() {
  long nlen1, nlen2;

  tick();
  
  // automatic slowdown when key is released.  
  if(velx!=0 && accelx==0) {
    if(abs(velx)<accel*dt) velx=0;
    else if(velx<0)        velx+=accel*dt;
    else                   velx-=accel*dt;
  }
  if(vely!=0 && accely==0) {
    if(abs(vely)<accel*dt) vely=0;
    else if(vely<0)        vely+=accel*dt;
    else                   vely-=accel*dt;
  }

  velx+=accelx*dt;
  vely+=accely*dt;
  float vtotal = sqrt(velx*velx+vely*vely);
  if(vtotal>feed_rate) {
    float scale = feed_rate/vtotal;
    velx*=scale;
    vely*=scale;
  }
  posx+=velx*dt;
  posy+=vely*dt;
  
  IK(posx,posy,nlen1,nlen2);
  adjustStringLengths(nlen1,nlen2);
}


//------------------------------------------------------------------------------
// change the acceleration (for drive-by kb or joystick)
static void jog(float x,float y) {
  float ax=x*accel;
  float ay=y*accel;

  float atotal = sqrt(ax*ax+ay*ay);
  if(atotal>accel) {
    float scale = accel/atotal;
    ax*=scale;
    ay*=scale;
  }

  accelx=ax;
  accely=ay;
}


//------------------------------------------------------------------------------
// called by the timer interrupt when in plan mode.
void plan_step() {
  if(planner_busy==1) return;
  planner_busy=1;

  sei();

  readSwitches();

  if(!switch1 || !switch2) {
    // Emergency stop, switch hit unexpectedly.
    planner_busy=0;
    return;
  }

  tick();
  
  if(current_block==NULL) {
    if(block_tail!=block_head) {
      current_block=&blocks[block_tail];
      current_block->tstart=t;
      current_block->tsum=0;
    }
    if(current_block==NULL) {
      planner_idle();
      planner_busy=0;
      return;
    }
  }
  
  if(current_block!=NULL) {
    current_block->tsum = t - current_block->tstart;
    if(current_block->tsum > current_block->time) {
      current_block->tsum = current_block->time;
    }

    long nlen1,nlen2;
    // Find where the plotter will be at tsum seconds
    //float nx = interpolate(current_block->sx, current_block->ex, current_block);
    //float ny = interpolate(current_block->sy, current_block->ey, current_block);
    //float nz = (current_block->ez - current_block->sz) * (current_block->tsum / current_block->time) + current_block->sz;

    // update motors
    //IK(nx,ny,nlen1,nlen2);

    long l1 = interpolate( current_block->m1s, current_block->m1e, current_block->tsum, current_block->t1, current_block->t2, current_block->time, current_block->startv, current_block->topv, current_block->endv, current_block->len );
    long l2 = interpolate( current_block->m2s, current_block->m2e, current_block->tsum, current_block->t1, current_block->t2, current_block->time, current_block->startv, current_block->topv, current_block->endv, current_block->len );

    adjustStringLengths(nlen1,nlen2);
    // move the pen
    //setPenAngle(current_block->ez);

    // is this block finished?    
    if(current_block->tsum >= current_block->time) {
      current_block=NULL;
      if(block_tail!=block_head) {
        // get next block
        block_tail=NEXT_BLOCK(block_tail);
      }
    }
  }
  
  planner_busy=0;
}


//------------------------------------------------------------------------------
// if you must reach target_vel at distance and you speed up by acceleration
// then what must your starting velocity be?
static float max_allowable_speed(float acceleration,float target_vel,float distance) {
  return sqrt( target_vel * target_vel - 2.0 * acceleration * distance );
}


//------------------------------------------------------------------------------
static void planner_recalculate() {
  block *curr, *next, *prev;

  // reverse pass
  prev=curr=next=NULL;
  int bi = block_head;
  while(bi != block_tail ) {
    bi=PREV_BLOCK(bi);
    next=curr;
    curr=prev;
    prev=&blocks[bi];
    
    if( !curr || !next ) continue;
    // already cruising at top speed?
    if(curr->startv == curr->maxstartv ) continue;
    if(!curr->nominal_length && curr->maxstartv > next->startv ) {
      curr->startv = min(curr->maxstartv, max_allowable_speed( -accel, next->startv, curr->len ) );
    } else {
      curr->startv = curr->maxstartv;
    }
    curr->touched=1;
  }
  
  // forward pass
  prev=curr=next=NULL;
  bi = block_tail;
  while(bi != block_head ) {
    prev=curr;
    curr=next;
    next=&blocks[bi];
    bi=NEXT_BLOCK(bi);
    
    if( !prev ) continue;
    if( prev->nominal_length ) continue;
    
    if(!prev->startv > curr->startv ) {
      float startv = min(curr->startv, max_allowable_speed( -accel, prev->startv, prev->len ) );
      if( curr->startv != startv ) {
        curr->startv = startv;
        curr->touched=1;
      }
    }
    curr->touched=1;
  }
  
  // recalculate trapezoids
  prev=curr=next=NULL;
  bi = block_tail;
  while(bi != block_head ) {
    curr=next;
    next=&blocks[bi];
    bi=NEXT_BLOCK(bi);
    
    if( !curr ) continue;

    if( curr->touched==1 || next->touched==1 ) {
      curr->endv=next->startv;
      travelTime(curr->len,curr->startv,curr->endv,curr->topv,curr->t1,curr->t2,curr->time);
      curr->touched=0;
    }
  }
  
  // last item in queue
  next->endv=0;
  travelTime(next->len,next->startv,next->endv,next->topv,next->t1,next->t2,next->time);
  next->touched=0;
}


//------------------------------------------------------------------------------
// This method assumes the limits have already been checked.
// It adds each line segment to a ring buffer.  Then it plans a route so that
// if lines are in the same direction the robot doesn't have to slow down.
static void line_queue(float x,float y,float z) {
#ifdef VERBOSE
  Serial.print("A");  Serial.print(block_tail);
  Serial.print(" ");  Serial.print(block_head);
  Serial.print(" x");  Serial.print(x);
  Serial.print(" y");  Serial.print(y);

  if(current_block) {
    Serial.print(" ");  Serial.print(current_block->tsum);
    Serial.print(" ");  Serial.print(current_block->time);
  }
  Serial.println(planner_busy?" busy":" idle");
#endif

  int next_head=NEXT_BLOCK(block_head);
  // while there is no room in the queue, wait.
  while(block_tail==next_head) sleep_mode();
    
  // set up the new block in the queue.
  float dx=x-posx;
  float dy=y-posy;
  float dz=z-posz;

#ifdef VERBOSE
  Serial.print("px ");  Serial.print(posx);
  Serial.print("py ");  Serial.print(posy);
  Serial.print("pz ");  Serial.print(posz);
  Serial.print("dx ");  Serial.print(dx);
  Serial.print("dy ");  Serial.print(dy);
  Serial.print("dz ");  Serial.println(dz);
#endif

  block *new_block=&blocks[block_head];
  IK(posx,posy,new_block->m1s,new_block->m2s);
  IK(   x,   y,new_block->m1e,new_block->m2e);
  new_block->m1total=abs(new_block->m1e-new_block->m1s);
  new_block->m2total=abs(new_block->m2e-new_block->m2s);
  new_block->step_count=max(new_block->m1total,new_block->m2total);
  new_block->m1dir = new_block->m1e > new_block->m1s ? 1 : -1;
  new_block->m2dir = new_block->m2e > new_block->m2s ? 1 : -1;
  new_block->m1o=0;
  new_block->m2o=0;

  new_block->sz=posz;
  new_block->ez=z;
  new_block->startv=0;
  new_block->endv=0;
  new_block->topv=feed_rate;
  new_block->len = sqrt(dx*dx + dy*dy);// + dz*dz);
  new_block->tsum=0;

  //if(new_block->len==0) return;
  if( sqrt(dx*dx + dy*dy + dz*dz) == 0 ) return;
  
  // Find the maximum speed around the corner from the previous line to this line.
  float maxjunctionv = 0.0;
  block *curr,*next;
  if( block_head != block_tail ) {
    // there is a previous line to compare to
    next = new_block;
    curr=&blocks[PREV_BLOCK(block_head)];

    // dot product the two vectors to get the cos(theta) of their angle.
    float x1 = ( curr->m1e - curr->m1s ) / curr->len;
    float y1 = ( curr->m2e - curr->m2s ) / curr->len;
    //float z1 = ( curr->ez - curr->sz ) / curr->len;
    float x2 = ( next->m1s - next->m1s ) / next->len;
    float y2 = ( next->m2e - next->m2s ) / next->len;
    //float z2 = ( curr->ez - curr->sz ) / curr->len;
    float dotproduct = x1*x2 + y1*y2;// + z1*z2;

    // Anything more than 90 is a full stop guaranteed.
    if( dotproduct > 0 ) {
      maxjunctionv = min( curr->topv * dotproduct, next->topv * dotproduct );
    }
  }
  new_block->maxstartv = maxjunctionv;
  
  float allowablev = max_allowable_speed(-accel,0,new_block->len);

  new_block->startv = min( maxjunctionv, allowablev );
  // no matter at what speed we start, is this line long enough to reach top speed?
  new_block->nominal_length = ( new_block->topv < allowablev );

#ifdef VERBOSE
  Serial.print("maxstartv=");   Serial.println(maxjunctionv);
  Serial.print("allowablev=");  Serial.println(allowablev);
  Serial.print("startv=");      Serial.println(new_block->startv);
#endif

  // make sure the trapezoid is calculated for this block
  new_block->touched = 1;
  
#ifdef VERBOSE
  Serial.print("B");  Serial.print(block_tail);
  Serial.print(" ");  Serial.println(next_head);
#endif

  // the line is now ready to be queued.
  // move the head
  block_head=next_head;

  posx=x;
  posy=y;
  posz=z;
  previous_topv=new_block->topv;
  
  planner_recalculate();
  planner_wakeup();
}


//------------------------------------------------------------------------------
static void line(float x,float y,float z) {
  long l1,l2;
  IK(x,y,l1,l2);
  long d1 = l1 - laststep1;
  long d2 = l2 - laststep2;

  long ad1=abs(d1);
  long ad2=abs(d2);
  int dir1=d1<0?REEL_IN:REEL_OUT;
  int dir2=d2<0?REEL_IN:REEL_OUT;
  long overflow=0;
  long i;

  int step_delay=5;

  // bresenham's line algorithm.
  if(ad1>ad2) {
    for(i=0;i<ad1;++i) {
      m1.onestep(dir1);
      delay(step_delay);
      overflow+=ad2;
      if(overflow>=ad1) {
        overflow-=ad1;
        m2.onestep(dir2);
        delay(step_delay);
      }
    }
  } else {
    for(i=0;i<ad2;++i) {
      m2.onestep(dir2);
      delay(step_delay);
      overflow+=ad1;
      if(overflow>=ad2) {
        overflow-=ad2;
        m1.onestep(dir1);
        delay(step_delay);
      }
    }
  }

  laststep1=l1;
  laststep2=l2;
  posx=x;
  posy=y;
}


//------------------------------------------------------------------------------
// This method assumes the limits have already been checked.
// This method assumes the start and end radius match.
// This method assumes arcs are not >180 degrees (PI radians)
// cx/cy - center of circle
// x/y - end position
// dir - ARC_CW or ARC_CCW to control direction of arc
static void arc(float cx,float cy,float x,float y,float z,float dir) {
  // get radius
  float dx = posx - cx;
  float dy = posy - cy;
  float radius=sqrt(dx*dx+dy*dy);

  // find angle of arc (sweep)
  float angle1=atan3(dy,dx);
  float angle2=atan3(y-cy,x-cx);
  float theta=angle2-angle1;
  
  if(dir>0 && theta<0) angle2+=2*PI;
  else if(dir<0 && theta>0) angle1+=2*PI;
  
  theta=angle2-angle1;
  
  // get length of arc
  // float circ=PI*2.0*radius;
  // float len=theta*circ/(PI*2.0);
  // simplifies to
  float len = abs(theta) * radius;

  int i, segments = floor( len / CM_PER_SEGMENT );
 
  float nx, ny, nz, angle3, scale;

  for(i=0;i<segments;++i) {
    // interpolate around the arc
    scale = ((float)i)/((float)segments);
    
    angle3 = ( theta * scale ) + angle1;
    nx = cx + cos(angle3) * radius;
    ny = cy + sin(angle3) * radius;
    nz = ( z - posz ) * scale + posz;
    // send it to the planner
    line(nx,ny,nz);
  }
  
  line(x,y,z);
}


//------------------------------------------------------------------------------
// loads 5m onto a spool.
static void loadspools() {
  float len=500.0;
  float amnt=len/THREADPERSTEP;
  Serial.print("== LOAD ");
  Serial.print(len);
  Serial.print(" ==");
  // uncomment the motor you want to load
  m1.step(amnt,REEL_IN);
  //m2.step(amnt,REEL_IN);
}


//------------------------------------------------------------------------------
// instantly move the virtual plotter position
// does not validate if the move is valid
static void teleport(float x,float y) {
  posx=x;
  posy=y;
  
  // @TODO: posz?
  long L1,L2;
  IK(posx,posy,L1,L2);
  laststep1=L1;
  laststep2=L2;
}


//------------------------------------------------------------------------------
static void help() {
  Serial.println("== DRAWBOT - http://github.com/i-make-robots/Drawbot/ ==");
  Serial.println("All commands end with a semi-colon.");
  Serial.println("HELP;  - display this message");
  Serial.println("CONFIG [Tx.xx] [Bx.xx] [Rx.xx] [Lx.xx];");
  Serial.println("       - display/update this robot's configuration.");
  Serial.println("HOME;  - recalibrate and move to 0,0");
  Serial.println("WHERE; - display current virtual coordinates");
  Serial.println("TELEPORT [Xx.xx] [Yx.xx]; - move the virtual plotter.");
  Serial.println("As well as the following G-codes (http://en.wikipedia.org/wiki/G-code):");
  Serial.println("G00,G01,G02,G03,G04,G20,G21,G90,G91");
}


//------------------------------------------------------------------------------
// find the current robot position and 
static void goHome() {
  Serial.println("Homing...");
  readSwitches();
  
  if(!switch1 || !switch2) {
    Serial.println("** ERROR **");
    Serial.println("Problem: Plotter is already touching switches.");
    Serial.println("Solution: Please unwind the strings a bit and try again.");
    return;
  }
  
  // reel in the left motor until contact is made.
  Serial.println("Find left...");
  do {
    m1.step(1,REEL_IN );
    m2.step(1,REEL_OUT);
    readSwitches();
  } while(switch1==1);
  laststep1=0;
  
  // reel in the right motor until contact is made
  Serial.println("Find right...");
  do {
    m2.step(1,REEL_IN );
    m1.step(1,REEL_OUT);
    laststep1++;
    readSwitches();
  } while(switch2==1);
  laststep2=0;
  
  Serial.println("Centering...");
  long L1,L2;
  IK(0,0,L1,L2);
  adjustStringLengths(L1,L2);
}


//------------------------------------------------------------------------------
static void where() {
  Serial.print("(");
  Serial.print(posx);
  Serial.print(",");
  Serial.print(posy);
  Serial.print(",");
  Serial.print(posz);
  Serial.print(")@");
  printFeedRate();
  Serial.print("\n");
}


//------------------------------------------------------------------------------
static void printConfig() {
  Serial.print("T");    Serial.println(limit_top);
  Serial.print("B");    Serial.println(limit_bottom);
  Serial.print("L");    Serial.println(limit_left);
  Serial.print("R");    Serial.println(limit_right);
  Serial.print("F");    printFeedRate();
  Serial.print("\nA");  Serial.println(accel);
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
void EEPROM_writeDouble(int ee, float value) {
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
float EEPROM_readDouble(int ee) {
  float value = 0.0;
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


//------------------------------------------------------------------------------
static void loadConfig() {
  char version=EEPROM.read(0);
  if(version==1) {
    limit_top   =EEPROM_readDouble(1);
    limit_bottom=EEPROM_readDouble(5);
    limit_right =EEPROM_readDouble(9);
    limit_left  =EEPROM_readDouble(13);
  }
}


//------------------------------------------------------------------------------
static void saveConfig() {
  char version=1;
  EEPROM.write( 0,version);
  EEPROM_writeDouble( 1,limit_top);
  EEPROM_writeDouble( 5,limit_bottom);
  EEPROM_writeDouble( 9,limit_right);
  EEPROM_writeDouble(13,limit_left);
}


//------------------------------------------------------------------------------
static void processCommand() {
#ifndef SMALL_FOOTPRINT
  if(!strncmp(buffer,"HELP",4)) {
    help();
  } else if(!strncmp(buffer,"HOME",4)) {
    goHome();
  } else if(!strncmp(buffer,"TELEPORT",8)) {
    float xx=posx;
    float yy=posy;
  
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1)*mode_scale;  break;
      case 'Y': yy=atof(ptr+1)*mode_scale;  break;
      default: ptr=0; break;
      }
    }

    teleport(xx,yy);
  } else 
#endif
  if(!strncmp(buffer,"WHERE",5)) {
    where();
  } else if(!strncmp(buffer,"CONFIG",6)) {
    float tt=limit_top;
    float bb=limit_bottom;
    float rr=limit_right;
    float ll=limit_left;
    
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'T': tt=atof(ptr+1);  break;
      case 'B': bb=atof(ptr+1);  break;
      case 'R': rr=atof(ptr+1);  break;
      case 'L': ll=atof(ptr+1);  break;
      default: ptr=0; break;
      }
    }
    
    // @TODO: check t>b, r>l ?
    limit_top=tt;
    limit_bottom=bb;
    limit_right=rr;
    limit_left=ll;
    
    saveConfig();
    printConfig();
  } else if(!strncmp(buffer,"G00 ",4) || !strncmp(buffer,"G01 ",4)
         || !strncmp(buffer,"G0 " ,3) || !strncmp(buffer,"G1 " ,3) ) {
    // line
    float xx, yy, zz;
    
    if(absolute_mode==1) {
      xx=posx;
      yy=posy;
      zz=posz;
    } else {
      xx=0;
      yy=0;
      zz=0;
    }
  
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1)*mode_scale;  break;
      case 'Y': yy=atof(ptr+1)*mode_scale;  break;
      case 'Z': zz=atof(ptr+1);  break;
      case 'F': setFeedRate(atof(ptr+1));  break;
      default: ptr=0; break;
      }
    }
 
    if(absolute_mode==0) {
      xx+=posx;
      yy+=posy;
      zz+=posz;
    }
    
    line(xx,yy,zz);
  } else if(!strncmp(buffer,"G02 ",4) || !strncmp(buffer,"G2 " ,3) 
         || !strncmp(buffer,"G03 ",4) || !strncmp(buffer,"G3 " ,3)) {
    // arc
    float xx, yy, zz;
    float dd = (!strncmp(buffer,"G02",3) || !strncmp(buffer,"G2",2)) ? -1 : 1;
    float ii = 0;
    float jj = 0;
    
    if(absolute_mode==1) {
      xx=posx;
      yy=posy;
      zz=posz;
    } else {
      xx=0;
      yy=0;
      zz=0;
    }
    
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'I': ii=atof(ptr+1)*mode_scale;  break;
      case 'J': jj=atof(ptr+1)*mode_scale;  break;
      case 'X': xx=atof(ptr+1)*mode_scale;  break;
      case 'Y': yy=atof(ptr+1)*mode_scale;  break;
      case 'Z': zz=atof(ptr+1);  break;
      case 'F': setFeedRate(atof(ptr+1));  break;
      default: ptr=0; break;
      }
    }
 
    if(absolute_mode==0) {
      xx+=posx;
      yy+=posy;
      zz+=posz;
    }

    arc(posx+ii,posy+jj,xx,yy,zz,dd);
  } else if(!strncmp(buffer,"G04 ",4) || !strncmp(buffer,"G4 ",3)) {
    // dwell
    long xx=0;

    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': 
      case 'U': 
      case 'P': xx=atoi(ptr+1);  break;
      default: ptr=0; break;
      }
    }

    delay(xx);
  } else if(!strncmp(buffer,"J00",3)) {
    // start jog mode
    planner_setup();
    Serial.println("Planner on");
  } else if(!strncmp(buffer,"J01",3)) {
    // end jog mode
    setup_jogger();
    Serial.println("Jog on");
  } else if(!strncmp(buffer,"J02 ",4)) {
    // jog
    float xx=0;
    float yy=0;
    float zz=posz;

    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1)*mode_scale;  break;
      case 'Y': yy=atof(ptr+1)*mode_scale;  break;
      case 'Z': zz=atof(ptr+1);  break;
      case 'F': setFeedRate(atof(ptr+1));  break;
      default: ptr=0; break;
      }
    }

    setPenAngle(zz);
    jog(xx,yy);
  } else {
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      if(!strncmp(ptr,"G20",3)) {
        mode_scale=0.393700787;  // inches -> cm
        strcpy(mode_name,"in");
        Serial.println("scale: inches.");
      } else if(!strncmp(ptr,"G21",3)) {
        mode_scale=0.1;  // mm -> cm
        strcpy(mode_name,"mm");
        Serial.println("scale: millimeters.");
      } else if(!strncmp(ptr,"G90",3)) {
        absolute_mode=1;
      } else if(!strncmp(ptr,"G91",3)) {
        absolute_mode=0;
      } else if(ptr) {
        if(strlen(ptr)>0) {
          Serial.print("Invalid command: '");
          Serial.print(ptr);
          Serial.println("'");
        }
        break;
      }
      ptr=strchr(ptr,' ')+1;
    }
  }
}


//------------------------------------------------------------------------------
void setup() {
  // start communications
  Serial.begin(BAUD);
  Serial.println("\n\n== HELLO WORLD ==");

  // initialize the scale
  strcpy(mode_name,"mm");
  mode_scale=0.1;
  
  // load the EEPROM values
  loadConfig();
  printConfig();
  
  // initialize the read buffer
  sofar=0;
  
  // servo should be on SER1, pin 10.
  s1.attach(SERVO_PIN);

  // turn on the pull up resistor
  digitalWrite(S1_PIN,HIGH);
  digitalWrite(S2_PIN,HIGH);
  
  // load string onto spool.  Only needed when the robot is being built.
//  loadspools();
  
  // display the help at startup.
  help();

  // initialize the plotter position.
  teleport(0,0);
  velx=accelx=0;
  velx=accelx=0;
  posz=PEN_UP_ANGLE;
  setPenAngle(PEN_UP_ANGLE);
  
  // start the timer interrupt
  planner_setup();

  Serial.print("> ");
}


//------------------------------------------------------------------------------
void loop() {
  // See: http://www.marginallyclever.com/2011/10/controlling-your-arduino-through-the-serial-monitor/
  // listen for serial commands
  while(Serial.available() > 0) {
    buffer[sofar++]=Serial.read();
    if(buffer[sofar-1]==';') break;  // in case there are multiple instructions
  }
 
  // if we hit a semi-colon, assume end of instruction.
  if(sofar>0 && buffer[sofar-1]==';') {
    // what if message fails/garbled?

    // echo confirmation
    buffer[sofar]=0;
//    Serial.println(buffer);
 
    // do something with the command
    processCommand();
 
    // reset the buffer
    sofar=0;
 
    // echo completion
    Serial.print("> ");
  }
}



//------------------------------------------------------------------------------
// Copyright (C) 2012 Dan Royer (dan@marginallyclever.com)
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//------------------------------------------------------------------------------

