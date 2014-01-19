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
 * TODO:
 * Could possibly adjust each value to acocunt for the calculations in the
 * interrupt routine, but I am satisfied with it as is.
 */
#include <avr/interrupt.h>
#include <avr/io.h>
#include "easyPWM.h"

volatile uint16_t restingValues[6];
volatile uint8_t flipFlopper = 0;
volatile uint8_t counter = 0;
uint16_t pulsewidth[] = { 2000, 2000, 2000, 2000, 2000, 2000 };

void easyPWM_init() {
	// TIMER START
	TCCR3A = 0; // Normal mode, no compare outputs
	TCCR3B = (TCCR3B & 0xFC) | (1 << CS11); // Set 8 prescaler
	TIMSK3 |= (1 << OCIE3A); //Enable interrupt on compare A for timer1
	// TIMER END
	DDRB |= 0b00111111; //PB0 to PB5 as outputs

	sei(); //Turn on global interrupts
}

void easyPWM_updateAngle(uint8_t channel, float angle) {
	if (channel >= 0 && channel < EASYPWM_CHANNELS && angle >= 0 && angle <= 180) {
		pulsewidth[channel] = ( angle * 2000.0 / 180.0 ) + 2000;
	}
}

void easyPWM_updatePulsewidth(uint8_t channel, uint16_t val) {
	if (channel >= 0 && channel < EASYPWM_CHANNELS && val >= 2000 && val <= 4000) {
		pulsewidth[channel] = val;
	}
}

ISR(TIMER3_COMPA_vect) {
	if (flipFlopper) {
		PORTB &= ~(1 << counter);
		//Here, the channel counter is done, so we go to the next, unless we have done all.
		//If we have done an update on all channels, we delay 20mS from channel 0's perspective (40k ticks from restingValue(0)).
		if( counter == EASYPWM_CHANNELS ) {
			OCR3A = restingValues[0] + 40000;
			counter = 0;
		} else {
			OCR3A = restingValues[counter] + CHANNEL_DELAY; //2500 µs so we have 1000 µs between pulses for safety.
			counter++;
		}
		flipFlopper ^= 0x01;
	} else {
		restingValues[counter] = TCNT3;
		PORTB |= (1 << counter);
		OCR3A = restingValues[counter] + pulsewidth[counter];
		flipFlopper ^= 0x01;
	}
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
