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
#ifdef HAS_SD
#include <SD.h>
#endif


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
#ifdef HAS_SD
Sd2Card card;
SdVolume volume;
SdFile root;
int sd_inserted;
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
  
  sd_inserted = digitalRead(SDCARDDETECT) ? LOW : HIGH;
  SD_check();
#endif  // HAS_SD
}


// Load the SD card and read some info about it
void SD_load_card() {
#ifdef HAS_SD
  if (!card.init(SPI_HALF_SPEED, SDSS)) {
    Serial.println(F("SD card initialization failed."));
    return;
  }

  // Now we will try to open the 'volume'/'partition' - it should be FAT16 or FAT32
  if (!volume.init(card)) {
    Serial.println("Could not find FAT16/FAT32 partition.\nMake sure you've formatted the card");
    return;
  }

  // print the type of card
  switch(card.type()) {
    case SD_CARD_TYPE_SD1:    Serial.print("SD1");      break;
    case SD_CARD_TYPE_SD2:    Serial.print("SD2");      break;
    case SD_CARD_TYPE_SDHC:   Serial.print("SDHC");     break;
    default:                  Serial.print("Unknown");  break;
  }

  // print the type and size of the first FAT-type volume
  Serial.print(F(" FAT"));
  Serial.print(volume.fatType(), DEC);
 
  uint32_t volumesize;
  volumesize = volume.blocksPerCluster();    // clusters are collections of blocks
  volumesize *= volume.clusterCount();       // we'll have a lot of clusters
  volumesize *= 512;                         // SD card blocks are always 512 bytes
  Serial.print(F(", "));
  Serial.print(volumesize);
  Serial.println(" bytes.");
 
  root.openRoot(volume);
  root.ls();
#endif
}


// Check if the SD card has been added or removed
void SD_check() {
#ifdef HAS_SD
  int state=digitalRead(SDCARDDETECT);
  if(sd_inserted != state) {
    Serial.print("SD is ");
    if(state==HIGH) {
      Serial.println(F("removed"));
    } else {
      Serial.println(F("added"));
      SD_load_card();
    }
    sd_inserted = state;
  }
#endif
}


void SD_ProcessFile(char *filename) {
#ifdef HAS_SD
  File f=SD.open(filename);
  if(!f) {
    Serial.print(F("File "));
    Serial.print(filename);
    Serial.println(F(" not found."));
    return;
  }
  
  int c;
  while(f.peek() != -1) {
    c=f.read();
    if(c=='\n' || c=='\r') continue;
    buffer[sofar++]=c;
    if(buffer[sofar]==';') {
      // end string
      buffer[sofar]=0;
      // print for our benefit
      Serial.println(buffer);
      // process command
      processCommand();
      // reset buffer for next line
      sofar=0;
    }
  }
  
  f.close();
#endif // HAS_SD
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
