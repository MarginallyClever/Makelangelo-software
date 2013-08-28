
/**
 * A base class for image filtering
 * @author Dan
 */
public class Filter {
	protected int decode(int pixel) {
		//pixel=(int)( Math.min(Math.max(pixel, 0),255) );
		float r = ((pixel>>16)&0xff);
		float g = ((pixel>> 8)&0xff);
		float b = ((pixel    )&0xff);
		return (int)( (r+g+b)/3 );
	}
	
	
	protected int encode(int i) {
		return (0xff<<24) | (i<<16) | (i<< 8) | i;
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */