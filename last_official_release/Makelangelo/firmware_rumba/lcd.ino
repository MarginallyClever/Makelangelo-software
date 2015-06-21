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
#ifdef HAS_LCD
#include <LiquidCrystal.h>
#include "sdcard.h"


#define LCD_DRAW_DELAY    (150)
#define LCD_TURN_PER_MENU (5)


// Convenience macros that make it easier to generate menus

#define MENU_START \
  ty=0;

#define MENU_END \
  num_menu_items=ty; \
  
#define MENU_ITEM_START(key) \
  if(ty>=screen_position && ty<screen_end) { \
    lcd.setCursor(0,ty-screen_position); \
    lcd.print((menu_position==ty)?'>':' '); \
    lcd.print(key); \

#define MENU_ITEM_END() \
  } \
  ++ty;

#define MENU_GOTO(new_menu) {  lcd_click_now=false;  lcd.clear();  num_menu_items=screen_position=menu_position=menu_position_sum=0;  current_menu=new_menu; return;  }

#define MENU_SUBMENU(menu_label,menu_method) \
    MENU_ITEM_START(menu_label) \
    if(menu_position==ty && lcd_click_now) MENU_GOTO(menu_method); \
    MENU_ITEM_END()
    
#define MENU_ACTION(menu_label,menu_method) MENU_SUBMENU(menu_label,menu_method)

#define MENU_LONG(key,value) \
    MENU_ITEM_START(key) \
    LCD_print_long(value); \
    if(menu_position==ty && lcd_click_now) LCD_update_long(key,value); \
    MENU_ITEM_END()

#define MENU_FLOAT(key,value) \
    MENU_ITEM_START(key) \
    LCD_print_float(value); \
    if(menu_position==ty && lcd_click_now) LCD_update_float(key,value); \
    MENU_ITEM_END()


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
LiquidCrystal lcd(LCD_PINS_RS, LCD_PINS_ENABLE, LCD_PINS_D4, LCD_PINS_D5, LCD_PINS_D6, LCD_PINS_D7);
long lcd_draw_delay = 0;

int lcd_rot_old  = 0;
int lcd_turn     = 0;
char lcd_click_old = HIGH;
char lcd_click_now = false;
uint8_t speed_adjust = 100;

int menu_position_sum=0, menu_position=0, screen_position=0, num_menu_items=0, ty, screen_end;

void (*current_menu)();


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

// initialize the Smart controller LCD panel
void LCD_init() {
  lcd.begin(LCD_WIDTH,LCD_HEIGHT);
  pinMode(BTN_EN1,INPUT);
  pinMode(BTN_EN2,INPUT);
  pinMode(BTN_ENC,INPUT);
  digitalWrite(BTN_EN1,HIGH);
  digitalWrite(BTN_EN2,HIGH);
  digitalWrite(BTN_ENC,HIGH);
  current_menu=LCD_status_menu;
}


void LCD_read() {
  // detect pot turns
  int rot = ((digitalRead(BTN_EN1)==LOW)<<BLEN_A)
          | ((digitalRead(BTN_EN2)==LOW)<<BLEN_B);
  switch(rot) {
  case encrot0:
    if( lcd_rot_old == encrot3 ) lcd_turn++;
    if( lcd_rot_old == encrot1 ) lcd_turn--;
    break;
  case encrot1:
    if( lcd_rot_old == encrot0 ) lcd_turn++;
    if( lcd_rot_old == encrot2 ) lcd_turn--;
    break;
  case encrot2:
    if( lcd_rot_old == encrot1 ) lcd_turn++;
    if( lcd_rot_old == encrot3 ) lcd_turn--;
    break;
  case encrot3:
    if( lcd_rot_old == encrot2 ) lcd_turn++;
    if( lcd_rot_old == encrot0 ) lcd_turn--;
    break;
  }
  lcd_rot_old = rot;
  
  // find click state
  int btn = digitalRead(BTN_ENC);
  if( btn != lcd_click_old && btn == HIGH ) {
    lcd_click_now=true;
  }
  lcd_click_old = btn;
}


void LCD_update() {
  LCD_read();
 
  if(millis() >= lcd_draw_delay ) {
    lcd_draw_delay = millis() + LCD_DRAW_DELAY;

    (*current_menu)();
    
    if( lcd_turn ) {
      int op = menu_position_sum / LCD_TURN_PER_MENU;
      menu_position_sum += lcd_turn;
      lcd_turn=0;
      
      if(num_menu_items>0) {
        if( menu_position_sum > (num_menu_items-1) * LCD_TURN_PER_MENU ) menu_position_sum = (num_menu_items-1) * LCD_TURN_PER_MENU;
      }
      
      menu_position = menu_position_sum / LCD_TURN_PER_MENU;
      if(op != menu_position) lcd.clear();
    
      if(menu_position>num_menu_items-1) menu_position=num_menu_items-1;
      if(menu_position<0) menu_position=0;
      if(screen_position>menu_position) screen_position=menu_position;
      if(screen_position<menu_position-(LCD_HEIGHT-1)) screen_position=menu_position-(LCD_HEIGHT-1);
      screen_end=screen_position+LCD_HEIGHT;
    }
  }
}


// display the current machine position and feedrate on the LCD.
void LCD_status_menu() {
  MENU_START
    // on click go to the main menu
    if(lcd_click_now) MENU_GOTO(LCD_main_menu);

    if(lcd_turn) {
      speed_adjust += lcd_turn;
    }
    // update the current status
    lcd.setCursor( 0, 0);  lcd.print('X');  LCD_print_float(posx);
    lcd.setCursor(10, 0);  lcd.print('Y');  LCD_print_float(posy);
    lcd.setCursor( 0, 1);  lcd.print('Z');  LCD_print_float(posz);
    lcd.setCursor(10, 1);  lcd.print('F');  LCD_print_float(feed_rate);
    lcd.setCursor( 0, 2);  lcd.print(F("Makelangelo.com #"));  lcd.print(robot_uid);
    lcd.setCursor( 0, 3);  LCD_print_long(speed_adjust);  lcd.print(F("% "));
    if(sd_printing_now==true/* && sd_printing_paused==false*/) {
      LCD_print_float(sd_percent_complete);
      lcd.print('%');
    } else {
      lcd.print(F("          "));
    }
  MENU_END
}


void LCD_main_menu() {
  MENU_START
    MENU_SUBMENU("Back",LCD_status_menu);
    if(!sd_printing_now) {
      MENU_ACTION("Disable motors",LCD_disable_motors);
      MENU_ACTION("Enable motors",LCD_enable_motors);
      MENU_ACTION("This is home",LCD_this_is_home);
      MENU_ACTION("Go home",LCD_go_home);
      if(sd_inserted) {
        MENU_SUBMENU("Start from file...",LCD_start_menu);
      }
    } else {
      if(sd_printing_paused) {
        MENU_ACTION("Unpause",LCD_pause);
      } else {
        MENU_ACTION("Pause",LCD_pause);
      }
      MENU_ACTION("Stop",LCD_stop);
    }
    //MENU_SUBMENU("Drive",LCD_drive_menu);
  MENU_END
}


void LCD_pause() {
  sd_printing_paused = (sd_printing_paused==true?false:true);
  MENU_GOTO(LCD_main_menu);
}


void LCD_stop() {
  sd_printing_now=false;
  MENU_GOTO(LCD_main_menu);
}

void LCD_disable_motors() {
  motor_disable();
  MENU_GOTO(LCD_main_menu);
}

void LCD_enable_motors() {
  motor_enable();
  MENU_GOTO(LCD_main_menu);
}


void LCD_this_is_home() {
  teleport(0,0);
  MENU_GOTO(LCD_main_menu);
}


void LCD_go_home() {
  polargraph_line( 0, 0, posz, DEFAULT_FEEDRATE );
  MENU_GOTO(LCD_main_menu);
}


void LCD_start_menu() {
  if(!sd_inserted) MENU_GOTO(LCD_main_menu);

  MENU_START
    MENU_SUBMENU("Back",LCD_main_menu);
    
    root.rewindDirectory();
    while(1) {
      File entry =  root.openNextFile();
      if (!entry) {
        // no more files, return to the first file in the directory
        break;
      }
      if (!entry.isDirectory() && entry.name()[0]!='_') {
        MENU_ITEM_START(entry.name())
        if(menu_position==ty && lcd_click_now) {
          lcd_click_now=false;
          SD_StartPrintingFile(entry.name());
          MENU_GOTO(LCD_status_menu);
        }
        MENU_ITEM_END()
      }
      entry.close();
    }
  MENU_END
}


void LCD_update_long(char *name,long &value) {
  lcd.clear();
  do {
    LCD_read();
    if( lcd_turn ) {
      value+=lcd_turn>0?1:-1;
      lcd_turn=0;
    }
    lcd.setCursor(0,0);
    lcd.print(name);
    lcd.setCursor(0,1);
    LCD_print_long(value);
  } while( !lcd_click_now );
}


void LCD_update_float(char *name,float &value) {
  lcd.clear();
  do {
    LCD_read();
    if( lcd_turn ) {
      value += lcd_turn > 0 ? 0.01 : -0.01;
      lcd_turn=0;
    }
    lcd.setCursor(0,0);
    lcd.print(name);
    lcd.setCursor(0,1);
    LCD_print_float(value);
  } while( !lcd_click_now );
}


void LCD_print_long(long v) {
  long av=abs(v);
  int x=1000;
  while(x>av && x>1) {
    lcd.print(' ');
    x/=10;
  };
  if(v>0) lcd.print(' ');
  lcd.print(v);
}



void LCD_print_float(float v) {
  int left = abs(v);
  int right = abs((int)(v*100)%100);

  if(left<1000) lcd.print(' ');
  if(left<100) lcd.print(' ');
  if(left<10) lcd.print(' ');
  if(v<0) lcd.print('-');
  else lcd.print(' ');
  lcd.print(left);
  lcd.print('.');
  if(right<10) lcd.print('0');
  lcd.print(right);
}


#else
void LCD_init() {}
void LCD_menu() {}
#endif  // HAS_LCD

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
