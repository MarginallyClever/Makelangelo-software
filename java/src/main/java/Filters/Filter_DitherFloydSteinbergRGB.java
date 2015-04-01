package Filters;

import java.awt.image.BufferedImage;
import Makelangelo.C3;


/**
 * Floyd/Steinberg dithering 
 * @author Dan
 * @see {@link http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering}
 */
public class Filter_DitherFloydSteinbergRGB extends Filter {
	C3 [] palette = new C3[] {
		new C3(0,0,0),
		new C3(255,0,0),
		new C3(0,255,0),
		new C3(0,0,255),
		new C3(255,255,255),
	};
	
	C3 QuantizeColor(C3 c) {
		C3 closest = palette[0];

	    for (C3 n : palette) 
	      if (n.diff(c) < closest.diff(c))
	        closest = n;

	    return closest;
	}
	
	private void DitherDirection(BufferedImage img,int y,C3[] error,C3[] nexterror,int direction) {
		int w = img.getWidth();
		C3 oldPixel = new C3(0,0,0);
		C3 newPixel = new C3(0,0,0);
		C3 quant_error = new C3(0,0,0);
		int start, end, x;

		for(x=0;x<w;++x) nexterror[x].set(0,0,0);
		
		if(direction>0) {
			start=0;
			end=w;
		} else {
			start=w-1;
			end=-1;
		}
		
		// for each x from left to right
		for(x=start;x!=end;x+=direction) {
			// oldpixel := pixel[x][y]
			oldPixel.set( new C3(img.getRGB(x, y)).add(error[x]) );
			// newpixel := find_closest_palette_color(oldpixel)
			newPixel = QuantizeColor(oldPixel);
			// pixel[x][y] := newpixel
			img.setRGB(x, y, newPixel.toInt());
			// quant_error := oldpixel - newpixel
			quant_error.set( oldPixel.sub( newPixel ) );
			// pixel[x+1][y  ] += 7/16 * quant_error
			// pixel[x-1][y+1] += 3/16 * quant_error
			// pixel[x  ][y+1] += 5/16 * quant_error
			// pixel[x+1][y+1] += 1/16 * quant_error
				nexterror[x          ].add(quant_error.mul(5.0/16.0));
			if(x+direction>=0 && x+direction < w) {
				    error[x+direction].add(quant_error.mul(7.0/16.0));
				nexterror[x+direction].add(quant_error.mul(1.0/16.0));
			}
			if(x-direction>=0 && x-direction < w) {
				nexterror[x-direction].add(quant_error.mul(3.0/16.0));
			}
		}
	}
	
	
	public BufferedImage Process(BufferedImage img) {
		int y;
		int h = img.getHeight();
		int w = img.getWidth();
		int direction=1;
		C3 [] error=new C3[w];
		C3 [] nexterror=new C3[w];
		
		for(y=0;y<w;++y) {
			error[y] = new C3(0,0,0);
			nexterror[y] = new C3(0,0,0);
		}
		
		// for each y from top to bottom
		for(y=0;y<h;++y) {
			DitherDirection(img,y,error,nexterror,direction);
			
			direction = -direction;
			C3 [] tmp = error;
			error=nexterror;
			nexterror=tmp;
		}
		
		return img;
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