/*
 *
 *  Created on: 20 Dec 2013
 *  Author: Axel Ljungdahl
 *  Contact: mail@axel.pw
 *  Version 0.1
 *
 *  Software 50Hz PWM, fully compatible with Receiver.h.
 *  20 mS period, 1000 to 2000 µs pulsewidth.
 *  Uses PB0 to PB5 for a total of up to 6 outputs.
 *  Has a range of 2000 to 4000 (2000), where an increase of 1 equals half of a microsecond.
 *  If you want a 1000 to 2000 range instead, just left shift whatever value you have 1 step
 *
 *	CAN NOT RESET TIMER1 FOR ANY REASON
 *
 */
#ifndef EASYPWM_H_
#define EASYPWM_H_
#include <inttypes.h> //Fix for Arduino

#define PWM_MAXPULSE 4000
#define PWM_MINPULSE 2000
#define CHANNEL_DELAY 5000
#define EASYPWM_CHANNELS 6

void easyPWM_updateAngle(uint8_t channel, float angle);
void easyPWM_updatePulsewidth(uint8_t channel, uint16_t val);
void easyPWM_init();


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
#endif /* EASYPWM_H_ */
