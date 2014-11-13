package Filters;


import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;


public class Filter_GeneratorScanline extends Filter {
	public String GetName() { return "Scanline"; }
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.Process(img);

		// Open the destination file
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		// Set up the conversion from image space to paper space, select the current tool, etc.
		ImageStart(img,out);
		// "please change to tool X and press any key to continue"
		tool.WriteChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		// figure out how many lines we're going to have on this image.
		int steps = (int)Math.ceil(tool.GetDiameter()/(1.75*scale));
		if(steps<1) steps=1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level=255.0/2.0;

		// from top to bottom of the image...
		int x,y,z,i=0;
		for(y=0;y<image_height;y+=steps) {
			++i;
			if((i%2)==0) {
				// every even line move left to right
				
				//MoveTo(file,x,y,pen up?)
				MoveTo(out,(float)0,(float)y,true);
				for(x=0;x<image_width;++x) {
					// read the image at x,y
					z=TakeImageSample(img,x,y);
					MoveTo(out,(float)x,(float)y,( z > level ));
				}
				MoveTo(out,(float)image_width,(float)y,true);
			} else {
				// every odd line move right to left
				MoveTo(out,(float)image_width,(float)y,true);
				for(x=image_width-1;x>=0;--x) {
					z=TakeImageSample(img,x,y);
					MoveTo(out,(float)x,(float)y,( z > level ));
				}
				MoveTo(out,(float)0,(float)y,true);
			}
		}

		// pen already lifted
		SignName(out);
		tool.WriteMoveTo(out, 0, 0);
		
		// close the file
		out.close();
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