package Filters;


import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import Makelangelo.Makelangelo;


public class Filter_ScanlineGenerator extends Filter {
	// image preprocessing
	boolean dither_first=false;
	
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		int i;
		int x,y;
		double leveladd = 255.0/2.0;
		double level=leveladd;
		int z=0;

		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.Process(img);
		
		if(dither_first) {
			Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
			img = dither.Process(img);
		}

		Makelangelo.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		
		ImageStart(img,out);

		int steps = (int)Math.ceil(tool.GetDiameter()/(1.75*scale));
		if(steps<1) steps=1;
		
		// set absolute coordinates
		out.write("G00 G90;\n");
		
		tool.WriteChangeTo(out);
		liftPen(out);
		lastup=true;
		previous_x=0;
		previous_y=0;

		Makelangelo.getSingleton().Log("<font color='green'>Generating layer 1</font>\n");
		// create horizontal lines across the image
		// raise and lower the pen to darken the appropriate areas
		i=0;
		for(y=0;y<image_height;y+=steps) {
			++i;
			if((i%2)==0) {
				MoveTo(out,(float)          0,(float)y,true);
				for(x=0;x<image_width;++x) {
					z=TakeImageSample(img,x,y);
					MoveTo(out,(float)x,(float)y,( z >= level ));
				}
				MoveTo(out,(float)image_width,(float)y,true);
			} else {
				MoveTo(out,(float)image_width,(float)y,true);
				for(x=image_width-1;x>=0;--x) {
					z=TakeImageSample(img,x,y);
					MoveTo(out,(float)x,(float)y,( z >= level ));
				}
				MoveTo(out,(float)          0,(float)y,true);
			}
		}
		level+=leveladd;
		
		// lift pen and return to home
		liftPen(out);
		tool.WriteMoveTo(out, 0, 0);
		out.close();
		
		// TODO Move to GUI
		Makelangelo.getSingleton().Log("<font color='green'>Completed.</font>\n");
		Makelangelo.getSingleton().PlayConversionFinishedSound();
		Makelangelo.getSingleton().LoadGCode(dest);
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