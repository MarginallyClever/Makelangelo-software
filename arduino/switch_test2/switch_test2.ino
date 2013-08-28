//------------------------------------------------------------------------------
// switch test 2
// dan@marginallycelver.com 2012 may 06
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/MarginallyClever/Makelangelo for more information.

#define S1_PIN (A3)
#define S2_PIN (A5)
#define SWITCH_HALF (512)

int switch1,switch2;

static void readSwitches() {
  // get the current switch state
  switch1=analogRead(S1_PIN) > SWITCH_HALF;
  switch2=analogRead(S2_PIN) > SWITCH_HALF;
}


void setup() {
  digitalWrite(S1_PIN,HIGH);
  digitalWrite(S2_PIN,HIGH);
}


void loop() {
  readSwitches();
  
  digitalWrite(13,(switch1&switch2) ? HIGH : LOW );
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

