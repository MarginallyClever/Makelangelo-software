import java.awt.image.BufferedImage;

	
/**
 * Converts an image to a fixed number of black & white levels - eg shades of grey.
 * @author Dan
 *
 */
public class Filter_BlackAndWhite extends Filter {
	int levels=2;

	Filter_BlackAndWhite(int _levels) {
		levels = _levels-1;
	}
	
	public BufferedImage Process(BufferedImage img) {
		int h = img.getHeight();
		int w = img.getWidth();
		int x,y,i;

		double max_intensity=-1000;
		double min_intensity=1000;
		double ilevels;
		
		if( levels != 0 ) ilevels = 1.0 / levels;
		else ilevels = 1;
		
		for(y=0;y<h;++y) {
			for(x=0;x<w;++x) {
				i=decode(img.getRGB(x, y));
				if(max_intensity<i) max_intensity=i;
				if(min_intensity>i) min_intensity=i;
			}
		}
		System.out.println("min_intensity="+min_intensity);
		System.out.println("max_intensity="+max_intensity);
		
		for(y=0;y<h;++y) {
			for(x=0;x<w;++x) {
				i=decode(img.getRGB(x, y));
				
				double a = (double)(i - min_intensity) / (float)(max_intensity - min_intensity);
				double c = Math.floor( a * levels ) * ilevels;
				int b = (int)( c * 255.0 );
				if(b>255) b=255;
				if(b<0) b=0;
				//if(b==255) System.out.println(x+"\t"+y+"\t"+i+"\t"+b);
				img.setRGB(x, y, encode(b));
			}
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