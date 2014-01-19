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


#define LCD_DRAW_DELAY           (150)


// Convenience macros that make it easier to generate menus

#define MENU_START \
  int menu_position=0, screen_position=0, num_menu_items=0, ty, screen_end; \
  lcd.clear(); \
  do { \
    LCD_read(); \
    if( lcd_turn ) { \
      menu_position += lcd_turn > 0 ? 1 : -1; \
      digitalWrite(13,state); \
      state=(state==HIGH?LOW:HIGH); \
      lcd_turn=0; \
      lcd.clear(); \
    } \
    if(menu_position>num_menu_items-1) menu_position=num_menu_items-1; \
    if(menu_position<0) menu_position=0; \
    if(screen_position>menu_position) screen_position=menu_position; \
    if(screen_position<menu_position-(LCD_HEIGHT-1)) screen_position=menu_position-(LCD_HEIGHT-1); \
    screen_end=screen_position+LCD_HEIGHT; \
    ty=0;

#define MENU_END \
    num_menu_items=ty; \
  } while(1);
  
#define MENU_ITEM_START(key) \
    if(ty>=screen_position && ty<screen_end) { \
      lcd.setCursor(0,ty-screen_position); \
      lcd.print((menu_position==ty)?'>':' '); \
      lcd.print(key); \

#define MENU_ITEM_END() \
    } \
    ++ty;
    
    
#define MENU_BACK() \
      MENU_ITEM_START("Back") \
      if(menu_position==ty && lcd_click_now) break; \
      MENU_ITEM_END()

#define MENU_SUBMENU(menu_label,menu_method) \
      MENU_ITEM_START(menu_label) \
      if(menu_position==ty && lcd_click_now) LCD_main_menu(); \
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

int lcd_rot_old=0;
int lcd_turn = 0;
char lcd_click_old=LOW;
char lcd_click_now = false;


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

// initialize the Smart controller LCD panel
void LCD_init() {
  lcd.begin(LCD_HEIGHT,LCD_WIDTH);
  pinMode(BTN_EN1,INPUT);
  pinMode(BTN_EN2,INPUT);
  pinMode(BTN_ENC,INPUT);
  digitalWrite(BTN_EN1,HIGH);
  digitalWrite(BTN_EN2,HIGH);
  digitalWrite(BTN_ENC,HIGH);
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
  lcd_click_now=false;
  
  int btn = digitalRead(BTN_ENC);
  if( btn != lcd_click_old && btn == LOW ) {
    lcd_click_now=true;
  }
  lcd_click_old = btn;
}


float c=0.1;
float d=100.2;
long e=500;
long aa=1;
int state=HIGH;


void LCD_menu() {
  MENU_START
    MENU_BACK();
    MENU_SUBMENU("main menu",LCD_main_menu);
    MENU_SUBMENU("status menu",LCD_status_menu);
    MENU_LONG("A",aa);
    MENU_FLOAT("C",c);
    MENU_FLOAT("D",d);
    MENU_LONG("E",e);
  MENU_END
}


// display the current machine position and feedrate on the LCD.
void LCD_status_menu() {
  MENU_START
    
    if( millis() > lcd_draw_delay) {
      lcd_draw_delay=millis() + LCD_DRAW_DELAY;
      
      float d1=laststep[0];
      float d2=laststep[1];
      float px,py;
      FK(d1,d2,px,py);
      px*=10;
      py*=10;
  
      lcd.clear();
      lcd.setCursor( 0, 0);  lcd.print('X');  LCD_print_float(px);
      lcd.setCursor(10, 0);  lcd.print('Y');  LCD_print_float(py);
      lcd.setCursor( 0, 1);  lcd.print('Z');  LCD_print_float(posz);
      lcd.setCursor(10, 1);  lcd.print('F');  LCD_print_float(feed_rate);
    }
  MENU_END
}


void LCD_main_menu() {
  MENU_START
    //if(not_started) {
    //MENU_SUBMENU("Start",LCD_start_menu);
    //} else {
    //MENU_ACTION("Pause",LCD_pause);
    //MENU_ACTION("Stop",LCD_stop);
    //}
    //MENU_SUBMENU("Drive",LCD_drive_menu);
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
  int right = (int)(v*100)%100;

  int x=1000;
  while(x>left && x>1) {
    lcd.print(' ');
    x/=10;
  };
  if(v>0) lcd.print(' ');
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
