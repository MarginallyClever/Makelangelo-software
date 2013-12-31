//------------------------------------------------------------------------------
// Draw robot - supports raprapdiscount RUMBA controller
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


#define LCD_STATUS_MENU   (0)
#define LCD_MAIN_MENU     (1)

#define LCD_TIMEOUT_DELAY        (3000)
#define LCD_DRAW_DELAY           (50)
#define LCD_STEPS_PER_MENU_ITEM  (5)

//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
LiquidCrystal lcd(LCD_PINS_RS, LCD_PINS_ENABLE, LCD_PINS_D4, LCD_PINS_D5, LCD_PINS_D6, LCD_PINS_D7);
long lcd_draw_delay = 0;
int lcd_menu = LCD_STATUS_MENU;
int lcd_menu_opt = 0;

int lcd_rot_old=0;
char lcd_click_old=LOW;

int lcd_turn = 0;
char lcd_click_now = false;
long lcd_timeout;
int lcd_turn_sum=0;

//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

// initialize the Smart controller LCD panel
void LCD_init() {
  lcd.begin(LCD_WIDTH, LCD_HEIGHT);
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


void LCD_menu() {
  LCD_read();

  // menu display
  char draw_now=0;
  if( millis() > lcd_draw_delay) {
    lcd_draw_delay=millis() + LCD_DRAW_DELAY;
    draw_now=1;
  }
  
  // menu logic
  switch(lcd_menu) {
  case 0: LCD_status(draw_now);  break;
  case 1: LCD_main_menu(draw_now);  break;
  }
}


// display the current machine position and feedrate on the LCD.
void LCD_status( char draw_now ) {
  if( lcd_click_now ) {
    lcd_menu = LCD_MAIN_MENU;
    lcd_menu_opt=0;
    lcd_timeout=millis() + LCD_TIMEOUT_DELAY;
  }
  
  lcd_turn_sum += lcd_turn;
  lcd_turn=0;
  
  if( draw_now != 0 ) {
    float d1=laststep[0];
    float d2=laststep[1];
    float px,py;
    FK(d1,d2,px,py);
    lcd.setCursor(0, 0);  lcd.print("                    ");
    lcd.setCursor(0, 1);  lcd.print("                    ");
    lcd.setCursor(0, 2);  lcd.print("                    ");
    lcd.setCursor(0, 3);  lcd.print("                    ");
    
    lcd.setCursor( 0, 0);  lcd.print('X');  lcd.print(px);
    lcd.setCursor(10, 0);  lcd.print('Y');  lcd.print(py);
    lcd.setCursor( 0, 1);  lcd.print('Z');  lcd.print(posz);
    lcd.setCursor(10, 1);  lcd.print('F');  lcd.print(feed_rate);
  }
}


void LCD_main_menu( char draw_now ) {
  if( lcd_turn ) {
    lcd_timeout = millis() + LCD_TIMEOUT_DELAY;
    lcd_menu_opt += lcd_turn;
    if( lcd_menu_opt<0 ) lcd_menu_opt=0;
    if( lcd_menu_opt>3*LCD_STEPS_PER_MENU_ITEM ) lcd_menu_opt=3*LCD_STEPS_PER_MENU_ITEM;
    lcd_turn=0;
  }
  if( lcd_click_now ) {
    lcd_timeout = millis() + LCD_TIMEOUT_DELAY;
    switch(lcd_menu_opt/LCD_STEPS_PER_MENU_ITEM) {
    }
  }
  if( millis() > lcd_timeout ) {
    lcd_menu = LCD_STATUS_MENU;
  }
  
  if( draw_now != 0 ) {
    lcd.setCursor(0, 0);  lcd.print(" Pause              ");
    lcd.setCursor(0, 1);  lcd.print(" Stop               ");
    lcd.setCursor(0, 2);  lcd.print(" Start              ");
    lcd.setCursor(0, 3);  lcd.print(" Drive              ");
    lcd.setCursor(0, lcd_menu_opt/LCD_STEPS_PER_MENU_ITEM);  lcd.print('>');
  }
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
