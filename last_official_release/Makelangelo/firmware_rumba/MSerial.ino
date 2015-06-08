//------------------------------------------------------------------------------
// Makelangelo - supports raprapdiscount RUMBA controller
// dan@marginallycelver.com 2013-12-26
// RUMBA should be treated like a MEGA 2560 Arduino.
//------------------------------------------------------------------------------
// Copyright at end of file.  Please see
// http://www.github.com/MarginallyClever/Makelangelo for more information.
/*
//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
#include <avr/interrupt.h> 
#include <avr/io.h> 

//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------

void serial_setup(long baud) {
  UBRR0H = ( baud & 0xFF00) >> 8; // Load upper 8-bits of the baud rate value into the high byte of the UBRR register 
  UBRR0L = ( baud & 0x00FF); // Load lower 8-bits of the baud rate value into the low byte of the UBRR register 
  UCSR0C |= (1 << UCSZ00) | (1 << UCSZ10); // Use 8-bit character sizes 
  //UCSR0B |= (1 << RXEN0) | (1 << TXEN0) | (1 << RXCIE0);   // Turn on the transmission, reception, and Receive interrupt   
  UCSR0B |= (1 << RXEN0);
  interrupts();
}


// serial receive interrupt
ISR(USART0_RX_vect) {
}
*/


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
