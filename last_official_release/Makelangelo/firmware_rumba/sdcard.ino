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
#include "sdcard.h"
#include <SD.h>

//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
#ifdef HAS_SD

File root;
char sd_inserted;
char sd_printing_now;
char sd_printing_paused;
File sd_print_file;
float sd_percent_complete;
long sd_file_size;
long sd_bytes_read;

#endif



//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

// initialize the SD card and print some info.
void SD_init() {
#ifdef HAS_SD
  pinMode(SDSS, OUTPUT);
  pinMode(SDCARDDETECT,INPUT);
  digitalWrite(SDCARDDETECT,HIGH);

  sd_inserted = false;
  sd_printing_now=false;
  sd_percent_complete=0;
  SD_check();
#endif  // HAS_SD
}


// Load the SD card and read some info about it
void SD_load_card() {
#ifdef HAS_SD
  SD.begin(SDSS);
  root = SD.open("/");
#endif
}


// Check if the SD card has been added or removed
void SD_check() {
#ifdef HAS_SD
  int state = (digitalRead(SDCARDDETECT) == LOW);
  if(sd_inserted != state) {
    Serial.print("SD is ");
    if(!state) {
      Serial.println(F("removed"));
      sd_printing_now=false;
    } else {
      Serial.println(F("added"));
      SD_load_card();
    }
    sd_inserted = state;
  }

  // read one line from the file.  don't read too fast or the LCD will appear to hang.
  if(sd_printing_now==true && sd_printing_paused==false && segment_buffer_full()==false ) {
    int c;
    while(sd_print_file.peek() != -1) {
      c=sd_print_file.read();
      buffer[sofar++]=c;
      sd_bytes_read++;
      if(c==';') {
        // eat to the end of the line
        while(sd_print_file.peek() != -1) {
          c=sd_print_file.read();
          sd_bytes_read++;
          if(c=='\n' || c=='\r') break;
        }
      }
      if(c=='\n' || c=='\r') {
        // update the % visible on the LCD.
        sd_percent_complete = (float)sd_bytes_read * 100.0 / (float)sd_file_size;

        // end string
        buffer[sofar]=0;
        // print for our benefit
        Serial.println(buffer);
        // process command
        processCommand();
        // reset buffer for next line
        ready();
        // quit this loop so we can update the LCD and listen for commands from the laptop (if any)
        break;
      }
    }

    if(sd_print_file.peek() == -1) {
      sd_print_file.close();
      sd_printing_now=false;
    }
  }
#endif // HAS_SD
}


void SD_StartPrintingFile(char *filename) {
#ifdef HAS_SD
  sd_print_file=SD.open(filename);
  if(!sd_print_file) {
    Serial.print(F("File "));
    Serial.print(filename);
    Serial.println(F(" not found."));
    return;
  }

  // count the number of lines (\n characters) for displaying % complete.
  sd_file_size=sd_print_file.size();
  sd_bytes_read=0;
  sd_percent_complete=0;

  // return to start
  sd_print_file.seek(0);

  sd_printing_now=true;
  sd_printing_paused=false;
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
