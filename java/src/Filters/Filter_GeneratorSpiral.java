package Filters;


import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import Makelangelo.Makelangelo;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
public class Filter_GeneratorSpiral extends Filter {
	public String GetName() { return "Spiral"; }
	
	boolean whole_image = false;  // draw the spiral right out to the edges of the square bounds.
	
	/**
	 * Overrides teh basic MoveTo() because optimizing for spirals is different logic than straight lines.
	 */
	protected void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		tool.WriteMoveTo(out, TX(x), TY(y));
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
	}
	
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255); 
		img = bw.Process(img);

		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		
		ImageStart(img,out);

		double toolDiameter=tool.GetDiameter()/scale;
		tool.WriteChangeTo(out);
		liftPen(out);

		//*
		// create a spiral across the image
		// raise and lower the pen to darken the appropriate areas
		
		// spiralize
		int x,y,i,j;
		final int steps=4;
		double leveladd = 255.0/5.0f;
		double level;
		int z=0;

		float maxr;
		if(whole_image) {
			// go right to the corners
			maxr = (float)(Math.sqrt( h2*h2 + w2*w2 ) + 1.0f);
		} else {
			// do the largest circle that still fits in the image.
			maxr = (h2>w2) ? w2 : h2;
		}
		maxr/=2;

		float r=maxr, d, f;
		float fx,fy;
		int numRings=0;
		double [] each_level = new double[steps];
		each_level[0]=leveladd*1;
		each_level[1]=leveladd*3;
		each_level[2]=leveladd*2;
		each_level[3]=leveladd*4;
		j=0;
		while(r>1) {
			d=r*2;
			++j;
			level = each_level[j%steps];
			// find circumference of current circle
			float circumference=(float) Math.floor(((d+d-toolDiameter)*Math.PI)/6.0f);

			for(i=0;i<=circumference;++i) {
				f = (float)Math.PI*2.0f*(i/circumference);
				fx = w2 + (float)(Math.cos(f)*d);
				fy = h2 + (float)(Math.sin(f)*d);
				x = (int)fx;
				y = (int)fy;
				// clip to image boundaries
				if( x>=0 && x<image_width && y>=0 && y<image_height ) {
					z=TakeImageSample(img,x,y);
					MoveTo(out,fx,fy,( z >= level ));
				} else {
					MoveTo(out,fx,fy,true);
				}
			}
			r-=toolDiameter*0.5;
			++numRings;
		}
		
		Makelangelo.getSingleton().Log("<font color='yellow'>"+numRings+" rings.</font>\n");

		liftPen(out);
		SignName(out);
		tool.WriteMoveTo(out, 0, 0);
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