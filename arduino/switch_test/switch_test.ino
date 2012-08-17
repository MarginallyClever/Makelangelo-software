//------------------------------------------------------------------------------
// Draw robot - switch test
// dan@marginallycelver.com 2012 may 06
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.github.com/i-make-robots/Drawbot for more information.


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



//------------------------------------------------------------------------------
// Copyright (C) 2012 Dan Royer (dan@marginallyclever.com)
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//------------------------------------------------------------------------------


