package Filters;


import Makelangelo.Makelangelo;

import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
public class Filter_GeneratorCrosshatch extends Filter {
	public String GetName() { return "Crosshatch"; }
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		int i,j;
		int x,y;
		double leveladd = 255.0/6.0;
		double level=leveladd;
		int z=0;

		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255); 
		img = bw.Process(img);

		Makelangelo.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		
		ImageStart(img,out);
		
		int steps = (int)Math.ceil(2.5*tool.GetDiameter()/scale);
		if(steps<1) steps=1;
		
		// set absolute coordinates
		out.write("G00 G90;\n");
		tool.WriteChangeTo(out);
		liftPen(out);

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


		Makelangelo.getSingleton().Log("<font color='green'>Generating layer 2</font>\n");
		// create vertical lines across the image
		// raise and lower the pen to darken the appropriate areas
		i=0;
		for(x=0;x<image_width;x+=steps) {
			++i;
			if((i%2)==0) {
				MoveTo(out,(float)x,(float)0           ,true);
				for(y=0;y<image_height;++y) {
					z=TakeImageSample(img,x,y);
					MoveTo(out,(float)x,(float)y,( z >= level ));
				}
				MoveTo(out,(float)x,(float)image_height,true);
			} else {
				MoveTo(out,(float)x,(float)image_height,true);
				for(y=image_height-1;y>=0;--y) {
					z=TakeImageSample(img,x,y);
					MoveTo(out,(float)x,(float)y,( z >= level ));
				}
				MoveTo(out,(float)x,(float)0           ,true);
			}
		}
		level+=leveladd;


		Makelangelo.getSingleton().Log("<font color='green'>Generating layer 3</font>\n");
		// create diagonal \ lines across the image
		// raise and lower the pen to darken the appropriate areas
		i=0;
		for(x=-(image_height-1);x<image_width;x+=steps) {
			int endx=image_height-1+x;
			int endy=image_height-1;
			if(endx >= image_width) {
				endy -= endx - (image_width-1);
				endx = image_width-1;
			}
			int startx=x;
			int starty=0;
			if( startx < 0 ) {
				starty -= startx;
				startx=0;
			}
			int delta=endy-starty;
			
			if((i%2)==0)
			{
				MoveTo(out,(float)startx,(float)starty,true);
				for(j=0;j<=delta;++j) {
					z=TakeImageSample(img,startx+j,starty+j);
					MoveTo(out,(float)(startx+j),(float)(starty+j),( z >= level ) );
				}
				MoveTo(out,(float)endx,(float)endy,true);
			} else {
				MoveTo(out,(float)endx,(float)endy,true);
				for(j=0;j<=delta;++j) {
					z=TakeImageSample(img,endx-j,endy-j);
					MoveTo(out,(float)(endx-j),(float)(endy-j),( z >= level ) );
				}
				MoveTo(out,(float)startx,(float)starty,true);
			}
			++i;
		}
		level+=leveladd;


		Makelangelo.getSingleton().Log("<font color='green'>Generating layer 4</font>\n");
		// create diagonal / lines across the image
		// raise and lower the pen to darken the appropriate areas
		i=0;
		for(x=0;x<image_width+image_height;x+=steps) {
			int endx=0;
			int endy=x;
			if( endy >= image_height ) {
				endx += endy - (image_height-1);
				endy = image_height-1;
			}
			int startx=x;
			int starty=0;
			if( startx >= image_width ) {
				starty += startx - (image_width-1);
				startx=image_width-1;
			}
			int delta=endy-starty;
			
			assert( (startx-endx) == (starty-endy) );

			++i;
			if((i%2)==0) {
				MoveTo(out,(float)startx,(float)starty,true);
				for(j=0;j<=delta;++j) {
					z=TakeImageSample(img,startx-j,starty+j);
					MoveTo(out,(float)(startx-j),(float)(starty+j),( z > level ) );
				}
				MoveTo(out,(float)endx,(float)endy,true);
			} else {
				MoveTo(out,(float)endx,(float)endy,true);
				for(j=0;j<delta;++j) {
					z=TakeImageSample(img,endx+j,endy-j);
					MoveTo(out,(float)(endx+j),(float)(endy-j),( z > level ) );
				}
				MoveTo(out,(float)startx,(float)starty,true);
			}
		}

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