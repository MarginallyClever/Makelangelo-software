//------------------------------------------------------------------------------
// Draw robot - switch test
// dan@marginallycelver.com 2012 may 06
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/MarginallyClever/Makelangelo for more information.


//------------------------------------------------------------------------------
// CONSTANTS
//------------------------------------------------------------------------------
#define CUTOFF (512)


//------------------------------------------------------------------------------
// VARIABLES
//------------------------------------------------------------------------------
char a,b;
long c=0;


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------


//------------------------------------------------------------------------------
// print the state of each button
void ps() {
  Serial.print(c++);
  Serial.print("\t");
  Serial.print(a==0?"Off":"On");
  Serial.print("\t");
  Serial.println(b==0?"Off":"On");
}


//------------------------------------------------------------------------------
void setup() {
  Serial.begin(57600);
  
  digitalWrite(A0,HIGH);
  digitalWrite(A1,HIGH);
  digitalWrite(A2,HIGH);
  digitalWrite(A3,HIGH);
  digitalWrite(A4,HIGH);
  
  Serial.print("T");
  Serial.print("\t");
  Serial.print("L");
  Serial.print("\t");
  Serial.println("R");

  a=analogRead(3)<CUTOFF;
  b=analogRead(5)<CUTOFF;
  ps();
}


//------------------------------------------------------------------------------
void loop() {
  char a1=analogRead(3)<CUTOFF;
  char b1=analogRead(5)<CUTOFF;
  
  if(a1!=a){
    a=a1;
    ps();
  }
  if(b1!=b) {
    b=b1;
    ps();
  }
  delay(2);
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
