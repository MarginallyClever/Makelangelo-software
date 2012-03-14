//------------------------------------------------------------------------------
// Draw robot
// dan@marginallycelver.com 2012 feb 11
//------------------------------------------------------------------------------
// Copyright at end of file.



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



//------------------------------------------------------------------------------
// CONSTANTS
//------------------------------------------------------------------------------
// Comment out this line to silence most serial output.
//#define VERBOSE         (1)

// Uncomment this line to compile for smaller boards
//#define SMALL_FOOTPRINT (1)

// Uncomment this line to use a more sophisticated motion profile
#define TRAPEZOID       (1)

// Distance between stepper shaft centers.
#define X_SEPARATION    (28.0)

// Distance from center of the drawing area up to the line between the two 
// steppers.  The plotter cannot physically reach this line - it would 
// require infinite tensile strength.
#define LIMYMAX         (21.5)

// Distance from center to bottom of drawing area.
#define LIMYMIN         (-30.0)

// distance from pen center to string ends
#define PLOTX           (2.4)
#define PLOTY           (1.9)

// which motor is on which pin?
#define M1_PIN          (2)
#define M2_PIN          (1)

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
#define MAX_RPM         (250.0)
#define DEFAULT_RPM     (200.0)

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

// calculate some numbers to help us find maxvel
#define SPOOL_CIRC      (SPOOL_DIAMETER*PI)  // circumference
#define THREADPERSTEP   (SPOOL_CIRC/STEPS_PER_TURN)  // thread per step

// if the plotter were hanging from a single stepper and the stepper turned at
// max RPM, how fast would the plotter move up/down?  All other motions are 
// cos(theta)*MAX_VEL, where theta is the angle between the desired
// direction of motion and a line from the plotter to the stepper.
#define MAX_VEL         (MAX_RPM*SPOOL_CIRC/60.0)  // cm/s

// max vel is only theoretical.  We have to run slower for accuracy.
#define DEFAULT_VEL     (DEFAULT_RPM*SPOOL_CIRC/60.0)  // cm/s

// limits plotter can move.
#define LIMXMAX         ( X_SEPARATION*0.5)
#define LIMXMIN         (-X_SEPARATION*0.5)

// for arc directions
#define ARC_CW          (1)
#define ARC_CCW         (-1)

// Serial communication bitrate
#define BAUD            (57600)
// Maximum length of serial input message.
#define MAX_BUF         (64)


//------------------------------------------------------------------------------
// VARIABLES
//------------------------------------------------------------------------------
// Initialize Adafruit stepper controller
static AF_Stepper m1((int)STEPS_PER_TURN, M2_PIN);
static AF_Stepper m2((int)STEPS_PER_TURN, M1_PIN);

static Servo s1;

// plotter position.
static float posx, velx, accelx;
static float posy, vely, accely;
static float posz;  // pen state

// motor position
static long laststep1, laststep2;

// acceleration.
static float accel=ACCELERATION;
static float maxvel=DEFAULT_VEL;

// absolute or incremental programming mode?
static char absolute_mode=1;

// time values
static long  t_millis;
static float t;   // since board power on
static float dt;  // since last tick

// Diameter of line made by plotter
static float tool_diameter=0.05;  

// Serial comm reception
static char buffer[MAX_BUF];  // Serial buffer
static int sofar;             // Serial buffer progress



//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------



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


#ifndef SMALL_FOOTPRINT
//------------------------------------------------------------------------------
// returns 0 if inside limits
// returns non-zero if outside limits.
static int outsideLimits(float x,float y) {
  return ((x<LIMXMIN)<<0)
        |((x>LIMXMAX)<<1)
        |((y<LIMYMIN)<<2)
        |((y>LIMYMAX)<<3);
}
#endif


//------------------------------------------------------------------------------
// Change pen state.
static void pen(float pen_angle) {
  posz=pen_angle;
  if(pen_angle<PEN_DOWN_ANGLE) posz=PEN_DOWN_ANGLE;
  if(pen_angle>PEN_UP_ANGLE  ) posz=PEN_UP_ANGLE;
  s1.write(posz);
  delay(PEN_DELAY);
}


//------------------------------------------------------------------------------
// Inverse Kinematics - turns XY coordinates into lengths L1,L2
static void IK(float x, float y, long &l1, long &l2) {
  // find length to M1
  float dy = y - LIMYMAX - PLOTY;
  float dx = x - PLOTX - LIMXMIN;
  l1 = floor( sqrt(dx*dx+dy*dy) / THREADPERSTEP );
  // find length to M2
  dx = LIMXMAX - (x + PLOTX);
  l2 = floor( sqrt(dx*dx+dy*dy) / THREADPERSTEP );
}


//------------------------------------------------------------------------------
// Forward Kinematics - turns L1,L2 lengths into XY coordinates
// use law of cosines,
// theta = acos((a*a+b*b-c*c)/(2*a*b));
// to find angle between M1M2 and M1P
// where P is the plotter position
static void FK(float l1, float l2,float &x,float &y) {
  float a=l1 * THREADPERSTEP;
  float b=X_SEPARATION - PLOTX*2;
  float c=l2 * THREADPERSTEP;
  
  // slow, uses trig
  float theta = acos((a*a+b*b-c*c)/(2.0*a*b));
  x = cos(theta)*l1 + LIMXMIN + PLOTX;
  y = sin(theta)*l1 + LIMYMAX - PLOTY;
}


//------------------------------------------------------------------------------
static void travelTime(float len,float vstart,float vend,float &t1,float &t2,float &t3) {
  
#ifdef TRAPEZOID

  t1 = (maxvel-vstart) / accel;
  t2 = (maxvel-vend) / accel;
  float d1 = vstart * t1 + 0.5 * accel * t1*t1;
  float d2 = vend * t2 + 0.5 * accel * t2*t2;
  float a = d1+d2;
  
  if(len>a) {
    t3 = t1+t2 + (len-a) / maxvel;
    t2 = t3-t2;
  } else {
    // http://wikipedia.org/wiki/Classical_mechanics#1-Dimensional_Kinematics
    float brake_distance=(2*accel*len-vstart*vstart+vend*vend) / (4*accel);
    // and we also know d=v0*t + att/2
    // so 
    t1 = ( -vstart + sqrt( 2*accel*brake_distance + vstart*vstart ) ) / accel;
    t2 = t1;
    t3 = t1 +( -vend + sqrt( 2*accel*(len-brake_distance) + vend*vend ) ) / accel;
  }
  
#else  // TRAPEZOID

  t3=len/maxvel;
  t1=t3/3;
  t2=t1*2;
  
#endif  // TRAPEZOID

}


//------------------------------------------------------------------------------
static float interpolate(float p0,float p3,float t,float t1,float t2,float t3,float len) {
  if(t<=0) return p0;

#ifdef TRAPEZOID
 
  float s = (p3-p0)/len;
  
  if(t<t1) {
    float d1 = 0.5 * accel * t * t;
    return p0 + (d1)*s;
  } else if(t<t2) {
    float d1 = 0.5 * accel * t1 * t1;
    float d2 = maxvel * (t-t1);
    return p0 + (d1+d2)*s;
  } else if(t<t3) {
    float t4 = t-t2;
    float d1 = 0.5 * accel * t1 * t1;
    float d2 = maxvel * (t2-t1);
    float v2 = accel*t1;
    float d3 = v2 * t4 - 0.5 * accel * t4 * t4;
    return p0 + (d1+d2+d3)*s;
  }
  
  return p3;

#else  // TRAPEZOID

  return p0 + (p3-p0) *(t/t3);

#endif  // TRAPEZOID

}


//------------------------------------------------------------------------------
// Turns the motors so that the real robot strings match our simulation.
// len1 & len2 are the current string lengths.
// nlen1 & nlen2 are the new string lengths.
static void adjustStringLengths(long nlen1,long nlen2) {
  // is the change in length >= one step?
  long steps=nlen1-laststep1;
  if(steps<0)      m1.step(-steps,REEL_IN ,STEP_MODE);  // it is shorter.
  else if(steps>0) m1.step( steps,REEL_OUT,STEP_MODE);  // it is longer.
  
  // is the change in length >= one step?
  steps=nlen2-laststep2;
  if(steps<0)      m2.step(-steps,REEL_IN ,STEP_MODE);  // it is shorter.
  else if(steps>0) m2.step( steps,REEL_OUT,STEP_MODE);  // it is longer.
  
  laststep1=nlen1;
  laststep2=nlen2;
}


//------------------------------------------------------------------------------
static float test_count=0;

static void jogStep() {
  long nlen1;
  long nlen2;

  tick();
  
  // automatic slowdown when key is released.  
  if(velx!=0 && accelx==0) {
    if(abs(velx)<accel*dt) velx=0;
    else if(velx<0)        velx+=accel*dt;
    else                   vely-=accel*dt;
  }
  if(vely!=0 && accely==0) {
    if(abs(vely)<accel*dt) vely=0;
    else if(vely<0)        vely+=accel*dt;
    else                   vely-=accel*dt;
  }

  if( velx!=0 || vely!=0 ) {
    test_count+=dt;
    float interval=0.25;
    if(test_count>interval) {
      Serial.print(velx);
      Serial.print(",");
      Serial.print(vely);
      Serial.print("\t");
      Serial.print(accelx);
      Serial.print(",");
      Serial.println(accely);
      test_count-=interval;
    }
    
    velx+=accelx*dt;
    vely+=accely*dt;
    float vtotal = sqrt(velx*velx+vely*vely);
    if(vtotal>maxvel) {
      float scale = maxvel/vtotal;
      velx*=scale;
      vely*=scale;
    }
    posx+=velx*dt;
    posy+=vely*dt;
  }
  
  IK(posx,posy,nlen1,nlen2);
  adjustStringLengths(nlen1,nlen2);
}


//------------------------------------------------------------------------------
// change the acceleration (for drive-by kb or joystick)
static void jog(float x,float y) {
  accelx=x*accel;
  accely=y*accel;

  float atotal = sqrt(accelx*accelx+accely*accely);
  if(atotal>accel) {
    float scale = accel/atotal;
    accelx*=scale;
    accely*=scale;
  }
}


//------------------------------------------------------------------------------
// This method assumes the limits have already been checked.
static void line(float x,float y) {
  float dx = x - posx;  // delta
  float dy = y - posy;
  float len = sqrt(dx*dx+dy*dy);
  float t1;
  float t2;
  float time;
  float tsum;
  float nx,ny;
  long nlen1,nlen2;
  char did_step;

  // get travel time
  travelTime(len,0,0,t1,t2,time);

  IK(x,y,nlen1,nlen2);
    
#ifdef VERBOSE
  Serial.print("x=");           Serial.println(x);
  Serial.print("y=");           Serial.println(y);
  Serial.print("posx=");        Serial.println(posx);
  Serial.print("posy=");        Serial.println(posy);
  Serial.print("start len1=");  Serial.println(laststep1);
  Serial.print("start len2=");  Serial.println(laststep2);
  Serial.print("end len1=");    Serial.println(nlen1);
  Serial.print("end len2=");    Serial.println(nlen2);
  Serial.print("dx=");          Serial.println(dx);
  Serial.print("dy=");          Serial.println(dy);
  Serial.print("len=");         Serial.println(len);
  Serial.print("time=");        Serial.println(time);
  long cnt=0;
  long a=micros();
  
#endif
  
  tick();
  float tstart=t;
  
  do {
    tick();
    tsum = t-tstart;
    if(tsum>time) tsum=time;

    // find where the plotter will be at tsum seconds
    nx = interpolate(posx,x,tsum,t1,t2,time,len);
    ny = interpolate(posy,y,tsum,t1,t2,time,len);
       
    // get the new string lengths
    IK(nx,ny,nlen1,nlen2);
    adjustStringLengths(nlen1,nlen2);

#ifdef VERBOSE
         if(tsum<t1  ) Serial.print("A\t");
    else if(tsum<t2  ) Serial.print("B\t");
    else if(tsum<time) Serial.print("C\t");
    else               Serial.print("D\t");
    Serial.print(tsum);       Serial.print('\t');
    Serial.print(t_millis);   Serial.print('\t');
    Serial.print(nx);         Serial.print('\t');
    Serial.print(ny);         Serial.print('\t');
    Serial.print(nlen1);      Serial.print('\t');
    Serial.print(nlen2);      Serial.print('\t');
    Serial.print(laststep1);  Serial.print('\t');
    Serial.print(laststep2);  Serial.print('\n');
    ++cnt;
#endif
  } while(tsum<time);

  posx=x;
  posy=y;

#ifdef VERBOSE  
  long b=micros();
  Serial.print((float)(b-a)/(float)cnt);
  Serial.println(" microseconds/loop average");
  Serial.println("Done.");
#endif
}


//------------------------------------------------------------------------------
// checks against the robot limits before attempting to move
static int lineSafe(float x,float y) {
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
static void arc(float cx,float cy,float x,float y,float dir) {
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
  
  // get travel time
  float t1;
  float t2;
  float time;
  travelTime(len,0,0,t1,t2,time);
  
#ifdef VERBOSE
  Serial.print("a1=");      Serial.println(angle1);
  Serial.print("a2=");      Serial.println(angle2);
  Serial.print("theta=");   Serial.println(theta*180.0/PI);
  Serial.print("x=");       Serial.println(x);
  Serial.print("y=");       Serial.println(y);
  Serial.print("posx=");    Serial.println(posx);
  Serial.print("posy=");    Serial.println(posy);
  Serial.print("radius=");  Serial.println(radius);
  Serial.print("len=");     Serial.println(len);
  Serial.print("time=");    Serial.println(time);
#endif

  float nx,ny;
  long nlen1,nlen2;
  char did_step;
  float tsum;

  tick();
  float tstart=t;
 
  do {
    tick();
    tsum=t-tstart;
    if(tsum>time) tsum=time;

    // find where the plotter will be at tsum seconds
    float angle3 = interpolate(angle1,angle2,tsum,t1,t2,time,len);
    nx = cx + cos(angle3) * radius;
    ny = cy + sin(angle3) * radius;

    // @TODO: could the rest of this loop be replaced with line(nx,ny)?

    // get the new string lengths
    IK(nx,ny,nlen1,nlen2);
    adjustStringLengths(nlen1,nlen2);
    
#ifdef VERBOSE
         if(tsum<t1  ) Serial.print("A\t");
    else if(tsum<t2  ) Serial.print("B\t");
    else if(tsum<time) Serial.print("C\t");
    Serial.print(tsum);    Serial.print('\t');
    Serial.print(nx);      Serial.print('\t');
    Serial.print(ny);      Serial.print('\t');
    Serial.print(nlen1);   Serial.print('\t');
    Serial.print(nlen2);   Serial.print('\n');
#endif

  } while(tsum < time);
  
  posx=x;
  posy=y;
  
#ifdef VERBOSE  
  Serial.println("Done.");
#endif
}



#ifndef SMALL_FOOTPRINT



//------------------------------------------------------------------------------
// is the point (dx,dy) in the arc segment subtended by angle1,angle2?
// return 0 if it is not.
static int pointInArc(float dx,float dy,float angle1,float angle2,float dir) {
  float angle3=atan3(-dy,dx);  // atan2 expects +y to be up, so flip the sign
  
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
static int canArc(float cx,float cy,float x,float y,float dir) {
#ifdef VERBOSE  
  limits();
  Serial.print("end=");  Serial.print(x);
  Serial.print(",");     Serial.println(y);
#endif
  if(outsideLimits(x,y)) return 1;

  float a=x-cx;
  float b=y-cy;
  float c=posx-cx;
  float d=posy-cy;
  float r1=sqrt(a*a+b*b);
  float r2=sqrt(c*c+d*d);
  
  if( abs(r1-r2) > 0.001 ) {
    Serial.print("r1=");  Serial.println(r1);
    Serial.print("r2=");  Serial.println(r2);

    return 2;  // radii don't match
  }
  
  float angle1=atan3(d,c);
  float angle2=atan3(b,a);

#ifdef VERBOSE
  Serial.print("A:");  Serial.print(c);
  Serial.print(",");   Serial.print(d);
  Serial.print("=");   Serial.println(angle1*180.0/PI);
  Serial.print("B:");  Serial.print(a);
  Serial.print(",");   Serial.print(b);
  Serial.print("=");   Serial.println(angle2*180.0/PI);
  Serial.print("r=");  Serial.println(r1);
#endif

  if(cx+r1>LIMXMAX) {
    // find the two points of intersection, see if they are inside the arc
    float dx=LIMXMAX-cx;
    float dy=sqrt(r1*r1-dx*dx);
    if(pointInArc(dx, dy,angle1,angle2,dir)) return 3;
    if(pointInArc(dx,-dy,angle1,angle2,dir)) return 4;
  }
  if(cx-r1<LIMXMIN) {
    // find the two points of intersection, see if they are inside the arc
    float dx=LIMXMIN-cx;
    float dy=sqrt(r1*r1-dx*dx);
    if(pointInArc(dx, dy,angle1,angle2,dir)) return 5;
    if(pointInArc(dx,-dy,angle1,angle2,dir)) return 6;
  }
  if(cy+r1>LIMYMAX) {
    // find the two points of intersection, see if they are inside the arc
    float dy=LIMYMAX-cy;
    float dx=sqrt(r1*r1-dy*dy);
    if(pointInArc( dx,dy,angle1,angle2,dir)) return 7;
    if(pointInArc(-dx,dy,angle1,angle2,dir)) return 8;
  }
  if(cy-r1<LIMYMIN) {
    // find the two points of intersection, see if they are inside the arc
    float dy=LIMYMIN-cy;
    float dx=sqrt(r1*r1-dy*dy);
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
static int arcSafe(float cx,float cy,float x,float y,float dir) {
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
  float x,y;
  
  Serial.println(atan3( 1, 1)*180.0/PI);
  Serial.println(atan3( 1,-1)*180.0/PI);
  Serial.println(atan3(-1,-1)*180.0/PI);
  Serial.println(atan3(-1, 1)*180.0/PI);
  
  x=LIMXMAX*0.75;
  y=LIMYMAX*0.50;

  Serial.println("arcs inside limits, center inside limits (should not fail)");
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

  x=LIMXMAX*0.75;
  y=LIMYMAX*0.50;
  Serial.println("arcs outside limits, arc center outside limits (should fail)");
  teleport(x,y);
  error(canArc(x,0,-x,y,ARC_CW));

  // LIMXMAX boundary test
  x=LIMXMAX*0.75;
  y=LIMYMAX*0.50;
  Serial.println("CCW through LIMXMAX (should fail)");      teleport(x, y);  error(canArc(x,0, x,-y, ARC_CCW));
  Serial.println("CW avoids LIMXMAX (should not fail)");    teleport(x, y);  error(canArc(x,0, x,-y, ARC_CW));
  Serial.println("CW through LIMXMAX (should fail)");       teleport(x,-y);  error(canArc(x,0, x, y, ARC_CW));
  Serial.println("CCW avoids LIMXMAX (should not fail)");   teleport(x,-y);  error(canArc(x,0, x, y, ARC_CCW));
  // LIMXMIN boundary test
  x=LIMXMIN*0.75;
  y=LIMYMAX*0.50;
  Serial.println("CW through LIMXMIN (should fail)");       teleport(x, y);  error(canArc(x,0, x,-y, ARC_CW));
  Serial.println("CCW avoids LIMXMIN (should not fail)");   teleport(x, y);  error(canArc(x,0, x,-y, ARC_CCW));
  Serial.println("CCW through LIMXMIN (should fail)");      teleport(x,-y);  error(canArc(x,0, x, y, ARC_CCW));
  Serial.println("CW avoids LIMXMIN (should not fail)");    teleport(x,-y);  error(canArc(x,0, x, y, ARC_CW));
  // LIMYMIN boundary test
  x=LIMXMAX*0.50;
  y=LIMYMIN*0.75;
  Serial.println("CW through LIMYMIN (should fail)");       teleport( x,y);  error(canArc(0,y,-x, y, ARC_CW));
  Serial.println("CCW avoids LIMYMIN (should not fail)");   teleport( x,y);  error(canArc(0,y,-x, y, ARC_CCW));
  Serial.println("CCW through LIMYMIN (should fail)");      teleport(-x,y);  error(canArc(0,y, x, y, ARC_CCW));
  Serial.println("CW avoids LIMYMIN (should not fail)");    teleport(-x,y);  error(canArc(0,y, x, y, ARC_CW));
  // LIMYMAX boundary test
  x=LIMXMAX*0.50;
  y=LIMYMAX*0.75;
  Serial.println("CCW through LIMYMAX (should fail)");      teleport( x,y);  error(canArc(0,y,-x, y, ARC_CCW));
  Serial.println("CW avoids LIMYMAX (should not fail)");    teleport( x,y);  error(canArc(0,y,-x, y, ARC_CW));
  Serial.println("CW through LIMYMAX (should fail)");       teleport(-x,y);  error(canArc(0,y, x, y, ARC_CW));
  Serial.println("CCW avoids LIMYMAX (should not fail)");   teleport(-x,y);  error(canArc(0,y, x, y, ARC_CCW));
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
static void testKinematics(float x,float y) {
  Serial.println("-- TEST KINEMATICS --");
  teleport(x,y);
  float xx,yy;
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
  float start=0;
  float end=1;
  float t1,t2,t3;
  float oldv=0,v;
  
  Serial.println("dist=1");
  travelTime(end-start,0,0,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(float t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }

  oldv=0;
  Serial.println("dist=5");
  end=5;
  travelTime(end-start,0,0,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(float t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start);
    if(t<t1) Serial.print("A\t");
    else if(t<t2) Serial.print("B\t");
    else if(t<t3) Serial.print("C\t");
    Serial.print(v);
    Serial.print("\t");
    Serial.println(v-oldv);
    oldv=v;
  }

  oldv=0;
  Serial.println("dist=15");
  end=15;
  travelTime(end-start,0,0,t1,t2,t3);
  Serial.print("t1=");  Serial.println(t1);
  Serial.print("t2=");  Serial.println(t2);
  Serial.print("t3=");  Serial.println(t3);
  for(float t=0;t<t3;t+=0.1) {
    v=interpolate(start,end,t,t1,t2,t3,end-start);
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
  Serial.print("maxvel=");  Serial.println(maxvel);

  float i;
  float a=10;
  float b=0;
  float c;
  for(i=3;i<maxvel;i+=0.5) {
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
static void testMaxVel() {
  Serial.println("-- TEST MAX VELOCITY --");
  Serial.print("ACCEL=");  Serial.println(ACCELERATION);
  Serial.print("MAX_VEL=");  Serial.println(MAX_VEL);

  float i;
  float a=10;
  float b=-10;
  float c;

  line(b,0);

  for(i=5;i<MAX_VEL;i+=1) {
    delay(2000);
    maxvel=i;
    Serial.println(maxvel);
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
// Show off line and arc movement.  This is the test pattern.
static void demo() {
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
    Serial.println((float)i/11.0);
    halftone(1,(float)i/11.0);
  }

  // return to origin
  //Serial.println("> L 6, 6");              line( 6, 6);
  Serial.println("> CENTER");              line( 0, 0);
}


//------------------------------------------------------------------------------
// instantly move the virtual plotter position
// does not validate if the move is valid
static void teleport(float x,float y) {
  posx=x;
  posy=y;
  IK(posx,posy,laststep1,laststep2);
}


//------------------------------------------------------------------------------
// instantly move the virtual plotter position
// checks against the robot limits before attempting to move
static int teleportSafe(float x,float y) {
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
static void halftone(float size,float fill) {
  float ymin=posy-(size*0.5);
  float ymax=posy+(size*0.5);
  
  float max_lines = size / tool_diameter;
  int infill = floor( max_lines * fill );

#ifdef VERBOSE
  Serial.print("size=");        Serial.println(size);
  Serial.print("fill=");        Serial.println(fill);
  Serial.print("max_lines=");   Serial.println(max_lines);
  Serial.print("infill=");      Serial.println(infill);
#endif

  // Save starting location because line() changes posx,posy!
  float ox=posx;
  float oy=posy;
  
  if(infill>1) {
    float step = size / (float)(infill);
#ifdef VERBOSE
    Serial.print("step=");          Serial.println(step);
#endif
    
    float x2=ox;
    float y2=oy;
    
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
  
  Serial.println("HELP; - display this message");
  Serial.println("WHERE; - display current virtual coordinates");
  Serial.println("LIMITS; - display maximum distance plotter can move");
  Serial.println("DEMO; - draw a test pattern");
  Serial.println("TELEPORT [Xx.xx] [Yx.xx]; - move the virtual plotter.");
  Serial.println("G00 [Xx.xx] [Yx.xx]; - draw line to X,Y");
  Serial.println("G01 [Xx.xx] [Yx.xx]; - draw line to X,Y");
  Serial.println("G02 Ix.xx Jx.xx [Xx.xx] [Yx.xx]; - draw a clockwise arc around I,J to X,Y.");
  Serial.println("G03 Ix.xx Jx.xx [Xx.xx] [Yx.xx]; - draw a counterclockwise arc around I,J to X,Y.");
  Serial.println("Fx.xx; - set feed rate (max speed). Can be done on the same line as a G command.");
  Serial.println("Zx.xx; - set pen height.  Can be done on the same line as a G command.");
  Serial.println("G90 - set absolute coordinates mode (default)");
  Serial.println("G91 - set relative coordinates mode");
  Serial.print("> ");
}


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
static void limits() {
  Serial.print("(");
  Serial.print(LIMXMIN);
  Serial.print(",");
  Serial.print(LIMYMIN);
  Serial.print(") - {");
  Serial.print(LIMXMAX);
  Serial.print(",");
  Serial.print(LIMYMAX);
  Serial.println(")");

  Serial.print("F");  Serial.println(maxvel);
  Serial.print("A");  Serial.println(accel);
}



#endif



//------------------------------------------------------------------------------
static void processCommand() {
#ifndef SMALL_FOOTPRINT
  if(!strncmp(buffer,"HELP",4)) {
    help();
  } else if(!strncmp(buffer,"WHERE",5)) {
    where();
  } else if(!strncmp(buffer,"LIMITS",6)) {
    limits();
  } else if(!strncmp(buffer,"DEMO",4)) {
    demo();
  } else if(!strncmp(buffer,"TELEPORT",8)) {
    float xx=posx;
    float yy=posy;
  
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1);  break;
      case 'Y': yy=atof(ptr+1);  break;
      default: ptr=0; break;
      }
    }

    teleportSafe(xx,yy);
  } else 
#endif
  if(!strncmp(buffer,"f",1)) {
    char *ptr=buffer+1;
    if(ptr<buffer+sofar) {
      maxvel=atof(ptr);
    }
  } else if(!strncmp(buffer,"G90",3)) {
    absolute_mode=1;
  } else if(!strncmp(buffer,"G91",3)) {
    absolute_mode=0;  
  } else if(!strncmp(buffer,"G00 ",4) || !strncmp(buffer,"G01 ",4)
         || !strncmp(buffer,"G0 " ,3) || !strncmp(buffer,"G1 " ,3) ) {
    // line
    float xx, yy, zz, ff=maxvel;
    
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
      case 'X': xx=atof(ptr+1);  break;
      case 'Y': yy=atof(ptr+1);  break;
      case 'Z': zz=atof(ptr+1);  break;
      case 'F': ff=atof(ptr+1);  break;
      default: ptr=0; break;
      }
    }
 
    if(absolute_mode==0) {
      xx+=posx;
      yy+=posy;
      zz+=posz;
    }
    
    maxvel=ff;
    pen(zz);
    error(lineSafe(xx,yy));
  } else if(!strncmp(buffer,"G02 ",4) || !strncmp(buffer,"G03 ",4)
         || !strncmp(buffer,"G2 " ,3) || !strncmp(buffer,"G3 " ,3)) {
    // arc
    float xx, yy, zz, ff=maxvel;
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
      case 'I': ii=atof(ptr+1);  break;
      case 'J': jj=atof(ptr+1);  break;
      case 'X': xx=atof(ptr+1);  break;
      case 'Y': yy=atof(ptr+1);  break;
      case 'Z': zz=atof(ptr+1);  break;
      case 'F': ff=atof(ptr+1);  break;
      default: ptr=0; break;
      }
    }
 
    if(absolute_mode==0) {
      xx+=posx;
      yy+=posy;
      zz+=posz;
    }

    maxvel=ff;
    pen(zz);
    error(arcSafe(posx+ii,posy+jj,xx,yy,dd));
  } else if(!strncmp(buffer,"J00",3)) {
    // jog
    float xx=0;
    float yy=0;
    float zz=posz;
    float ff=maxvel;

    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'X': xx=atof(ptr+1);  break;
      case 'Y': yy=atof(ptr+1);  break;
      case 'Z': zz=atof(ptr+1);  break;
      case 'F': ff=atof(ptr+1);  break;
      default: ptr=0; break;
      }
    }

    maxvel=ff;
    pen(zz);
    jog(xx,yy);
  } else {
    Serial.print("Invalid command: ");
    Serial.println(buffer);
  }
}


//------------------------------------------------------------------------------
void setup() {
  // start communications
  Serial.begin(BAUD);
  Serial.println("== HELLO WORLD ==");
  limits();
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
//  testMaxVel();
//  testArcs();

  // display the help at startup.
  help();  
#endif  // SMALL_FOOTPRINT

  // initialize the plotter position.
  posx=velx=accelx=0;
  posy=velx=accelx=0;
  IK(posx,posy,laststep1,laststep2);
  pen(PEN_UP_ANGLE);
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
  
  jogStep();
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