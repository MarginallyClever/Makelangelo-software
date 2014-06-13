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
// Saving config
#include <EEPROM.h>
#include <Arduino.h>  // for type definitions


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
void EEPROM_writeLong(int ee, long value) {
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);
}


//------------------------------------------------------------------------------
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
float EEPROM_readLong(int ee) {
  long value = 0;
  byte* p = (byte*)(void*)&value;
  for (int i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


//------------------------------------------------------------------------------
void LoadConfig() {
  char version_number=EEPROM.read(ADDR_VERSION);
  if(version_number!=EEPROM_VERSION) {
    // If not the current EEPROM_VERSION or the EEPROM_VERSION is sullied (i.e. unknown data)
    // Update the version number
    EEPROM.write(ADDR_VERSION,EEPROM_VERSION);
    // Update robot uuid
    robot_uid=0;
    SaveUID();
    // Update spool diameter variables
    SaveSpoolDiameter();
  }
  
  if(version_number==3) {
    // Retrieve Stored Configuration
    robot_uid=EEPROM_readLong(ADDR_UUID);
    adjustSpoolDiameter((float)EEPROM_readLong(ADDR_SPOOL_DIA1)/10000.0f);   //3 decimal places of percision is enough   
    // save the new data so the next load doesn't screw up one bobbin size
    SaveSpoolDiameter();
  } else if(version_number==4) {
    // Retrieve Stored Configuration
    robot_uid=EEPROM_readLong(ADDR_UUID);
    adjustSpoolDiameter((float)EEPROM_readLong(ADDR_SPOOL_DIA1)/10000.0f);   //3 decimal places of percision is enough
  } else if(version_number==5) {
    
  } else {
    // Code should not get here if it does we should display some meaningful error message
    Serial.println(F("An Error Occurred during LoadConfig"));
  }
}


//------------------------------------------------------------------------------
void SaveUID() {
  EEPROM_writeLong(ADDR_UUID,(long)robot_uid);
}

//------------------------------------------------------------------------------
void SaveSpoolDiameter() {
  EEPROM_writeLong(ADDR_SPOOL_DIA1,SPOOL_DIAMETER*10000);
  EEPROM_writeLong(ADDR_SPOOL_DIA2,SPOOL_DIAMETER*10000);
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
