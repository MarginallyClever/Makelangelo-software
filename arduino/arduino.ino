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
#define L_PIN          (A3)
#define R_PIN          (A5)

// NEMA17 are 200 steps (1.8 degrees) per turn.  If a spool is 0.8 diameter
// then it is 2.5132741228718345 circumference, and
// 2.5132741228718345 / 200 = 0.0125663706 thread moved each step.
// NEMA17 are rated up to 3000RPM.  Adafruit can handle >1000RPM.
// These numbers directly affect the maximum velocity.
#define STEPS_PER_TURN  (200.0)
#define MAX_RPM         (2000.0)

// delay between steps, in microseconds.
#define STEP_DELAY      (5200)  // = 3.5ms


// *****************************************************************************
// *** Don't change the constants below unless you know what you're doing.   ***
// *****************************************************************************

// switch sensitivity
#define SWITCH_HALF     (512)

// servo angles for pen control
#define PEN_UP_ANGLE    (90)
#define PEN_DOWN_ANGLE  (10)  // Some steppers don't like 0 degrees
#define PEN_DELAY       (150)  // in ms

#define MAX_STEPS_S     (STEPS_PER_TURN*MAX_RPM/60.0)  // steps/s

#define MIN_VEL         (0.001) // cm/s

// for arc directions
#define ARC_CW          (1)
#define ARC_CCW         (-1)
// Arcs are split into many line segments.  How long are the segments?
#define CM_PER_SEGMENT   (0.1)

// Serial communication bitrate
#define BAUD            (57600)
// Maximum length of serial input message.
#define MAX_BUF         (64)

// servo pin differs based on device
#ifdef __AVR_ATmega2560__
#define SERVO_PIN        50
#else
#define SERVO_PIN        9
#endif


//------------------------------------------------------------------------------
// VARIABLES
//------------------------------------------------------------------------------
// Initialize Adafruit stepper controller
static AF_Stepper m1((int)STEPS_PER_TURN, M2_PIN);
static AF_Stepper m2((int)STEPS_PER_TURN, M1_PIN);

static Servo s1;

// robot UID
long robot_uid=0;

// plotter limits
// all distances are relative to the calibration point of the plotter.
// (normally this is the center of the drawing area)
static float limit_top = 0;  // distance to top of drawing area.
static float limit_bottom = 0;  // Distance to bottom of drawing area.
static float limit_right = 0;  // Distance to right of drawing area.
static float limit_left = 0;  // Distance to left of drawing area.

// what are the motors called?
char m1d='L';
char m2d='R';

// which way are the spools wound, relative to motor movement?
int M1_REEL_IN  = FORWARD;
int M1_REEL_OUT = BACKWARD;
int M2_REEL_IN  = FORWARD;
int M2_REEL_OUT = BACKWARD;

// calculate some numbers to help us find feed_rate
float SPOOL_DIAMETER = 0.950;
float SPOOL_CIRC     = SPOOL_DIAMETER*PI;  // circumference
float THREADPERSTEP  = SPOOL_CIRC/STEPS_PER_TURN;  // thread per step
float MAX_VEL        = MAX_STEPS_S * THREADPERSTEP;  // cm/s

// plotter position.
static float posx, velx;
static float posy, vely;
static float posz;  // pen state
static float feed_rate=0;

// old pen position
static int old_pen_angle=-1;

// switch state
static char switch1, switch2;

// motor position
static long laststep1, laststep2;

static char absolute_mode=1;  // absolute or incremental programming mode?
static float mode_scale;   // mm or inches?
static char mode_name[3];

// time values
static long  t_millis;
static float t;   // since board power on
static float dt;  // since last tick

// Serial comm reception
static char buffer[MAX_BUF];  // Serial buffer
static int sofar;             // Serial buffer progress


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------



//------------------------------------------------------------------------------
// calculate max velocity, threadperstep.
static void adjustSpoolDiameter(float dia) {
  SPOOL_DIAMETER = dia;
  SPOOL_CIRC     = SPOOL_DIAMETER*PI;  // circumference
  THREADPERSTEP  = SPOOL_CIRC/STEPS_PER_TURN;  // thread per step
  MAX_VEL        = MAX_STEPS_S * THREADPERSTEP;  // cm/s
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


//------------------------------------------------------------------------------
static char readSwitches() {
  // get the current switch state
  switch1=analogRead(L_PIN) < SWITCH_HALF;
  switch2=analogRead(R_PIN) < SWITCH_HALF;
  
  return ( switch1 | switch2 );
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
/*
  Serial.print(" (");
  Serial.print((feed_rate/THREADPERSTEP));
  Serial.print(" steps/s) ");
  Serial.print((MAX_VEL*60.0/mode_scale));
  Serial.print(mode_name);
  Serial.print("/min ");
  Serial.print((MIN_VEL*60.0/mode_scale));
  Serial.print(mode_name);
  Serial.print("/min");
*/
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
static void line(float x,float y,float z) {
  long l1,l2;
  IK(x,y,l1,l2);
  long d1 = l1 - laststep1;
  long d2 = l2 - laststep2;

  long ad1=abs(d1);
  long ad2=abs(d2);
  int dir1=d1<0?M1_REEL_IN:M1_REEL_OUT;
  int dir2=d2<0?M2_REEL_IN:M2_REEL_OUT;
  long over=0;
  long i;
  
  int step_delay=1000000.0/(feed_rate/THREADPERSTEP);
  
  // bresenham's line algorithm.
  if(ad1>ad2) {
    for(i=0;i<ad1;++i) {
      m1.onestep(dir1);
      over+=ad2;
      if(over>=ad1) {
        over-=ad1;
        m2.onestep(dir2);
      }
      delayMicroseconds(step_delay);
      if(readSwitches()) return;
    }
  } else {
    for(i=0;i<ad2;++i) {
      m2.onestep(dir2);
      over+=ad1;
      if(over>=ad2) {
        over-=ad2;
        m1.onestep(dir1);
      }
      delayMicroseconds(step_delay);
      if(readSwitches()) return;
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
  Serial.println("TELEPORT [Xx.xx] [Yx.xx]; - move the virtual plotter.");
  Serial.println("As well as the following G-codes (http://en.wikipedia.org/wiki/G-code):");
  Serial.println("G00,G01,G02,G03,G04,G20,G21,G28,G90,G91,M114");
}


//------------------------------------------------------------------------------
// find the current robot position and 
static void goHome() {
  Serial.println("Homing...");
  readSwitches();
  
  if(switch1 || switch2) {
    Serial.println("** ERROR **");
    Serial.println("Problem: Plotter is already touching switches.");
    Serial.println("Solution: Please unwind the strings a bit and try again.");
    return;
  }
  
  int step_delay=3;
  int safe_out=400;
  
  // reel in the left motor until contact is made.
  Serial.println("Find left...");
  do {
    m1.step(1,M1_REEL_IN );
    m2.step(1,M2_REEL_OUT);
    delay(step_delay);
  } while(!readSwitches());
  laststep1=0;
  
  // back off so we don't get a false positive on the next motor
  int i;
  for(i=0;i<safe_out;++i) {
    m1.step(1,M1_REEL_OUT);
    delay(step_delay);
  }
  laststep1=safe_out;
  
  // reel in the right motor until contact is made
  Serial.println("Find right...");
  do {
    m1.step(1,M1_REEL_OUT);
    m2.step(1,M2_REEL_IN );
    delay(step_delay);
    laststep1++;
  } while(!readSwitches());
  laststep2=0;
  
  // back off so we don't get a false positive that kills line()
  for(i=0;i<safe_out;++i) {
    m2.step(1,M2_REEL_OUT);
    delay(step_delay);
  }
  laststep2=safe_out;
  
  Serial.println("Centering...");
  line(0,0,90);
}


//------------------------------------------------------------------------------
static void where() {
  Serial.print("X");
  Serial.print(posx);
  Serial.print(" Y");
  Serial.print(posy);
  Serial.print(" Z");
  Serial.print(posz);
  Serial.print(" F");
  printFeedRate();
  Serial.print("\n");
}


//------------------------------------------------------------------------------
static void printConfig() {
  Serial.print(m1d);          Serial.print("=");  
  Serial.print(limit_top);    Serial.print(",");
  Serial.print(limit_left);   Serial.print("\n");
  Serial.print(m2d);          Serial.print("=");  
  Serial.print(limit_top);    Serial.print(",");
  Serial.print(limit_right);  Serial.print("\n");
  Serial.print("Bottom=");    Serial.println(limit_bottom);
  Serial.print("Feed rate=");    printFeedRate();
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
void EEPROM_writeLong(int ee, long value) {
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
float EEPROM_readLong(int ee) {
  long value = 0;
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


//------------------------------------------------------------------------------
static void LoadConfig() {
  char version=EEPROM.read(0);
  if(version==1) {
    // update the version number
    robot_uid=0;
    EEPROM.write(0,2);
    SaveUID();
  } else if(version==2) {
    robot_uid=EEPROM_readLong(1);
  } else {
    // update the version number
    robot_uid=0;
    EEPROM.write(0,2);
    SaveUID();
  }
}


//------------------------------------------------------------------------------
static void SaveUID() {
  EEPROM_writeLong(1,robot_uid);
}


//------------------------------------------------------------------------------
static void processCommand() {
#ifndef SMALL_FOOTPRINT
  if(!strncmp(buffer,"HELP",4)) {
    help();
  } else if(!strncmp(buffer,"UID",3)) {
    robot_uid=atol(strchr(buffer,' ')+1);
    SaveUID();
  } else if(!strncmp(buffer,"G28",4)) {
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
  if(!strncmp(buffer,"M114",4)) {
    where();
  } else if(!strncmp(buffer,"CONFIG",6)) {
    float tt=limit_top;
    float bb=limit_bottom;
    float rr=limit_right;
    float ll=limit_left;
    
    char gg=m1d;
    char hh=m2d;
    
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      ptr=strchr(ptr,' ')+1;
      switch(*ptr) {
      case 'T': tt=atof(ptr+1);  break;
      case 'B': bb=atof(ptr+1);  break;
      case 'R': rr=atof(ptr+1);  break;
      case 'L': ll=atof(ptr+1);  break;
      case 'G': gg=*(ptr+1);  break;
      case 'H': hh=*(ptr+1);  break;
      case 'I': {
        if(atoi(ptr+1)>0) {
          M1_REEL_IN=FORWARD;
          M1_REEL_OUT=BACKWARD;
        } else {
          M1_REEL_IN=BACKWARD;
          M1_REEL_OUT=FORWARD;
        }
      }  break;
      case 'J':  {
        if(atoi(ptr+1)>0) {
          M2_REEL_IN=FORWARD;
          M2_REEL_OUT=BACKWARD;
        } else {
          M2_REEL_IN=BACKWARD;
          M2_REEL_OUT=FORWARD;
        }
      }  break;
      default: ptr=0; break;
      }
    }
    
    // @TODO: check t>b, r>l ?
    limit_top=tt;
    limit_bottom=bb;
    limit_right=rr;
    limit_left=ll;
    m1d=gg;
    m2d=hh;
    
    teleport(0,0);
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
  } else if(!strncmp(buffer,"D00 ",4)) {
    // move one motor
    char *ptr=strchr(buffer,' ')+1;
    int amount = atoi(ptr+1);
    int i, dir;
    if(*ptr == m1d) {
      dir = amount < 0 ? M1_REEL_IN : M1_REEL_OUT;
      amount=abs(amount);
      for(i=0;i<amount;++i) {  m1.step(1,dir);  delay(2);  }
    } else if(*ptr == m2d) {
      dir = amount < 0 ? M2_REEL_IN : M2_REEL_OUT;
      amount = abs(amount);
      for(i=0;i<amount;++i) {  m2.step(1,dir);  delay(2);  }
    }
  }  else if(!strncmp(buffer,"D01 ",4)) {
    // move one motor
    char *ptr=strchr(buffer,' ')+1;
    float amount = atof(ptr+1);
    adjustSpoolDiameter(amount);
  } else {
    char *ptr=buffer;
    while(ptr && ptr<buffer+sofar) {
      if(!strncmp(ptr,"G20",3)) {
        mode_scale=24.5/10.0;  // inches -> cm
        strcpy(mode_name,"in");
        printFeedRate();
      } else if(!strncmp(ptr,"G21",3)) {
        mode_scale=0.1;  // mm -> cm
        strcpy(mode_name,"mm");
        printFeedRate();
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
  LoadConfig();
  
  // start communications
  Serial.begin(BAUD);
  Serial.print("\n\nHELLO WORLD! I AM DRAWBOT #");
  Serial.println(robot_uid);

  // initialize the scale
  strcpy(mode_name,"mm");
  mode_scale=0.1;
  
  adjustSpoolDiameter(0.950);
  setFeedRate(MAX_VEL*30/mode_scale);  // *30 because i also /2
  
  // initialize the read buffer
  sofar=0;
  
  // servo should be on SER1, pin 10.
  s1.attach(SERVO_PIN);

  // turn on the pull up resistor
  digitalWrite(L_PIN,HIGH);
  digitalWrite(R_PIN,HIGH);
  
  // display the help at startup.
  help();

  // initialize the plotter position.
  teleport(0,0);
  velx=0;
  velx=0;
  posz=PEN_UP_ANGLE;
  setPenAngle(PEN_UP_ANGLE);
  
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

