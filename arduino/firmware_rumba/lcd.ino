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
#endif


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
#ifdef HAS_LCD
LiquidCrystal lcd(LCD_PINS_RS, LCD_PINS_ENABLE, LCD_PINS_D4, LCD_PINS_D5, LCD_PINS_D6, LCD_PINS_D7);
long lcd_delay=0;
#endif



//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

// initialize the Smart controller LCD panel
void LCD_init() {
#ifdef HAS_LCD
  lcd.begin(LCD_WIDTH, LCD_HEIGHT);
#endif
}


/**
 * Display text on the LCD panel starting at the given coordinates
 * @input x starting column
 * @input y starting row
 * @input str null-terminated string of text.  must not be longer than one row on the LCD.
 */
void LCD_print(int x,int y,char *str) {
#ifdef HAS_LCD
  lcd.setCursor(x, y);
  lcd.print(str);
#endif
}


// display the current machine position and feedrate on the LCD.
void LCD_where() {
#ifdef HAS_LCD
  long t=millis();
  if(lcd_delay<t+200) {
    lcd_delay=t;
    lcd.setCursor(0, 0);    lcd.print("                ");
    lcd.setCursor(0, 0);    lcd.print('X');  lcd.print((long)posx);
    lcd.setCursor(0, 1);    lcd.print("                ");
    lcd.setCursor(0, 1);    lcd.print('Y');  lcd.print((long)posy);
    lcd.setCursor(0, 2);    lcd.print("                ");
    lcd.setCursor(0, 2);    lcd.print('Z');  lcd.print((long)posz);
    lcd.setCursor(0, 3);    lcd.print("                ");
    lcd.setCursor(0, 3);    lcd.print('F');  lcd.print((long)feed_rate);
  }
#endif
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
