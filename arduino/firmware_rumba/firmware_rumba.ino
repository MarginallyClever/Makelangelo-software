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
#include "configure.h"
#include "Vector3.h"


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------

// robot UID
int robot_uid=0;

// plotter limits
// all distances are relative to the calibration point of the plotter.
// (normally this is the center of the drawing area)
float limit_top = 0;  // distance to top of drawing area.
float limit_bottom = 0;  // Distance to bottom of drawing area.
float limit_right = 0;  // Distance to right of drawing area.
float limit_left = 0;  // Distance to left of drawing area.

// what are the motors called?
char m1d='L';
char m2d='R';

// calculate some numbers to help us find feed_rate
float SPOOL_DIAMETER = 3.2;  // cm

float MAX_VEL = 0;  // cm/s
float THREAD_PER_STEP=0;


// plotter position.
float posx, posy, posz;  // pen state
float feed_rate=0;

// motor position
volatile long laststep[NUM_AXIES];

char absolute_mode=1;  // absolute or incremental programming mode?

// Serial comm reception
char buffer[MAX_BUF];  // Serial buffer
int sofar;             // Serial buffer progress

Vector3 tool_offset[NUM_TOOLS];
int current_tool=0;

//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------


//------------------------------------------------------------------------------
// calculate max velocity, threadperstep.
void adjustSpoolDiameter(float diameter1) {
  SPOOL_DIAMETER = diameter1;
  float SPOOL_CIRC = SPOOL_DIAMETER*PI;  // circumference
  THREAD_PER_STEP = SPOOL_CIRC/STEPS_PER_TURN;  // thread per step
}


//------------------------------------------------------------------------------
// returns angle of dy/dx as a value from 0...2PI
float atan3(float dy,float dx) {
  float a=atan2(dy,dx);
  if(a<0) a=(PI*2.0)+a;
  return a;
}


//------------------------------------------------------------------------------
char readSwitches() {
#ifdef USE_LIMIT_SWITCH
  // get the current switch state
  return ( (analogRead(L_PIN) < SWITCH_HALF) | (analogRead(R_PIN) < SWITCH_HALF) );
#else
  return 0;
#endif  // USE_LIMIT_SWITCH
}


//------------------------------------------------------------------------------
// feed rate is given in units/min and converted to cm/s
void setFeedRate(float v1) {
  if( feed_rate != v1 ) {
    feed_rate = v1;
    if(feed_rate > MAX_FEEDRATE) feed_rate = MAX_FEEDRATE;
    if(feed_rate < MIN_FEEDRATE) feed_rate = MIN_FEEDRATE;
#ifdef VERBOSE
    Serial.print(F("F="));
    Serial.println(feed_rate);
#endif
  }
}


//------------------------------------------------------------------------------
void pause(long ms) {
  delay(ms / 1000);
  delayMicroseconds(ms % 1000);
}


//------------------------------------------------------------------------------
void printFeedRate() {
  Serial.print(F("F"));
  Serial.print(feed_rate);
  Serial.print(F("steps/s"));
}


//------------------------------------------------------------------------------
// Inverse Kinematics - turns XY coordinates into lengths L1,L2
void IK(float x, float y, float &l1, float &l2) {
  // find length to M1
  float dy = y - limit_top;
  float dx = x - limit_left;
  l1 = floor( sqrt(dx*dx+dy*dy) / THREAD_PER_STEP );
  // find length to M2
  dx = x - limit_right;
  l2 = floor( sqrt(dx*dx+dy*dy) / THREAD_PER_STEP );
}


//------------------------------------------------------------------------------
// Forward Kinematics - turns L1,L2 lengths into XY coordinates
// use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
// to find angle between M1M2 and M1P where P is the plotter position.
void FK(float l1, float l2,float &x,float &y) {
  float a = l1 * THREAD_PER_STEP;
  float b = (limit_right-limit_left);
  float c = l2 * THREAD_PER_STEP;
  
  // slow, uses trig
  // we know law of cosines:   cc = aa + bb -2ab * cos( theta )
  // or cc - aa - bb = -2ab * cos( theta )
  // or ( aa + bb - cc ) / ( 2ab ) = cos( theta );
  // or theta = acos((aa+bb-cc)/(2ab));
  //x = cos(theta)*l1 + limit_left;
  //y = sin(theta)*l1 + limit_top;
  // and we know that cos(acos(i)) = i
  // and we know that sin(acos(i)) = sqrt(1-i*i)
  float theta = ((a*a+b*b-c*c)/(2.0*a*b));
  x = theta * a + limit_left;
  y = limit_top - (sqrt( 1.0 - theta * theta ) * a);
}


//------------------------------------------------------------------------------
void processConfig() {
  limit_top=parsenumber('T',limit_top);
  limit_bottom=parsenumber('B',limit_bottom);
  limit_right=parsenumber('R',limit_right);
  limit_left=parsenumber('L',limit_left);
  
  char gg=parsenumber('G',m1d);
  char hh=parsenumber('H',m2d);
  char i=parsenumber('I',0);
  char j=parsenumber('J',0);
  if(i!=0) {
    if(i>0) {
      motors[0].reel_in  = HIGH;
      motors[0].reel_out = LOW;
    } else {
      motors[0].reel_in  = LOW;
      motors[0].reel_out = HIGH;
    }
  }
  if(j!=0) {
    if(j>0) {
      motors[1].reel_in  = HIGH;
      motors[1].reel_out = LOW;
    } else {
      motors[1].reel_in  = LOW;
      motors[1].reel_out = HIGH;
    }
  }
  
  // @TODO: check t>b, r>l ?
  printConfig();
  
  teleport(0,0);
/*
  test_kinematics(0,0);
  test_kinematics(10,0);
  test_kinematics(-10,0);
  test_kinematics(0,10);
  test_kinematics(0,-10);
  test_kinematics(10,10);
  test_kinematics(-6.2,0.41);
  test_kinematics(-8,-3);
  test_kinematics(9.24,-7.55);
*/
}


// test FK(IK(x,y))=x,y
void test_kinematics(float x,float y) {
  float A, B, C, D;
  IK(x,y,A,B);
  FK(A,B,C,D);
  Serial.print(F("\tx="));  Serial.print(x);
  Serial.print(F("\ty="));  Serial.print(y);
  Serial.print(F("\tL="));  Serial.print(A);
  Serial.print(F("\tR="));  Serial.print(B);
  Serial.print(F("\tx'="));  Serial.print(C);
  Serial.print(F("\ty'="));  Serial.print(D);
  Serial.print(F("\tdx="));  Serial.print(C-x);
  Serial.print(F("\tdy="));  Serial.println(D-y);
}


//------------------------------------------------------------------------------
void line_safe(float x,float y,float z) {
  // split up long lines to make them straighter?
  float dx=x-posx;
  float dy=y-posy;
  float dz=z-posz;
/*
  Serial.print("dx ");  Serial.println(dx);
  Serial.print("dy ");  Serial.println(dy);
  Serial.print("dz ");  Serial.println(dz);

  Serial.print("posx ");  Serial.println(posx);
  Serial.print("posy ");  Serial.println(posy);
  Serial.print("posz ");  Serial.println(posz);
*/
  float len=sqrt(dx*dx+dy*dy);
//  Serial.print("LEN ");  Serial.println(len);
  
  long pieces=floor(len/CM_PER_SEGMENT);
//  Serial.print("pieces ");  Serial.println(pieces);
  
  float x0=posx;
  float y0=posy;
  float z0=posz;
  float a;
  for(long j=0;j<pieces;++j) {
    a=(float)j/(float)pieces;
//  Serial.print("a ");  Serial.println(a);

    polargraph_line(dx*a+x0,
                    dy*a+y0,
                    dz*a+z0);
  }
  polargraph_line(x,y,z);
}


//------------------------------------------------------------------------------
void polargraph_line(float x,float y,float z) {
  float l1,l2;
  IK(x,y,l1,l2);
  posx=x;
  posy=y;
  posz=z;
  motor_line(l1,l2,z,feed_rate);
}


//------------------------------------------------------------------------------
// This method assumes the limits have already been checked.
// This method assumes the start and end radius match.
// This method assumes arcs are not >180 degrees (PI radians)
// cx/cy - center of circle
// x/y - end position
// dir - ARC_CW or ARC_CCW to control direction of arc
void arc(float cx,float cy,float x,float y,float z,float dir) {
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
    line_safe(nx,ny,nz);
  }
  
  line_safe(x,y,z);
}


//------------------------------------------------------------------------------
// instantly move the virtual plotter position
// does not check if the move is valid
void teleport(float x,float y) {
  posx=x;
  posy=y;
  
  // @TODO: posz?
  float L1,L2;
  IK(posx,posy,L1,L2);
  
  motor_set_step_count(L1,L2,0);
}


//------------------------------------------------------------------------------
void help() {
  Serial.print(F("\n\nHELLO WORLD! I AM DRAWBOT #"));
  Serial.println(robot_uid);
  Serial.println(F("== DRAWBOT - http://www.makelangelo.com/ =="));
  Serial.println(F("All commands end with a semi-colon."));
  Serial.println(F("HELP;  - display this message"));
  Serial.println(F("CONFIG [Tx.xx] [Bx.xx] [Rx.xx] [Lx.xx];"));
  Serial.println(F("       - display/update this robot's configuration."));
  Serial.println(F("TELEPORT [Xx.xx] [Yx.xx]; - move the virtual plotter."));
  Serial.println(F("As well as the following G-codes (http://en.wikipedia.org/wiki/G-code):"));
  Serial.println(F("G00,G01,G02,G03,G04,G28,G90,G91,M18,M114"));
}


//------------------------------------------------------------------------------
// find the current robot position and 
void FindHome() {
#ifdef USE_LIMIT_SWITCH
  Serial.println(F("Homing..."));
  
  if(readSwitches()) {
    Serial.println(F("** ERROR **"));
    Serial.println(F("Problem: Plotter is already touching switches."));
    Serial.println(F("Solution: Please unwind the strings a bit and try again."));
    return;
  }
  
  int safe_out=50;

  // reel in the left motor until contact is made.
  Serial.println(F("Find left..."));
  digitalWrite(motors[0].dir_pin,HIGH);
  digitalWrite(motors[1].dir_pin,LOW);
  do {
    digitalWrite(motors[0].step_pin,HIGH);
    digitalWrite(motors[0].step_pin,LOW);
    digitalWrite(motors[1].step_pin,HIGH);
    digitalWrite(motors[1].step_pin,LOW);
    pause(STEP_DELAY);
  } while(!readSwitches());
  laststep1=0;
  
  // back off so we don't get a false positive on the next motor
  int i;
  digitalWrite(motors[0].dir_pin,LOW);
  for(i=0;i<safe_out;++i) {
    digitalWrite(motors[0].step_pin,HIGH);
    digitalWrite(motors[0].step_pin,LOW);
    pause(STEP_DELAY);
  }
  laststep1=safe_out;
  
  // reel in the right motor until contact is made
  Serial.println(F("Find right..."));
  digitalWrite(motors[0].dir_pin,LOW);
  digitalWrite(motors[1].dir_pin,HIGH);
  do {
    digitalWrite(motors[0].step_pin,HIGH);
    digitalWrite(motors[0].step_pin,LOW);
    digitalWrite(motors[1].step_pin,HIGH);
    digitalWrite(motors[1].step_pin,LOW);
    pause(STEP_DELAY);
    laststep1++;
  } while(!readSwitches());
  laststep2=0;
  
  // back off so we don't get a false positive that kills line()
  digitalWrite(motors[1].dir_pin,LOW);
  for(i=0;i<safe_out;++i) {
    digitalWrite(motors[1].step_pin,HIGH);
    digitalWrite(motors[1].step_pin,LOW);
    pause(STEP_DELAY);
  }
  laststep2=safe_out;
  
  Serial.println(F("Centering..."));
  line(0,0,posz);
#endif // USE_LIMIT_SWITCH
}


//------------------------------------------------------------------------------
void where() {
  Serial.print(F("X"));
  Serial.print(posx);
  Serial.print(F(" Y"));
  Serial.print(posy);
  Serial.print(F(" Z"));
  Serial.print(posz);
  Serial.print(F(" "));
  printFeedRate();
}


//------------------------------------------------------------------------------
void printConfig() {
  Serial.print(limit_left);       Serial.print(F(","));
  Serial.print(limit_top);        Serial.print(F(" - "));
  Serial.print(limit_right);     Serial.print(F(","));
  Serial.print(limit_bottom);      Serial.print(F("\n"));
}




//------------------------------------------------------------------------------
void set_tool_offset(int axis,float x,float y,float z) {
  tool_offset[axis].x=x;
  tool_offset[axis].y=y;
  tool_offset[axis].z=z;
}


//------------------------------------------------------------------------------
Vector3 get_end_plus_offset() {
  return Vector3(tool_offset[current_tool].x + posx,
                 tool_offset[current_tool].y + posy,
                 tool_offset[current_tool].z + posz);
}


//------------------------------------------------------------------------------
void tool_change(int tool_id) {
  if(tool_id < 0) tool_id=0;
  if(tool_id > NUM_TOOLS) tool_id=NUM_TOOLS;
  current_tool=tool_id;
}


/**
 * Look for character /code/ in the buffer and read the float that immediately follows it.
 * @return the value found.  If nothing is found, /val/ is returned.
 * @input code the character to look for.
 * @input val the return value if /code/ is not found.
 **/
float parsenumber(char code,float val) {
  char *ptr=buffer;
  while(ptr && *ptr && ptr<buffer+sofar) {
    if(*ptr==code) {
      return atof(ptr+1);
    }
    ptr=strchr(ptr,' ')+1;
  }
  return val;
}


//------------------------------------------------------------------------------
void processCommand() {
  // blank lines
  if(buffer[0]==';') return;
  
  if(!strncmp(buffer,"UID",3)) {
    robot_uid=atoi(strchr(buffer,' ')+1);
    SaveUID();
  } else if(!strncmp(buffer,"TELEPORT",8)) {
    teleport(parsenumber('X',posx),
             parsenumber('Y',posy));
  } else if(!strncmp(buffer,"CONFIG",6)) {
    processConfig();
  } 

  int cmd=parsenumber('M',-1);
  switch(cmd) {
  case 6:  tool_change(parsenumber('T',current_tool));  break;
  case 18:  motor_enable();  break;
  case 17:  motor_disable();  break;
  case 100:  help();  break;
  case 114:  where();  break;
  }

  cmd=parsenumber('G',-1);
  switch(cmd) {
  case 0:
  case 1: {  // line
      Vector3 offset=get_end_plus_offset();
      setFeedRate(parsenumber('F',feed_rate));
      line_safe( parsenumber('X',(absolute_mode?offset.x:0)*10)*0.1 + (absolute_mode?0:offset.x),
                 parsenumber('Y',(absolute_mode?offset.y:0)*10)*0.1 + (absolute_mode?0:offset.y),
                 parsenumber('Z',(absolute_mode?offset.z:0)) + (absolute_mode?0:offset.z) );
      break;
    }
  case 2:
  case 3: {  // arc
      Vector3 offset=get_end_plus_offset();
      setFeedRate(parsenumber('F',feed_rate));
      arc(parsenumber('I',(absolute_mode?offset.x:0))*0.1 + (absolute_mode?0:offset.x),
          parsenumber('J',(absolute_mode?offset.y:0))*0.1 + (absolute_mode?0:offset.y),
          parsenumber('X',(absolute_mode?offset.x:0))*0.1 + (absolute_mode?0:offset.x),
          parsenumber('Y',(absolute_mode?offset.y:0))*0.1 + (absolute_mode?0:offset.y),
          parsenumber('Z',(absolute_mode?offset.z:0)) + (absolute_mode?0:offset.z),
          (cmd==2) ? -1 : 1);
      break;
    }
  case 4:  {  // dwell
      wait_for_empty_segment_buffer();
      pause(parsenumber('S',0) + parsenumber('P',0)*1000.0f);
      break;
    }
  case 28:  FindHome();  break;
  case 54:
  case 55:
  case 56:
  case 57:
  case 58:
  case 59: {  // 54-59 tool offsets
    int tool_id=cmd-54;
    set_tool_offset(tool_id,parsenumber('X',tool_offset[tool_id].x),
                            parsenumber('Y',tool_offset[tool_id].y),
                            parsenumber('Z',tool_offset[tool_id].z));
    break;
    }
  case 90:  absolute_mode=1;  break;  // absolute mode
  case 91:  absolute_mode=0;  break;  // relative mode
  case 92: {  // set position (teleport)
      Vector3 offset = get_end_plus_offset();
      teleport( parsenumber('X',offset.x),
                         parsenumber('Y',offset.y)
                         //,
                         //parsenumber('Z',offset.z)
                         );
      break;
    }
  }

  cmd=parsenumber('D',-1);
  switch(cmd) {
  case 0: {
      // move one motor
      int i,amount=parsenumber(m1d,0);
      digitalWrite(motors[0].dir_pin,amount < 0 ? motors[0].reel_in : motors[0].reel_out);
      amount=abs(amount);
      for(i=0;i<amount;++i) {
        digitalWrite(motors[0].step_pin,HIGH);
        digitalWrite(motors[0].step_pin,LOW);
        pause(STEP_DELAY);
      }
      
      amount=parsenumber(m2d,0);
      digitalWrite(motors[1].dir_pin,amount < 0 ? motors[1].reel_in : motors[1].reel_out);
      amount = abs(amount);
      for(i=0;i<amount;++i) {
        digitalWrite(motors[1].step_pin,HIGH);
        digitalWrite(motors[1].step_pin,LOW);
        pause(STEP_DELAY);
      }
    }
    break;
  case 1: {
      // adjust spool diameters
      float amountL=parsenumber('L',SPOOL_DIAMETER);
  
      float tps1=STEPS_PER_TURN;
      adjustSpoolDiameter(amountL);
      if(STEPS_PER_TURN != tps1) {
        // Update EEPROM
        SaveSpoolDiameter();
      }
    }
    break;
  case 2:
    Serial.print('L');  Serial.print(SPOOL_DIAMETER);
    Serial.print(F(" R"));   Serial.println(SPOOL_DIAMETER);
    break;
  case 4:  SD_StartPrintingFile(strchr(buffer,' ')+1);  break;  // read file
  }
}


/**
 * prepares the input buffer to receive a new message and tells the serial connected device it is ready for more.
 */
void ready() {
  sofar=0;  // clear input buffer
  Serial.print(F("\n> "));  // signal ready to receive input
}


//------------------------------------------------------------------------------
void setup() {
  LoadConfig();
  
  // start communications
  Serial.begin(BAUD);
  
  // display the help at startup.
  help();
  
  motor_setup();
  motor_enable();
  
  //easyPWM_init();
  SD_init();
  LCD_init();
  
  // initialize the plotter position.
  teleport(0,0);
  setPenAngle(PEN_UP_ANGLE);
  setFeedRate((MAX_FEEDRATE+MIN_FEEDRATE)/2);
  ready();
}


//------------------------------------------------------------------------------
// See: http://www.marginallyclever.com/2011/10/controlling-your-arduino-through-the-serial-monitor/
void Serial_listen() {
  // listen for serial commands
  while(Serial.available() > 0) {
    buffer[sofar++]=Serial.read();
    if(buffer[sofar-1]==';') break;  // in case there are multiple instructions
  }
 
  // if we hit a semi-colon, assume end of instruction.
  if(sofar>0 && buffer[sofar-1]==';') {
    buffer[sofar]=0;
    
    // echo confirmation
//    Serial.println(F(buffer));
 
    // do something with the command
    processCommand();
    ready();
  } 
}


//------------------------------------------------------------------------------
void loop() {
  Serial_listen();
  SD_check();
  LCD_update();
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
