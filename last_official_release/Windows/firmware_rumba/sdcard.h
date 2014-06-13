#ifndef SDCARD_H
#define SDCARD_H


#include <SD.h>

#ifdef HAS_SD

extern File root;
extern char sd_inserted;
extern char sd_printing_now;
extern char sd_printing_paused;
extern float sd_percent_complete;
#endif


#endif // SDCARD_H
