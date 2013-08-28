//------------------------------------------------------------------------------
// servo movement test
// dan@marginallycelver.com 2012 may 06
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/MarginallyClever/Makelangelo for more information.

// Adafruit motor driver library
#include <AFMotorDrawbot.h>

// Default servo library
#include <Servo.h> 

static Servo s1;


void setup() {
  s1.attach(10);
  s1.write(0);
  delay(2000);
}

void loop() {
    s1.write( 0 );
    delay(750);
    s1.write( 180 );
    delay(750);
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
