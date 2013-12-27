#ifndef CONFIGURE_H
#define CONFIGURE_H
//------------------------------------------------------------------------------
// Draw robot - supports raprapdiscount RUMBA controller
// dan@marginallycelver.com 2013-12-26
// RUMBA should be treated like a MEGA 2560 Arduino.
//------------------------------------------------------------------------------
// Copyright at end of file.  Please see
// http://www.github.com/MarginallyClever/Makelangelo for more information.

//------------------------------------------------------------------------------
// CONSTANTS
//------------------------------------------------------------------------------
//#define VERBOSE              (1)  // add to get a lot more serial output.
#define HAS_SD  // comment this out if there is no SD card
#define HAS_LCD  // comment this out if there is no SMART LCD controller
//#define USE_LIMIT_SWITCH  (1)  // Comment out this line to disable findHome and limit switches


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
#define STEPS_PER_TURN  (400.0)
#define MAX_RPM         (200.0)

// delay between steps, in microseconds.
#define STEP_DELAY      (150)


// *****************************************************************************
// *** Don't change the constants below unless you know what you're doing.   ***
// *****************************************************************************


// servo angles for pen control
#define PEN_UP_ANGLE    (80)
#define PEN_DOWN_ANGLE  (10)  // Some steppers don't like 0 degrees
#define PEN_DELAY       (250)  // in ms

#define BAUD                 (57600)  // How fast is the Arduino talking?
#define MAX_BUF              (64)  // What is the longest message Arduino can store?

#define STEPS_PER_TURN       (400)  // depends on your stepper motor.  most are 200.
#define MAX_FEEDRATE         (200)
#define MIN_FEEDRATE         (0.01)

#define NUM_AXIES            (2)

#define CLOCK_FREQ           (16000000L)
#define MAX_COUNTER          (65536L)
#define MAX_SEGMENTS         (32)

// for arc directions
#define ARC_CW          (1)
#define ARC_CCW         (-1)
#define CM_PER_SEGMENT  (0.2)  // Arcs are split into many line segments.  How long are the segments?

// servo pin differs based on device
#define SERVO_PIN        10


#ifdef HAS_LCD
#define HAS_SD
#endif

// SD card settings
#define SDPOWER            -1
#define SDSS               53
#define SDCARDDETECT       49
// Smart controller settings
#define BEEPER             44
#define LCD_PINS_RS        19 
#define LCD_PINS_ENABLE    42
#define LCD_PINS_D4        18
#define LCD_PINS_D5        38 
#define LCD_PINS_D6        41
#define LCD_PINS_D7        40
#define LCD_HEIGHT         20
#define LCD_WIDTH          4
// Encoder rotation values
#define BTN_EN1            11
#define BTN_EN2            12
#define BTN_ENC            43
#define BLEN_C 2
#define BLEN_B 1
#define BLEN_A 0
#define encrot0 0
#define encrot1 2
#define encrot2 3
#define encrot3 1


//------------------------------------------------------------------------------
// EEPROM MEMORY MAP
//------------------------------------------------------------------------------
#define EEPROM_VERSION   4             // Increment EEPROM_VERSION when adding new variables
#define ADDR_VERSION     0             // address of the version number (one byte)
#define ADDR_UUID        1             // address of the UUID (long - 4 bytes)
#define ADDR_SPOOL_DIA1  5             // address of the spool diameter (float - 4 bytes)
#define ADDR_SPOOL_DIA2  9             // address of the spool diameter (float - 4 bytes)


//------------------------------------------------------------------------------
// STRUCTS
//------------------------------------------------------------------------------
// for line()
typedef struct {
  long step_count;
  long delta;  // number of steps to move
  long absdelta;
  long over;  // for dx/dy bresenham calculations
  int dir;
} Axis;


typedef struct {
  int step_pin;
  int dir_pin;
  int enable_pin;
  int limit_switch_pin;
  int limit_switch_state;
} Motor;


typedef struct {
  Axis a[NUM_AXIES];
  long steps;
  long steps_left;
  long feed_rate;
} Segment;



//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
extern Motor motors[NUM_AXIES];


#endif // CONFIGURE_H
