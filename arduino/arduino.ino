//------------------------------------------------------------------------------
// Draw robot
// dan@marginallycelver.com 2012 feb 11
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/i-make-robots/Drawbot for more information.


//------------------------------------------------------------------------------
// PARTS
//------------------------------------------------------------------------------
// 1 - Arduino UNO, Duemilanove, or MEGA 2560
// 1 - Adafruit motor shield
// 2 - NEMA17 stepper motors
// 2 - sewing machine bobbins
// 1 - 5v2a power supply
// 1 - binder clip to hold plotter
// Stepper motors M1 and M2 should be mounted parallel.
// M1 is expected to be on the left.



//------------------------------------------------------------------------------
// COORDINATE SYSTEM:
//------------------------------------------------------------------------------
// (0,0) is center of drawing surface.  -y is up.  -x is left.
// All lengths are in cm unless otherwise stated.



//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
// Adafruit motor driver library
#include <AFMotor.h>

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

// Uncomment this line to compile for smaller boards
//#define SMALL_FOOTPRINT (1)

// which motor is on which pin?
#define M1_PIN          (1)
#define M2_PIN          (2)

// which way are the spools wound, relative to motor movement?
#define REEL_IN         FORWARD
#define REEL_OUT        BACKWARD

// NEMA17 are 200 steps (1.8 degrees) per turn.  If a spool is 0.8 diameter
// then it is 2.5132741228718345 circumference, and
// 2.5132741228718345 / 200 = 0.0125663706 thread moved each step.
// NEMA17 are rated up to 3000RPM.  Adafruit can handle >1000RPM.
// These numbers directly affect the maximum velocity.
#define STEPS_PER_TURN  (200.0)
#define SPOOL_DIAMETER  (0.85)
#define MAX_RATED_RPM   (3000.0)
#define MAX_RPM         (300.0)
#define DEFAULT_RPM     (75.0)

// how fast can the plotter accelerate?
#define ACCELERATION    (5.00)  // cm/s/s


// *****************************************************************************
// *** Don't change the constants below unless you know what you're doing.   ***
// *****************************************************************************

// The step mode controls how the Adafruit driver moves a stepper.
// options are SINGLE, DOUBLE, INTERLEAVE, and MICROSTEP.
#define STEP_MODE       SINGLE

// servo angles for pen control
#define PEN_UP_ANGLE    (90)
#define PEN_DOWN_ANGLE  (10)  // Some steppers don't like 0 degrees
#define PEN_DELAY       (150)  // in ms

// calculate some numbers to help us find feed_rate
#define SPOOL_CIRC      (SPOOL_DIAMETER*PI)  // circumference
#define THREADPERSTEP   (SPOOL_CIRC/STEPS_PER_TURN)  // thread per step

// Speed of the timer interrupt
#define STEPS_S         (STEPS_PER_TURN*MAX_RPM/60)  // steps/s
#define TIMER_FREQUENCY (1000000.0/(float)STEPS_S)  // microseconds/step

// The interrupt makes calls to move the stepper.
// the maximum speed cannot be faster than the timer interrupt.
#define MAX_VEL         (STEPS_S * THREADPERSTEP)  // cm/s
#define MIN_VEL         (0.001) // cm/s
#define DEFAULT_VEL     (MAX_VEL/2)  // cm/s



// for arc directions
#define ARC_CW          (1)
#define ARC_CCW         (-1)
// Arcs are split into many line segments.  How long are the segments?
#define CM_PER_SEGMENT   (0.1)

// Serial communication bitrate
#define BAUD            (9600)
// Maximum length of serial input message.
#define MAX_BUF         (64)

// look-ahead planning
#define MAX_BLOCKS       (16)
#define NEXT_BLOCK(x)    ((x+1)%MAX_BLOCKS)
#define PREV_BLOCK(x)    ((x+MAX_BLOCKS-1)%MAX_BLOCKS)



//------------------------------------------------------------------------------
// STRUCTURES
//------------------------------------------------------------------------------

// based on https://github.com/grbl/grbl/ planner
typedef struct {
  double sx, sy;
  double ex, ey;
  double t1, t2, time, tsum, tstart;
  double startv,endv,topv,maxstartv;
  double len;
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
static double limit_top = 21.5;  // distance to top of drawing area.
static double limit_bottom =-30.0;  // Distance to bottom of drawing area.
static double limit_right = 14.0;  // Distance to right of drawing area.
static double limit_left =-14.0;  // Distance to left of drawing area.

// plotter position.
static double posx, velx, accelx;
static double posy, vely, accely;
static double posz;  // pen state

// motor position
static volatile long laststep1, laststep2;

// speeds, feeds, and delta-vs.
static double accel=ACCELERATION;
static double feed_rate=DEFAULT_VEL;

static char absolute_mode=1;  // absolute or incremental programming mode?
static double mode_scale=1;   // mm or inches?

// time values
static long  t_millis;
static double t;   // since board power on
static double dt;  // since last tick

// Diameter of line made by plotter
static double tool_diameter=0.05;

// Serial comm reception
static char buffer[MAX_BUF];  // Serial buffer
static int sofar;             // Serial buffer progress

// look-ahead ring buffer
static block blocks[MAX_BLOCKS];
static volatile int block_head, block_tail;
static block *current_block=NULL;
static double previous_topv;

// timer interrupt blocking
static char planner_busy=0;
static char planner_awake=0;


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------



//------------------------------------------------------------------------------
void setup_planner() {
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
static void error(int r) {
  if(r!=0) {
    Serial.print("Error: code ");
    Serial.println(r);
  }
}


//------------------------------------------------------------------------------
// increment internal clock
static void tick() {
  long nt_millis=millis();
  long dt_millis=nt_millis-t_millis;

  t_millis=nt_millis;

  dt=(double)dt_millis*0.001;  // time since last tick, in seconds
  t=(double)nt_millis*0.001;
}


//------------------------------------------------------------------------------
static void setFeedRate(double v) {
  feed_rate=v;
  if(feed_rate>MAX_VEL) feed_rate=MAX_VEL;
  if(feed_rate<MIN_VEL) feed_rate=MIN_VEL;
  Serial.print("F=");
  Serial.println(feed_rate);
}


//------------------------------------------------------------------------------
// returns angle of dy/dx as a value from 0...2PI
static double atan3(double dy,double dx) {
  double a=atan2(dy,dx);
  if(a<0) a=(PI*2.0)+a;
  return a;
}


//------------------------------------------------------------------------------
// Change pen state.
static void pen(double pen_angle) {
  posz=pen_angle;
  if(pen_angle<PEN_DOWN_ANGLE) posz=PEN_DOWN_ANGLE;
  if(pen_angle>PEN_UP_ANGLE  ) posz=PEN_UP_ANGLE;
  s1.write(posz);
}


//------------------------------------------------------------------------------
// Inverse Kinematics - turns XY coordinates into lengths L1,L2
static void IK(double x, double y, long &l1, long &l2) {
  // find length to M1
  double dy = y - limit_top;
  double dx = x - limit_left;
  l1 = floor( sqrt(dx*dx+dy*dy) / THREADPERSTEP );
  // find length to M2
  dx = limit_right - x;
  l2 = floor( sqrt(dx*dx+dy*dy) / THREADPERSTEP );
}


//------------------------------------------------------------------------------
// Forward Kinematics - turns L1,L2 lengths into XY coordinates
// use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
// to find angle between M1M2 and M1P where P is the plotter position.
static void FK(double l1, double l2,double &x,double &y) {
  double a = l1 * THREADPERSTEP;
  double b = (limit_right-limit_left);
  double c = l2 * THREADPERSTEP;
  
  // slow, uses trig
  double theta = acos((a*a+b*b-c*c)/(2.0*a*b));
  x = cos(theta)*l1 + limit_left;
  y = sin(theta)*l1 + limit_top;
}


//------------------------------------------------------------------------------
static void travelTime(double len,double v1,double v3,double v2,double &t1,double &t2,double &t3) {
  t1 = (v2-v1) / accel;
  t2 = (v2-v3) / accel;
  double d1 = v1 * t1 + 0.5 * accel * t1*t1;
  double d2 = v3 * t2 + 0.5 * accel * t2*t2;
  double a = d1+d2;
  
  if(len>a) {
    t3 = t1+t2 + (len-a) / v2;
    t2 = t3-t2;
  } else {
    // http://wikipedia.org/wiki/Classical_mechanics#1-Dimensional_Kinematics
    double brake_distance=(2.0*accel*len-v1*v1+v3*v3) / (4.0*accel);
    // and we also know d=v0*t + att/2
    // so 
    t1 =      ( -v1 + sqrt( 2.0*accel*brake_distance       + v1*v1 ) ) / accel;
    t2 = t1;
    t3 = t1 + ( -v3 + sqrt( 2.0*accel*(len-brake_distance) + v3*v3 ) ) / accel;
  }
/*
  Serial.print("\tlen=");  Serial.print(len);
  Serial.print("\taccel=");  Serial.print(accel);
  Serial.print("\tv1=");  Serial.print(v1);
  Serial.print("\tv3=");  Serial.print(v3);
  Serial.print("\tt1=");  Serial.print(t1);
  Serial.print("\tt2=");  Serial.print(t2);
  Serial.print("\tt3=");  Serial.println(t3);
*/
}


//------------------------------------------------------------------------------
static double interpolate(double p0,double p3,double t,double t1,double t2,double t3,double len,double v0,double v3,double v2) {
  if(t<=0) return p0;
 
  double s = (p3-p0)/len;
  
  if(t<t1) {
    double d1 = v0 * t + 0.5 * accel * t * t;
    return p0 + (d1)*s;
  } else if(t<t2) {
    double d1 = v0 * t1 + 0.5 * accel * t1 * t1;
    double d2 = v2 * (t-t1);
    return p0 + (d1+d2)*s;
  } else if(t<t3) {
    double d1 = v0 * t1 + 0.5 * accel * t1 * t1;
    double d2 = v2 * (t2-t1);

    double t4 = t-t2;
    //double v2 = accel*t1;
    double d3 = v2 * t4 - 0.5 * accel * t4 * t4;
    return p0 + (d1+d2+d3)*s;
  }
  
  return p3;
}


//------------------------------------------------------------------------------
// Turns the motors so that the real robot strings match our simulation.
// len1 & len2 are the current string lengths.
// nlen1 & nlen2 are the new string lengths.
static void adjustStringLengths(long nlen1,long nlen2) {
  // is the change in length >= one step?
  long steps=nlen1-laststep1;
  if(steps<0)      m1.onestep(REEL_IN ,STEP_MODE);  // it is shorter.
  else if(steps>0) m1.onestep(REEL_OUT,STEP_MODE);  // it is longer.
//  if(steps<0)      m1.step(-steps,REEL_IN ,STEP_MODE);  // it is shorter.
//  else if(steps>0) m1.step( steps,REEL_OUT,STEP_MODE);  // it is longer.
  
  // is the change in length >= one step?
  steps=nlen2-laststep2;
  if(steps<0)      m2.onestep(REEL_IN ,STEP_MODE);  // it is shorter.
  else if(steps>0) m2.onestep(REEL_OUT,STEP_MODE);  // it is longer.
//  if(steps<0)      m2.step(-steps,REEL_IN ,STEP_MODE);  // it is shorter.
//  else if(steps>0) m2.step( steps,REEL_OUT,STEP_MODE);  // it is longer.
  
  laststep1=nlen1;
  laststep2=nlen2;
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
  double vtotal = sqrt(velx*velx+vely*vely);
  if(vtotal>feed_rate) {
    double scale = feed_rate/vtotal;
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
static void jog(double x,double y) {
  double ax=x*accel;
  double ay=y*accel;

  double atotal = sqrt(ax*ax+ay*ay);
  if(atotal>accel) {
    double scale = accel/atotal;
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

  tick();
  
  if(current_block==NULL) {
    if(block_tail!=block_head) {
//      Serial.print("s");
//      Serial.println(block_tail);
      current_block=&blocks[block_tail];
      current_block->tstart=t;
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

    // find where the plotter will be at tsum seconds
    double nx = interpolate(current_block->sx,current_block->ex,current_block->tsum,current_block->t1,current_block->t2,current_block->time,current_block->len,current_block->startv,current_block->endv,current_block->topv);
    double ny = interpolate(current_block->sy,current_block->ey,current_block->tsum,current_block->t1,current_block->t2,current_block->time,current_block->len,current_block->startv,current_block->endv,current_block->topv);

    // get the new string lengths
    long nlen1,nlen2;
    IK(nx,ny,nlen1,nlen2);

    // move the motors
    adjustStringLengths(nlen1,nlen2);

    // is this block finished?    
    if(current_block->tsum >= current_block->time) {
//      Serial.print("e");
//      Serial.println(block_tail);
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
// if you must reach target_vel within distance and you speed up by acceleration
// then what must your minimum velocity be?
static double max_allowable_speed(double acceleration,double target_vel,double distance) {
  return sqrt( target_vel * target_vel - 2 * acceleration * distance );
}


//------------------------------------------------------------------------------
// 
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
      curr->startv = min(curr->maxstartv,
                        max_allowable_speed( -accel, next->startv, curr->len ) );
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
      double startv = min(curr->startv, max_allowable_speed( -accel, prev->startv, prev->len ) );
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
static void line(double x,double y) {
/*
  Serial.print("A");
  Serial.print(block_tail);
  Serial.print(" ");
  Serial.print(block_head);
  
  Serial.print("x ");
  Serial.println(x);
  Serial.print("y ");
  Serial.println(y);

  if(current_block) {
    Serial.print(" ");
    Serial.print(current_block->tsum);
    Serial.print(" ");
    Serial.print(current_block->time);
  }
  Serial.println(planner_busy?" busy":" idle");
*/

  int next_head=NEXT_BLOCK(block_head);
  // while there is no room in the queue, wait.
  while(block_tail==next_head) sleep_mode();
    
  // set up the new block in the queue.
  double dx=x-posx;
  double dy=y-posy;
/*
  Serial.print("posx ");
  Serial.println(posx);
  Serial.print("posy ");
  Serial.println(posy);
  
  Serial.print("dx ");
  Serial.println(dx);
  Serial.print("dy ");
  Serial.println(dy);
*/  
  block *new_block=&blocks[block_head];
  new_block->sx=posx;
  new_block->sy=posy;
  new_block->ex=x;
  new_block->ey=y;
  new_block->startv=0;
  new_block->endv=0;
  new_block->topv=feed_rate;
  new_block->len = sqrt(dx*dx+dy*dy);
  new_block->tsum=0;

  if(new_block->len==0) return;
  
  // Find the maximum speed around the corner from the previous line to this line.
  double maxjunctionv = 0.0;

  block *curr,*next;
  if( block_head != block_tail ) {
    // there is a previous line to compare to
    next = new_block;
    curr=&blocks[PREV_BLOCK(block_head)];

    // dot product the two vectors to get the cos(theta) of their angle.
    double x1 = ( curr->ex - curr->sx ) / curr->len;
    double y1 = ( curr->ey - curr->sy ) / curr->len;
    double x2 = ( next->ex - next->sx ) / next->len;
    double y2 = ( next->ey - next->sy ) / next->len;
    double dotproduct = x1*x2+y1*y2;

    // Close enought to straight (>=0.95) we don't slow down.
    // Anything more than 90 is a full stop guaranteed.
    if( dotproduct >=0.95 ) {
      maxjunctionv = min( previous_topv, curr->topv );
    } else if( dotproduct > 0 ) {
      maxjunctionv = min( curr->topv * dotproduct, next->topv * dotproduct );
    }
  }
  new_block->maxstartv = maxjunctionv;
  
  double allowablev = max_allowable_speed(-accel,0,new_block->len);

  new_block->startv = min( maxjunctionv, allowablev );
  // no matter at what speed we start, is this line long enough to reach top speed?
  new_block->nominal_length = ( new_block->topv < allowablev );

//  Serial.print("maxstartv=");   Serial.println(maxjunctionv);
//  Serial.print("allowablev=");  Serial.println(allowablev);
//  Serial.print("startv=");      Serial.println(new_block->startv);

  // make sure the trapezoid is calculated for this block
  new_block->touched = 1;
  
/*
  Serial.print("B");
  Serial.print(block_tail);
  Serial.print(" ");
  Serial.println(next_head);
*/
  // the line is now ready to be queued.
  // move the head
  block_head=next_head;

  posx=x;
  posy=y;
  previous_topv=new_block->topv;
  
  planner_recalculate();
  planner_wakeup();
}


//------------------------------------------------------------------------------
// checks against the robot limits before attempting to move
static int lineSafe(double x,double y) {
#ifndef SMALL_FOOTPRINT
  if(outsideLimits(x,y)) return 1;
#endif
  
  line(x,y);
  return 0;
}


//------------------------------------------------------------------------------
// This method assumes the limits have already been checked.
// This method assumes the start and end radius match.
// This method assumes arcs are not >180 degrees (PI radians)
// cx/cy - center of circle
// x/y - end position
// dir - ARC_CW or ARC_CCW to control direction of arc
static void arc(double cx,double cy,double x,double y,double dir) {
  // get radius
  double dx = posx - cx;
  double dy = posy - cy;
  double radius=sqrt(dx*dx+dy*dy);

  // find angle of arc (sweep)
  double angle1=atan3(dy,dx);
  double angle2=atan3(y-cy,x-cx);
  double theta=angle2-angle1;
  
  if(dir>0 && theta<0) angle2+=2*PI;
  else if(dir<0 && theta>0) angle1+=2*PI;
  
  theta=angle2-angle1;
  
  // get length of arc
  // double circ=PI*2.0*radius;
  // double len=theta*circ/(PI*2.0);
  // simplifies to
  double len = abs(theta) * radius;

  int segments = floor( len / CM_PER_SEGMENT );
 
  double nx, ny, angle3, scale;
/*
  Serial.print("cx ");
  Serial.println(cx);
  Serial.print("cy ");
  Serial.println(cy);
    
  Serial.print("a1 ");
  Serial.println(angle1);
  Serial.print("a2 ");
  Serial.println(angle2);

  Serial.print("radius ");
  Serial.println(radius);
  Serial.print("segments ");
  Serial.println(segments);
*/
  for(int i=0;i<segments;++i) {
    // interpolate around the arc
    scale = ((double)i)/((double)segments);
    
    angle3 = ( theta * scale ) + angle1;
    nx = cx + cos(angle3) * radius;
    ny = cy + sin(angle3) * radius;
/*    
    Serial.print("s ");
    Serial.println(scale);
    Serial.print("a3 ");
    Serial.println(angle3);
    Serial.print("nx ");
    Serial.println(nx);
    Serial.print("ny ");
    Serial.println(ny);
*/
    // send it to the planner
    line(nx,ny);
  }
  
  line(x,y);
}



#ifndef SMALL_FOOTPRINT


//------------------------------------------------------------------------------
// returns 0 if inside limits
// returns non-zero if outside limits.
static int outsideLimits(double x,double y) {
  return ((x<limit_left)<<0)
        |((x>limit_right)<<1)
        |((y<limit_bottom)<<2)
        |((y>limit_top)<<3);
}


//------------------------------------------------------------------------------
// is the point (dx,dy) in the arc segment subtended by angle1,angle2?
// return 0 if it is not.
static int pointInArc(double dx,double dy,double angle1,double angle2,double dir) {
  double angle3=atan3(-dy,dx);  // atan2 expects +y to be up, so flip the sign
  
#ifdef VERBOSE 
  Serial.print("C:");  Serial.print(dx);
  Serial.print(",");   Serial.print(dy);
  Serial.print("=");   Serial.println(angle3*180.0/PI);
#endif

  if(dir==ARC_CW) {
    if(angle1<angle2) angle1+=PI*2;
#ifdef VERBOSE 
  Serial.print("CW");
  Serial.print(angle1*180.0/PI);  Serial.print(" < ");
  Serial.print(angle3*180.0/PI);  Serial.print(" < ");
  Serial.print(angle2*180.0/PI);  Serial.println("?");
#endif
    if(angle2<=angle3 && angle3<=angle1) return 1;
  } else {
    if(angle2<angle1) angle2+=PI*2;
#ifdef VERBOSE 
  Serial.print("CCW");
  Serial.print(angle2*180.0/PI);  Serial.print(" > ");
  Serial.print(angle3*180.0/PI);  Serial.print(" > ");
  Serial.print(angle1*180.0/PI);  Serial.println("?");
#endif
    if(angle1<=angle3 && angle3<=angle2) return 2;
  }

  return 0;
}


//------------------------------------------------------------------------------
// ...checks start & end radius match
// ...checks against the envelope limits
static int canArc(double cx,double cy,double x,double y,double dir) {
  if(outsideLimits(x,y)) return 1;

  double a=x-cx;
  double b=y-cy;
  double c=posx-cx;
  double d=posy-cy;
  double r1=sqrt(a*a+b*b);
  double r2=sqrt(c*c+d*d);
  
  if( abs(r1-r2) > 0.001 ) {
    Serial.print("r1=");  Serial.println(r1);
    Serial.print("r2=");  Serial.println(r2);
    return 2;  // radii don't match
  }
  
  double angle1=atan3(d,c);
  double angle2=atan3(b,a);

#ifdef VERBOSE
  Serial.print("A:");  Serial.print(c);
  Serial.print(",");   Serial.print(d);
  Serial.print("=");   Serial.println(angle1*180.0/PI);
  Serial.print("B:");  Serial.print(a);
  Serial.print(",");   Serial.print(b);
  Serial.print("=");   Serial.println(angle2*180.0/PI);
  Serial.print("r=");  Serial.println(r1);
#endif

  if(cx+r1>limit_right) {
    // find the two points of intersection, see if they are inside the arc
    double dx=limit_right-cx;
    double dy=sqrt(r1*r1-dx*dx);
    if(pointInArc(dx, dy,angle1,angle2,dir)) return 3;
    if(pointInArc(dx,-dy,angle1,angle2,dir)) return 4;
  }
  if(cx-r1<limit_left) {
    // find the two points of intersection, see if they are inside the arc
    double dx=limit_left-cx;
    double dy=sqrt(r1*r1-dx*dx);
    if(pointInArc(dx, dy,angle1,angle2,dir)) return 5;
    if(pointInArc(dx,-dy,angle1,angle2,dir)) return 6;
  }
  if(cy+r1>limit_top) {
    // find the two points of intersection, see if they are inside the arc
    double dy=limit_top-cy;
    double dx=sqrt(r1*r1-dy*dy);
    if(pointInArc( dx,dy,angle1,angle2,dir)) return 7;
    if(pointInArc(-dx,dy,angle1,angle2,dir)) return 8;
  }
  if(cy-r1<limit_bottom) {
    // find the two points of intersection, see if they are inside the arc
    double dy=limit_bottom-cy;
    double dx=sqrt(r1*r1-dy*dy);
    if(pointInArc( dx,dy,angle1,angle2,dir)) return 9;
    if(pointInArc(-dx,dy,angle1,angle2,dir)) return 10;
  }

  return 0;
}



#endif  // SMALL_FOOTPRINT



//------------------------------------------------------------------------------
// before attempting to move...
// ...checks start & end radius match
// ...checks against the envelope limits
static int arcSafe(double cx,double cy,double x,double y,double dir) {
#ifndef SMALL_FOOTPRINT
  int r=canArc(cx,cy,x,y,dir);
  if(r!=0) return r;
#endif
  
  arc(cx,cy,x,y,dir);
  return 0;
}



#ifndef SMALL_FOOTPRINT



//------------------------------------------------------------------------------
static void testArcs() {
  int r;
  double x,y;
  
  Serial.println(atan3( 1, 1)*180.0/PI);
  Serial.println(atan3( 1,-1)*180.0/PI);
  Serial.println(atan3(-1,-1)*180.0/PI);
  Serial.println(atan3(-1, 1)*180.0/PI);
  
  x=limit_right*0.75;
  y=limit_top*0.50;

  Serial.println("arcs inside limits, center inside limits (should pass)");    
  teleport(x,0);
  error(canArc(0,0,-x,0,ARC_CCW));
  error(canArc(0,0, x,0,ARC_CCW));
  error(canArc(0,0,-x,0,ARC_CW));
  error(canArc(0,0, x,0,ARC_CW));

  Serial.println("arcs outside limits, center inside limits (should fail)");
  x=x*5;
  teleport(x,0);
  error(canArc(0,0,-x,0,ARC_CCW));
  error(canArc(0,0, x,0,ARC_CCW));
  error(canArc(0,0,-x,0,ARC_CW));
  error(canArc(0,0, x,0,ARC_CW));

  x=limit_right*0.75;
  y=limit_top*0.50;
  Serial.println("arcs outside limits, arc center outside limits (should fail)");
  teleport(x,y);
  error(canArc(x,0,-x,y,ARC_CW));

  // limit_right boundary test
  x=limit_right*0.75;
  y=limit_top*0.50;
  Serial.println("CCW through limit_right (should fail)");     teleport(x, y);  error(canArc(x,0, x,-y, ARC_CCW));
  Serial.println("CW avoids limit_right (should pass)");       teleport(x, y);  error(canArc(x,0, x,-y, ARC_CW));
  Serial.println("CW through limit_right (should fail)");      teleport(x,-y);  error(canArc(x,0, x, y, ARC_CW));
  Serial.println("CCW avoids limit_right (should pass)");      teleport(x,-y);  error(canArc(x,0, x, y, ARC_CCW));
  // limit_left boundary test
  x=limit_left*0.75;
  y=limit_top*0.50;
  Serial.println("CW through limit_left (should fail)");       teleport(x, y);  error(canArc(x,0, x,-y, ARC_CW));
  Serial.println("CCW avoids limit_left (should pass)");       teleport(x, y);  error(canArc(x,0, x,-y, ARC_CCW));
  Serial.println("CCW through limit_left (should fail)");      teleport(x,-y);  error(canArc(x,0, x, y, ARC_CCW));
  Serial.println("CW avoids limit_left (should pass)");        teleport(x,-y);  error(canArc(x,0, x, y, ARC_CW));
  // limit_bottom boundary test
  x=limit_right*0.50;
  y=limit_bottom*0.75;
  Serial.println("CW through limit_bottom (should fail)");     teleport( x,y);  error(canArc(0,y,-x, y, ARC_CW));
  Serial.println("CCW avoids limit_bottom (should pass)");     teleport( x,y);  error(canArc(0,y,-x, y, ARC_CCW));
  Serial.println("CCW through limit_bottom (should fail)");    teleport(-x,y);  error(canArc(0,y, x, y, ARC_CCW));
  Serial.println("CW avoids limit_bottom (should pass)");      teleport(-x,y);  error(canArc(0,y, x, y, ARC_CW));
  // limit_top boundary test
  x=limit_right*0.50;
  y=limit_top*0.75;
  Serial.println("CCW through limit_top (should fail)");       teleport( x,y);  error(canArc(0,y,-x, y, ARC_CCW));
  Serial.println("CW avoids limit_top (should pass)");         teleport( x,y);  error(canArc(0,y,-x, y, ARC_CW));
  Serial.println("CW through limit_top (should fail)");        teleport(-x,y);  error(canArc(0,y, x, y, ARC_CW));
  Serial.println("CCW avoids limit_top (should pass)");        teleport(-x,y);  error(canArc(0,y, x, y, ARC_CCW));
}


//------------------------------------------------------------------------------
static void testClock() {
  for(int i=0;i<100;++i) {
    tick();
    Serial.print(t_millis);    Serial.print("\t");
    Serial.print(t);           Serial.print("\t");
    Serial.print(dt);          Serial.print("\n");
    delay(25);
  }
}


//------------------------------------------------------------------------------
static void testKinematics(double x,double y) {
  Serial.println("-- TEST KINEMATICS --");
  teleport(x,y);
  double xx,yy;
  long a,b;
  IK(posx,posy,a,b);
  FK(a,b,xx,yy);
  Serial.print("x=");  Serial.print(x);
  Serial.print("\ty=");  Serial.print(y);
  Serial.print("\ta=");  Serial.print(a);
  Serial.print("\tb=");  Serial.print(b);
  Serial.print("\txx=");  Serial.print(xx);
  Serial.print("\tyy=");  Serial.println(yy);
}


//------------------------------------------------------------------------------
static void testFullCircle() {
  Serial.println("-- TEST FULL CIRCLE --");

  int i;
  long a,b;
  
  a=micros();
  for(i=0;i<STEPS_PER_TURN;++i) {
    m1.step(1,REEL_OUT,STEP_MODE);  // reel out
  }
  for(i=0;i<STEPS_PER_TURN;++i) {
    m2.step(1,REEL_OUT,STEP_MODE);  // reel out
  }
  b=micros();
  Serial.println((b-a)/STEPS_PER_TURN);

  a=micros();
  for(i=0;i<STEPS_PER_TURN;++i) {
    m1.step(1,REEL_IN,STEP_MODE);  // reel out
  }
  for(i=0;i<STEPS_PER_TURN;++i) {
    m2.step(1,REEL_IN,STEP_MODE);  // reel out
  }
  b=micros();
  Serial.println((b-a)/STEPS_PER_TURN);
  
  a=micros();
  for(i=0;i<STEPS_PER_TURN;++i) {
    m1.step(1,REEL_OUT,STEP_MODE);  // reel out
    m2.step(1,REEL_OUT,STEP_MODE);  // reel out
  }
  b=micros();
  Serial.println((b-a)/STEPS_PER_TURN);
  a=micros();
  for(i=0;i<STEPS_PER_TURN/2;++i) {
    m1.step(2,REEL_IN,STEP_MODE);   // reel in
    m2.step(2,REEL_IN,STEP_MODE);   // reel in
  }
  b=micros();
  Serial.println((b-a)/STEPS_PER_TURN);
}


//------------------------------------------------------------------------------
static void testInterpolation() {
  Serial.println("-- TEST INTERPOLATE2 --");
  double start=0;
  double end=1;
  double t1,t2,t3;
  double oldv=0,v;
  
  Serial.println("dist=1, full stop");
  travelTime(end-start,0,0,feed_rate,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(double t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start,0,0,feed_rate);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }

  oldv=0;
  Serial.println("dist=5, full stop");
  end=5;
  travelTime(end-start,0,0,feed_rate,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(double t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start,0,0,feed_rate);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }

  oldv=0;
  Serial.println("dist=15, full stop");
  end=15;
  travelTime(end-start,0,0,feed_rate,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(double t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start,0,0,feed_rate);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }

  oldv=0;
  Serial.println("dist=5, keep going");
  end=5;
  travelTime(end-start,0,feed_rate,feed_rate,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(double t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start,0,0,feed_rate);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }

  oldv=0;
  Serial.println("dist=5, slow to stop");
  end=5;
  travelTime(end-start,feed_rate,0,feed_rate,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(double t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start,0,0,feed_rate);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }
}


//------------------------------------------------------------------------------
static void testAcceleration() {
  Serial.println("-- TEST ACCELERATION --");
  Serial.print("feed_rate=");  Serial.println(feed_rate);

  double i;
  double a=10;
  double b=0;
  double c;
  for(i=3;i<feed_rate;i+=0.5) {
    delay(2000);
    accel=i;
    Serial.println(accel);
    line(a,0);
    c=b;
    b=a;
    a=c;
  }
}


//------------------------------------------------------------------------------
static void testfeed_rate() {
  Serial.println("-- TEST MAX VELOCITY --");
  Serial.print("ACCEL=");  Serial.println(ACCELERATION);
  Serial.print("MAX_VEL=");  Serial.println(MAX_VEL);

  double i;
  double a=10;
  double b=-10;
  double c;

  line(b,0);

  for(i=5;i<MAX_VEL;i+=1) {
    delay(2000);
    setFeedRate(i);
    Serial.println(feed_rate);
    line(a,0);
    c=b;
    b=a;
    a=c;
  }
  line(0,0);
}


//------------------------------------------------------------------------------
// loads 5m onto a spool.
static void loadspools() {
  double len=500.0;
  double amnt=len/THREADPERSTEP;
  Serial.print("== LOAD ");
  Serial.print(len);
  Serial.print(" ==");
  // uncomment the motor you want to load
  m1.step(amnt,REEL_IN);
  //m2.step(amnt,REEL_IN);
}


//------------------------------------------------------------------------------
// Show off line and arc movement.  This is the test pattern.
static void demo() {
  setFeedRate(1.0);
  
  // square
  Serial.println("> L 0,-2");              line( 0,-2);
  Serial.println("> L-2,-2");              line(-2,-2);
  Serial.println("> L-2, 2");              line(-2, 2);
  Serial.println("> L 2, 2");              line( 2, 2);
  Serial.println("> L 2,-2");              line( 2,-2);
  Serial.println("> L 0,-2");              line( 0,-2);
  // arc
  Serial.println("> A 0,-4,0,-6,ARC_CW");  arc(0,-4,0,-6,ARC_CW);
  Serial.println("> A 0,-4,0,-2,ARC_CCW"); arc(0,-4,0,-2,ARC_CCW);
  // square
  Serial.println("> L 0,-4");              line( 0,-4);
  Serial.println("> L 4,-4");              line( 4,-4);
  Serial.println("> L 4, 4");              line( 4, 4);
  Serial.println("> L-4, 4");              line(-4, 4);
  Serial.println("> L-4,-4");              line(-4,-4);
  Serial.println("> L 0,-4");              line( 0,-4);
  // square
  Serial.println("> L 0,-6");              line( 0,-6);
  Serial.println("> L-6,-6");              line(-6,-6);
  Serial.println("> L-6, 6");              line(-6, 6);
  Serial.println("> L 6, 6");              line( 6, 6);
  Serial.println("> L 6,-6");              line( 6,-6);
  Serial.println("> L 0,-6");              line( 0,-6);
  // large circle
  Serial.println("> A 0,0,0, 6,ARC_CW");   arc(0,0, 0, 6,ARC_CW);
  Serial.println("> A 0,0,0,-6,ARC_CW");   arc(0,0, 0,-6,ARC_CW);

  // triangle
  Serial.println("> L  5.196, 3");         line( 5.196, 3);
  Serial.println("> L -5.196, 3");         line(-5.196, 3);
  Serial.println("> L 0,-6");              line( 0,-6);

  // halftones
  Serial.println("> L -6,-6");             line(-6,-6);
  Serial.println("> L -6, 8");             line(-6, 8);

  int i;
  for(i=0;i<12;++i) {
    Serial.print("> H 1, ");
    Serial.println((double)i/11.0);
    halftone(1,(double)i/11.0);
  }

  // return to origin
  //Serial.println("> L 6, 6");              line( 6, 6);
  Serial.println("> CENTER");              line( 0, 0);
}


//------------------------------------------------------------------------------
// instantly move the virtual plotter position
// does not validate if the move is valid
static void teleport(double x,double y) {
  posx=x;
  posy=y;
  
  // @TODO: posz?
  long L1,L2;
  IK(posx,posy,L1,L2);
  laststep1=L1;
  laststep2=L2;

  where();
}


//------------------------------------------------------------------------------
// instantly move the virtual plotter position
// checks against the robot limits before attempting to move
static int teleportSafe(double x,double y) {
  if(outsideLimits(x,y)) return 1;
  
  teleport(x,y);
  return 0;
}


//------------------------------------------------------------------------------
// halftone generator
// This method assumes the limits have already been checked.
// This method assumes posx, posy is in the middle left of the halftone area
// size - width and height of area to fill.
// fill - [0...1], 0 being least fill and 1 being most fill
//
// plotter will travel left to right filling area from (x1,y1-size/2) to
// (x1+size,y1+size/2) with zigzags.
static void halftone(double size,double fill) {
  double ymin=posy-(size*0.5);
  double ymax=posy+(size*0.5);
  
  double max_lines = size / tool_diameter;
  int infill = floor( max_lines * fill );

#ifdef VERBOSE
  Serial.print("size=");        Serial.println(size);
  Serial.print("fill=");        Serial.println(fill);
  Serial.print("max_lines=");   Serial.println(max_lines);
  Serial.print("infill=");      Serial.println(infill);
#endif

  // Save starting location because line() changes posx,posy!
  double ox=posx;
  double oy=posy;
  
  if(infill>1) {
    double step = size / (double)(infill);
#ifdef VERBOSE
    Serial.print("step=");          Serial.println(step);
#endif
    
    double x2=ox;
    double y2=oy;
    
    for( int i=0; i<infill; ++i ) {
      x2 += step;
      y2 = (i%2)? ymin : ymax;  // the zig-zag effect
      line(x2,y2);  
    }
  }

  // return to the middle of the other side of the halftone square
  line(ox+size,oy);
}


//------------------------------------------------------------------------------
static void help() {
  Serial.println("== DRAWBOT - 2012 Feb 28 - dan@marginallyclever.com ==");
  Serial.println("All commands end with a semi-colon.");
  Serial.println("HELP;  - display this message");
  Serial.println("CONFIG [Tx.xx] [Bx.xx] [Rx.xx] [Lx.xx];");
  Serial.println("       - display/update this robot's configuration.");
  Serial.println("WHERE; - display current virtual coordinates");
  Serial.println("DEMO;  - draw a test pattern");
  Serial.println("TELEPORT [Xx.xx] [Yx.xx]; - move the virtual plotter.");
  Serial.println("As well as the following G-codes (http://en.wikipedia.org/wiki/G-code):");
  Serial.println("G01-G04,G20,G21,G90,G91");
}


#endif  // SMALL_FOOTPRINT



//------------------------------------------------------------------------------
static void where() {
  Serial.print("(");
  Serial.print(posx);
  Serial.print(",");
  Serial.print(posy);
  Serial.print(",");
  Serial.print(posz);
  Serial.println(")");
}


//------------------------------------------------------------------------------
static void printConfig() {
  Serial.print("T");  Serial.println(limit_top);
  Serial.print("B");  Serial.println(limit_bottom);
  Serial.print("L");  Serial.println(limit_left);
  Serial.print("R");  Serial.println(limit_right);
  Serial.print("F");  Serial.println(feed_rate);
  Serial.print("A");  Serial.println(accel);
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
void EEPROM_writeDouble(int ee, double value) {
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
double EEPROM_readDouble(int ee) {
  double value = 0.0;
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
  } else if(!strncmp(buffer,"DEMO",4)) {
    demo();
  } else if(!strncmp(buffer,"TELEPORT",8)) {
    double xx=posx;
    double yy=posy;
  
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1)*mode_scale;  break;
      case 'Y': yy=atof(ptr+1)*mode_scale;  break;
      default: ptr=0; break;
      }
    }

    teleportSafe(xx,yy);
  } else if(!strncmp(buffer,"F",1)) {
    char *ptr=buffer+1;
    if(ptr<buffer+sofar) {
      setFeedRate(atof(ptr));
    }
  } else 
#endif
  if(!strncmp(buffer,"WHERE",5)) {
    where();
  } else if(!strncmp(buffer,"CONFIG",6)) {
    double tt=limit_top;
    double bb=limit_bottom;
    double rr=limit_right;
    double ll=limit_left;
    
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'T': tt=atof(ptr+1)*mode_scale;  break;
      case 'B': bb=atof(ptr+1)*mode_scale;  break;
      case 'R': rr=atof(ptr+1)*mode_scale;  break;
      case 'L': ll=atof(ptr+1)*mode_scale;  break;
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
    double xx, yy, zz, ff=feed_rate;
    
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
      case 'Z': zz=atof(ptr+1)*mode_scale;  break;
      case 'F': ff=atof(ptr+1)*mode_scale;  break;
      default: ptr=0; break;
      }
    }
 
    if(absolute_mode==0) {
      xx+=posx;
      yy+=posy;
      zz+=posz;
    }
    
    setFeedRate(ff);
    pen(zz);
    error(lineSafe(xx,yy));
  } else if(!strncmp(buffer,"G02 ",4) || !strncmp(buffer,"G2 " ,3) 
         || !strncmp(buffer,"G03 ",4) || !strncmp(buffer,"G3 " ,3)) {
    // arc
    double xx, yy, zz, ff=feed_rate;
    double dd = (!strncmp(buffer,"G02",3) || !strncmp(buffer,"G2",2)) ? -1 : 1;
    double ii = 0;
    double jj = 0;
    
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
      case 'Z': zz=atof(ptr+1)*mode_scale;  break;
      case 'F': ff=atof(ptr+1)*mode_scale;  break;
      default: ptr=0; break;
      }
    }
 
    if(absolute_mode==0) {
      xx+=posx;
      yy+=posy;
      zz+=posz;
    }

    setFeedRate(ff);
    pen(zz);
    error(arcSafe(posx+ii,posy+jj,xx,yy,dd));
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
    setup_planner();
    Serial.println("Planner on");
  } else if(!strncmp(buffer,"J01",3)) {
    // end jog mode
    setup_jogger();
    Serial.println("Jog on");
  } else if(!strncmp(buffer,"J02 ",4)) {
    // jog
    double xx=0;
    double yy=0;
    double zz=posz;
    double ff=feed_rate;

    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1)*mode_scale;  break;
      case 'Y': yy=atof(ptr+1)*mode_scale;  break;
      case 'Z': zz=atof(ptr+1)*mode_scale;  break;
      case 'F': ff=atof(ptr+1)*mode_scale;  break;
      default: ptr=0; break;
      }
    }

    setFeedRate(ff);
    pen(zz);
    jog(xx,yy);
  } else {
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      if(!strncmp(ptr,"G20",3)) {
        mode_scale=2.54;
        Serial.println("scale: inches.");
      } else if(!strncmp(ptr,"G21",3)) {
        mode_scale=1.0;
        Serial.println("scale: millimeters.");
      } else if(!strncmp(ptr,"G90",3)) {
        absolute_mode=1;
      } else if(!strncmp(ptr,"G91",3)) {
        absolute_mode=0;  
      } else if(ptr) {
        Serial.print("Invalid command: ");
        Serial.println(ptr);
        break;
      }
    }
  }
}


//------------------------------------------------------------------------------
void setup() {
  // start communications
  Serial.begin(BAUD);
  Serial.println("== HELLO WORLD ==");
  loadConfig();
  printConfig();
  sofar=0;

  // set the stepper speed
  m1.setSpeed(MAX_RATED_RPM);
  m2.setSpeed(MAX_RATED_RPM);
  // servo should be on SER1, pin 10.
  s1.attach(10);

#ifndef SMALL_FOOTPRINT
  // load string onto spool.  Only needed when the robot is being built.
//  loadspool();

  // test suite
//  testClock();
//  testKinematics( 0, 0);
//  testKinematics( 5, 0);
//  testKinematics( 0, 5);
//  testKinematics(-5,-5);
//  testInterpolation();
//  testFullCircle();
//  testAcceleration();
//  testfeed_rate();
//  testArcs();

  // display the help at startup.
  help();  
#endif  // SMALL_FOOTPRINT

  // initialize the plotter position.
  posx=velx=accelx=0;
  posy=velx=accelx=0;

  long L1,L2;
  IK(posx,posy,L1,L2);
  laststep1=L1;
  laststep2=L2;
  
  pen(PEN_UP_ANGLE);
  
  // start the timer interrupt
  setup_planner();

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
    Serial.println(buffer);
 
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


