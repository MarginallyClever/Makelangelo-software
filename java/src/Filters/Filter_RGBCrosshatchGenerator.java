package Filters;



import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ProgressMonitor;

import Makelangelo.MachineConfiguration;
import Makelangelo.Makelangelo;
import Makelangelo.Point2D;

/** 
 * @author Dan
 */
public class Filter_RGBCrosshatchGenerator extends Filter {
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		int i,j,x,y,z=0;
		double level=255.0/2.0f;

		Makelangelo.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		
		ImageStart(img,out);
		
		int steps = (int)Math.ceil(2.5*tool.GetDiameter()/scale);
		if(steps<1) steps=1;
		
		// set absolute coordinates
		out.write("G00 G90;\n");
		tool.WriteChangeTo(out);
		liftPen(out);
		lastup=true;
		previous_x=0;
		previous_y=0;
		
		boolean allow_black = false;
		boolean allow_red = true;
		boolean allow_green = true;
		boolean allow_blue = true;

		if(allow_black) {
			color_channel=0;
			Makelangelo.getSingleton().Log("<font color='green'>Generating layer 1 (black)</font>\n");
			// create horizontal lines across the image
			// raise and lower the pen to darken the appropriate areas
			i=0;
			for(y=0;y<image_height;y+=steps) {
				++i;
				if((i%2)==0) {
					MoveTo(out,          0,y,true);
					for(x=0;x<image_width;++x) {
						z=TakeImageSample(img,x,y);
						MoveTo(out,x,y,( z > level ));
					}
					MoveTo(out,image_width,y,true);
				} else {
					MoveTo(out,image_width,y,true);
					for(x=image_width-1;x>=0;--x) {
						z=TakeImageSample(img,x,y);
						MoveTo(out,x,y,( z > level ));
					}
					MoveTo(out,          0,y,true);
				}
			}
		}

		if(allow_red) {
			color_channel=1;
			liftPen(out);
			tool.WriteChangeTo(out);
	
			Makelangelo.getSingleton().Log("<font color='green'>Generating layer 2 (green)</font>\n");
			// create vertical lines across the image
			// raise and lower the pen to darken the appropriate areas
			i=0;
			for(x=0;x<image_width;x+=steps) {
				++i;
				if((i%2)==0) {
					MoveTo(out,x,0           ,true);
					for(y=0;y<image_height;++y) {
						z=TakeImageSample(img,x,y);
						MoveTo(out,x,y,( z > level ));
					}
					MoveTo(out,x,image_height,true);
				} else {
					MoveTo(out,x,image_height,true);
					for(y=image_height-1;y>=0;--y) {
						z=TakeImageSample(img,x,y);
						MoveTo(out,x,y,( z > level ));
					}
					MoveTo(out,x,0           ,true);
				}
			}
		}

		if(allow_green) {
			color_channel=2;
			liftPen(out);
			tool.WriteChangeTo(out);
	
			Makelangelo.getSingleton().Log("<font color='green'>Generating layer 3 (green)</font>\n");
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
					MoveTo(out,startx,starty,true);
					for(j=0;j<=delta;++j) {
						z=TakeImageSample(img,startx+j,starty+j);
						MoveTo(out,(startx+j),(starty+j),( z > level ) );
					}
					MoveTo(out,endx,endy,true);
				} else {
					MoveTo(out,endx,endy,true);
					for(j=0;j<=delta;++j) {
						z=TakeImageSample(img,endx-j,endy-j);
						MoveTo(out,(endx-j),(endy-j),( z > level ) );
					}
					MoveTo(out,startx,starty,true);
				}
				++i;
			}
		}
		
		if(allow_blue) {
			color_channel=3;
			liftPen(out);
			tool.WriteChangeTo(out);
	
			Makelangelo.getSingleton().Log("<font color='green'>Generating layer 4 (blue)</font>\n");
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
					MoveTo(out,startx,starty,true);
					for(j=0;j<=delta;++j) {
						z=TakeImageSample(img,startx-j,starty+j);
						MoveTo(out,(startx-j),(starty+j),( z > level ) );
					}
					MoveTo(out,endx,endy,true);
				} else {
					MoveTo(out,endx,endy,true);
					for(j=0;j<delta;++j) {
						z=TakeImageSample(img,endx+j,endy-j);
						MoveTo(out,(endx+j),(endy-j),( z > level ) );
					}
					MoveTo(out,startx,starty,true);
				}
			}
		}

		liftPen(out);
		SignName(out);
		tool.WriteMoveTo(out, 0, 0);
		out.close();
		
		// TODO Move to GUI
		Makelangelo.getSingleton().Log("<font color='green'>Completed.</font>\n");
		Makelangelo.getSingleton().PlayConversionFinishedSound();
		Makelangelo.getSingleton().LoadGCode(dest);
	}
	
	
	protected void SignName(OutputStreamWriter out) throws IOException {
		TextSetAlign(Align.RIGHT);
		TextSetVAlign(VAlign.BOTTOM);
		TextSetPosition(image_width, image_height);
		TextCreateMessageNow("Makelangelo #"+Long.toString(MachineConfiguration.getSingleton().GetUID()),out);
		//TextCreateMessageNow("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890<>,?/\"':;[]!@#$%^&*()_+-=\\|~`{}.",out);
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