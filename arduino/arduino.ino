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
#define SPOOL_DIAMETER  (0.85)
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
  double sx, sy, sz;
  double ex, ey, ez;
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

// old pen position
static int old_pen_angle=-1;

// switch state
static char switch1;
static char switch2;

// motor position
static volatile long laststep1, laststep2;

// speeds, feeds, and delta-vs.
static double accel=ACCELERATION;
static double feed_rate=DEFAULT_VEL;

static char absolute_mode=1;  // absolute or incremental programming mode?
static double mode_scale;   // mm or inches?
static char mode_name[3];

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
// feed rate is given in units/min and converted to cm/s
static void setFeedRate(double v) {
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
// returns angle of dy/dx as a value from 0...2PI
static double atan3(double dy,double dx) {
  double a=atan2(dy,dx);
  if(a<0) a=(PI*2.0)+a;
  return a;
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
//    current_block->tsum += dt;
    if(current_block->tsum > current_block->time) {
      current_block->tsum = current_block->time;
    }

    // find where the plotter will be at tsum seconds
    double nx = interpolate(current_block->sx,
                            current_block->ex,
                            current_block->tsum,
                            current_block->t1,
                            current_block->t2,
                            current_block->time,
                            current_block->len,
                            current_block->startv,
                            current_block->endv,
                            current_block->topv);
    double ny = interpolate(current_block->sy,
                            current_block->ey,
                            current_block->tsum,
                            current_block->t1,
                            current_block->t2,
                            current_block->time,
                            current_block->len,
                            current_block->startv,
                            current_block->endv,
                            current_block->topv);
//    double nz = (current_block->ez - current_block->sz) * (current_block->tsum / current_block->time) + current_block->sz;

    // get the new string lengths
    long nlen1,nlen2;
    IK(nx,ny,nlen1,nlen2);

    // move the motors
    adjustStringLengths(nlen1,nlen2);
    // move the pen
//    setPenAngle(current_block->ez);

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
// if you must reach target_vel within distance and you speed up by acceleration
// then what must your minimum velocity be?
static double max_allowable_speed(double acceleration,double target_vel,double distance) {
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
static void line(double x,double y,double z) {
#ifdef VERBOSE
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
#endif

  int next_head=NEXT_BLOCK(block_head);
  // while there is no room in the queue, wait.
  while(block_tail==next_head) sleep_mode();
    
  // set up the new block in the queue.
  double dx=x-posx;
  double dy=y-posy;
  double dz=z-posz;

#ifdef VERBOSE
  Serial.print("posx ");  Serial.println(posx);
  Serial.print("posy ");  Serial.println(posy);
  Serial.print("posz ");  Serial.println(posz);
  Serial.print("dx ");    Serial.println(dx);
  Serial.print("dy ");    Serial.println(dy);
  Serial.print("dz ");    Serial.println(dz);
#endif

  block *new_block=&blocks[block_head];
  new_block->sx=posx;
  new_block->sy=posy;
  new_block->sz=posz;
  new_block->ex=x;
  new_block->ey=y;
  new_block->ez=z;
  new_block->startv=0;
  new_block->endv=0;
  new_block->topv=feed_rate;
  new_block->len = sqrt(dx*dx + dy*dy);// + dz*dz);
  new_block->tsum=0;

  //if(new_block->len==0) return;
  if( sqrt(dx*dx + dy*dy + dz*dz) == 0 ) return;
  
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
    //double z1 = ( curr->ez - curr->sz ) / curr->len;
    double x2 = ( next->ex - next->sx ) / next->len;
    double y2 = ( next->ey - next->sy ) / next->len;
    //double z2 = ( curr->ez - curr->sz ) / curr->len;
    double dotproduct = x1*x2 + y1*y2;// + z1*z2;

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
// This method assumes the limits have already been checked.
// This method assumes the start and end radius match.
// This method assumes arcs are not >180 degrees (PI radians)
// cx/cy - center of circle
// x/y - end position
// dir - ARC_CW or ARC_CCW to control direction of arc
static void arc(double cx,double cy,double x,double y,double z,double dir) {
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
 
  double nx, ny, nz, angle3, scale;
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
    nz = ( z - posz ) * scale + posz;
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
    line(nx,ny,nz);
  }
  
  line(x,y,z);
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
static void help() {
  Serial.println("== DRAWBOT - http://github.com/i-make-robots/Drawbot/ ==");
  Serial.println("All commands end with a semi-colon.");
  Serial.println("HELP;  - display this message");
  Serial.println("CONFIG [Tx.xx] [Bx.xx] [Rx.xx] [Lx.xx];");
  Serial.println("       - display/update this robot's configuration.");
  Serial.println("HOME;  - recalibrate and move to 0,0");
  Serial.println("WHERE; - display current virtual coordinates");
  Serial.println("DEMO;  - draw a test pattern");
  Serial.println("TELEPORT [Xx.xx] [Yx.xx]; - move the virtual plotter.");
  Serial.println("As well as the following G-codes (http://en.wikipedia.org/wiki/G-code):");
  Serial.println("G00,G01,G02,G03,G04,G20,G21,G90,G91");
}


//------------------------------------------------------------------------------
static void readSwitches() {
  // get the current switch state
  switch1=analogRead(S1_PIN) > SWITCH_HALF;
  switch2=analogRead(S2_PIN) > SWITCH_HALF;
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

  // set the stepper speed
  m1.setSpeed(MAX_RPM);
  m2.setSpeed(MAX_RPM);
  
  // reel in the left motor until contact is made.
  do {
    m1.step(1,REEL_IN );
    m2.step(1,REEL_OUT);
    readSwitches();
  } while(switch1==1);

  Serial.println("Found left...");
  laststep1=0;

  
  // reel in the right motor until contact is made
  do {
    m2.step(1,REEL_IN );
    m1.step(1,REEL_OUT);
    laststep1++;
    readSwitches();
  } while(switch2==1);

  Serial.println("Found right...");
  laststep2=0;
  
  Serial.println("Calculating IK...");
  // center the robot
  long L1,L2;
  IK(0,0,L1,L2);

  Serial.println("Centering...");
  adjustStringLengths(L1,L2);

  // set the stepper speed
  m1.setSpeed(MAX_RATED_RPM);
  m2.setSpeed(MAX_RATED_RPM);
}


//------------------------------------------------------------------------------
static void where() {
  Serial.print("(");
  Serial.print(posx);
  Serial.print(",");
  Serial.print(posy);
  Serial.print(",");
  Serial.print(posz);
  Serial.print(") F=");
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
  } else if(!strncmp(buffer,"HOME",4)) {
    goHome();
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

    teleport(xx,yy);
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
    double xx, yy, zz;
    
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
    double xx, yy, zz;
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
    double xx=0;
    double yy=0;
    double zz=posz;

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
  Serial.println("== HELLO WORLD ==");

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
  
  // set the stepper speed
  m1.setSpeed(MAX_RATED_RPM);
  m2.setSpeed(MAX_RATED_RPM);
  
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
//  testServo();

  // display the help at startup.
  help();  
#endif  // SMALL_FOOTPRINT

  // initialize the plotter position.
  posx=velx=accelx=0;
  posy=velx=accelx=0;
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


