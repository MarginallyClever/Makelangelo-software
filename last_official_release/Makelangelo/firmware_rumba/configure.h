#ifndef CONFIGURE_H
#define CONFIGURE_H
//------------------------------------------------------------------------------
// Makelangelo - supports raprapdiscount RUMBA controller
// dan@marginallycelver.com 2013-12-26
// RUMBA should be treated like a MEGA 2560 Arduino.
//------------------------------------------------------------------------------
// Copyright at end of file.  Please see
// http://www.github.com/MarginallyClever/Makelangelo for more information.

//------------------------------------------------------------------------------
// CONSTANTS
//------------------------------------------------------------------------------
//#define VERBOSE           (1)  // add to get a lot more serial output.
#define HAS_SD  // comment this out if there is no SD card
#define HAS_LCD  // comment this out if there is no SMART LCD controller
//#define USE_LIMIT_SWITCH  (1)  // Comment out this line to disable findHome and limit switches

// machine style
#define POLARGRAPH2  // uncomment this line if you use a polargraph like the Makelangelo
//#define COREXY  // uncomment this line if you use a CoreXY setup.
//#define TRADITIONALXY  // uncomment this line if you use a traditional XY setup.


// servo angles for pen control
#define PEN_UP_ANGLE         (80)
#define PEN_DOWN_ANGLE       (10)  // Some steppers don't like 0 degrees

// for serial comms
#define BAUD                 (115200)  // How fast is the Arduino talking?
#define MAX_BUF              (64)  // What is the longest message Arduino can store?


#define MICROSTEPS           (16.0)  // microstepping on this microcontroller
#define STEPS_PER_TURN       (400 * MICROSTEPS)  // default number of steps per turn * microsteps

#define MAX_FEEDRATE         (30000.0)  // depends on timer interrupt & hardware
#define MIN_FEEDRATE         (1000)
#define DEFAULT_FEEDRATE     (8500.0)
#define DEFAULT_ACCELERATION (250)

#define STEP_DELAY           (150)  // delay between steps, in microseconds, when doing fixed tasks like homing

#define NUM_AXIES            (3)  // x,y,z
#define NUM_TOOLS            (6)
#define MAX_SEGMENTS         (32)  // number of line segments to buffer ahead

// for arc directions
#define ARC_CW               (1)
#define ARC_CCW              (-1)
#define MM_PER_SEGMENT       (10)  // Arcs are split into many line segments.  How long are the segments?


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
#define LCD_HEIGHT         4
#define LCD_WIDTH          20
// Encoder rotation values
#define BTN_EN1            11
#define BTN_EN2            12
#define BTN_ENC            43
#define BLEN_C             2
#define BLEN_B             1
#define BLEN_A             0
#define encrot0            0
#define encrot1            2
#define encrot2            3
#define encrot3            1

#define NUM_SERVOS         (1)
#define SERVO0_PIN         (5)
#define SERVO1_PIN         (4)


#define MOTHERBOARD 1 // RUMBA
//#define MOTHERBOARD 2 // RAMPS

#if MOTHERBOARD == 1
#define MOTOR_0_DIR_PIN           (16)
#define MOTOR_0_STEP_PIN          (17)
#define MOTOR_0_ENABLE_PIN        (48)
#define MOTOR_0_LIMIT_SWITCH_PIN  (37)

#define MOTOR_1_DIR_PIN           (47)
#define MOTOR_1_STEP_PIN          (54)
#define MOTOR_1_ENABLE_PIN        (55)
#define MOTOR_1_LIMIT_SWITCH_PIN  (36)

// alternate pins in case you want to do something interesting
#define MOTOR_2_DIR_PIN           (56)
#define MOTOR_2_STEP_PIN          (57)
#define MOTOR_2_ENABLE_PIN        (62)
#define MOTOR_2_LIMIT_SWITCH_PIN  (35)

#define MOTOR_3_DIR_PIN           (22)
#define MOTOR_3_STEP_PIN          (23)
#define MOTOR_3_ENABLE_PIN        (27)
#define MOTOR_3_LIMIT_SWITCH_PIN  (34)

#define MOTOR_4_DIR_PIN           (25)
#define MOTOR_4_STEP_PIN          (26)
#define MOTOR_4_ENABLE_PIN        (24)
#define MOTOR_4_LIMIT_SWITCH_PIN  (33)

#define MOTOR_5_DIR_PIN           (28)
#define MOTOR_5_STEP_PIN          (29)
#define MOTOR_5_ENABLE_PIN        (39)
#define MOTOR_5_LIMIT_SWITCH_PIN  (32)

#endif

#if MOTHERBOARD == 2
#endif


//------------------------------------------------------------------------------
// EEPROM MEMORY MAP
//------------------------------------------------------------------------------
#define EEPROM_VERSION   4                         // Increment EEPROM_VERSION when adding new variables
#define ADDR_VERSION     0                         // address of the version number (one byte)
#define ADDR_UUID        (ADDR_VERSION+1)          // address of the UUID (long - 4 bytes)
#define ADDR_SPOOL_DIA1  (ADDR_UUID+4)             // address of the spool diameter (float - 4 bytes)
#define ADDR_SPOOL_DIA2  (ADDR_SPOOL_DIA1+4)       // address of the spool diameter (float - 4 bytes)


//------------------------------------------------------------------------------
// TIMERS
//------------------------------------------------------------------------------
// for timer interrupt control
#define CLOCK_FREQ            (16000000L)
#define MAX_COUNTER           (65536L)
// time passed with no instruction?  Make sure PC knows we are waiting.
#define TIMEOUT_OK            (1000)

// optimize code, please
#define FORCE_INLINE         __attribute__((always_inline)) inline


#ifndef CRITICAL_SECTION_START
  #define CRITICAL_SECTION_START  unsigned char _sreg = SREG;  cli();
  #define CRITICAL_SECTION_END    SREG = _sreg;
#endif //CRITICAL_SECTION_START


//------------------------------------------------------------------------------
// STRUCTS
//------------------------------------------------------------------------------
// for line()
typedef struct {
  long step_count;
  long delta;  // number of steps to move
  long absdelta;
  int dir;
  float delta_normalized;
} Axis;


typedef struct {
  int step_pin;
  int dir_pin;
  int enable_pin;
  int limit_switch_pin;
  int limit_switch_state;
  int reel_in;
  int reel_out;
} Motor;


typedef struct {
  Axis a[NUM_AXIES];
  int steps_total;
  int steps_taken;
  int accel_until;
  int decel_after;
  unsigned short feed_rate_max;
  unsigned short feed_rate_start;
  unsigned short feed_rate_start_max;
  unsigned short feed_rate_end;
  char nominal_length_flag;
  char recalculate_flag;
  char busy;
} Segment;



//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

extern Segment line_segments[MAX_SEGMENTS];
extern Segment *working_seg;
extern volatile int current_segment;
extern volatile int last_segment;
extern float acceleration;


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
extern Motor motors[NUM_AXIES];


#endif // CONFIGURE_H
