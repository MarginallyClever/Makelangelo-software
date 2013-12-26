import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ProgressMonitor;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
class Filter_4levelGcodeGenerator extends Filter {
	String dest;
	int numPoints;
	Point2D[] points = null;
	int image_width, image_height;
	int scount;
	boolean lastup;
	float w2,h2;
	ProgressMonitor pm;
	double scale,iscale;
	double margin;
	String previous_command;
	
	
	Filter_4levelGcodeGenerator(String _dest,double _scale,double _margin) {
		dest=_dest;
		scale=_scale;
		margin=_margin;
	}
	
	
	public void SetPaperLimits(double _paper_top, double _paper_bottom, double _paper_left, double _paper_right) {}
	public void SetMachineLimits(double _limit_top, double _limit_bottom, double _limit_left, double _limit_right) {}

	
	private double RoundOff(double value) {
		return value;//Math.round(value * 1000) / 1000;
	}
	
	private void MoveTo(BufferedWriter out,float x,float y,boolean up) throws IOException {
		String command="G00 X"+RoundOff((x-w2)*iscale) + " Y" + RoundOff((h2-y)*iscale)+"\n";
		if(up) {
			previous_command=command;
		}
		if(lastup!=up) {
			lastup=up;
			if(up) {
				// go faster with pen up
				out.write("G00 Z90 F2000\n");
			} else {
				out.write(previous_command);
				out.write("G00 Z0 F1000\n");
			}
		}
		if(!up) {
			out.write(command);
		}
	}
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Process(BufferedImage img) {
		image_height = img.getHeight();
		image_width = img.getWidth();
		int x,y,i,j;
		int steps=5;
		iscale=1.0/scale;
		w2=image_width/2;
		h2=image_height/2;
		double leveladd = 255.0/5.0;
		double level=leveladd;
		int z=0;
		
		DrawbotGUI.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(dest));
			// change to tool 0
			out.write("M06 T0\n");
			// set absolute coordinates
			out.write("G00 G90\n");
			// set a default feed rate
			out.write("G00 F1000\n");
			// lift pen
			out.write("G00 Z90\n");
			lastup=true;
			previous_command="";

			
			DrawbotGUI.getSingleton().Log("<font color='green'>Generating layer 1</font>\n");
			// create horizontal lines across the image
			// raise and lower the pen to darken the appropriate areas
			i=0;
			for(y=0;y<image_height;y+=steps) {
				++i;
				if((i%2)==0) {
					MoveTo(out,(float)          0,(float)y,true);
					for(x=0;x<image_width;++x) {
						z=decode(img.getRGB(x,y));
						MoveTo(out,(float)x,(float)y,( z >= level ));
					}
					MoveTo(out,(float)image_width,(float)y,true);
				} else {
					MoveTo(out,(float)image_width,(float)y,true);
					for(x=image_width-1;x>=0;--x) {
						z=decode(img.getRGB(x,y));
						MoveTo(out,(float)x,(float)y,( z >= level ));
					}
					MoveTo(out,(float)          0,(float)y,true);
				}
			}
			level+=leveladd;


			DrawbotGUI.getSingleton().Log("<font color='green'>Generating layer 2</font>\n");
			// create vertical lines across the image
			// raise and lower the pen to darken the appropriate areas
			i=0;
			for(x=0;x<image_width;x+=steps) {
				++i;
				if((i%2)==0) {
					MoveTo(out,(float)x,(float)0           ,true);
					for(y=0;y<image_height;++y) {
						z=decode(img.getRGB(x,y));
						MoveTo(out,(float)x,(float)y,( z >= level ));
					}
					MoveTo(out,(float)x,(float)image_height,true);
				} else {
					MoveTo(out,(float)x,(float)image_height,true);
					for(y=image_height-1;y>=0;--y) {
						z=decode(img.getRGB(x,y));
						MoveTo(out,(float)x,(float)y,( z >= level ));
					}
					MoveTo(out,(float)x,(float)0           ,true);
				}
			}
			level+=leveladd;


			DrawbotGUI.getSingleton().Log("<font color='green'>Generating layer 3</font>\n");
			// create diagonal \ lines across the image
			// raise and lower the pen to darken the appropriate areas
			i=0;
			for(x=-image_height;x<image_width;x+=steps) {
				int endx=image_height+x;
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
				
				if((i%2)==0) {
					MoveTo(out,(float)startx,(float)starty,true);
					for(j=0;j<delta;++j) {
						z=decode(img.getRGB(startx+j,starty+j));
						MoveTo(out,(float)(startx+j),(float)(starty+j),( z >= level ) );
					}
					MoveTo(out,(float)endx,(float)endy,true);
				} else {
					MoveTo(out,(float)endx,(float)endy,true);
					for(j=0;j<delta;++j) {
						z=decode(img.getRGB(endx-j,endy-j));
						MoveTo(out,(float)(endx-j),(float)(endy-j),( z >= level ) );
					}
					MoveTo(out,(float)startx,(float)starty,true);
				}
				++i;
			}
			level+=leveladd;


			DrawbotGUI.getSingleton().Log("<font color='green'>Generating layer 4</font>\n");
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
					for(j=0;j<delta;++j) {
						z=decode(img.getRGB(startx-j,starty+j));
						MoveTo(out,(float)(startx-j),(float)(starty+j),( z > level ) );
					}
					MoveTo(out,(float)endx,(float)endy,true);
				} else {
					MoveTo(out,(float)endx,(float)endy,true);
					for(j=0;j<delta;++j) {
						z=decode(img.getRGB(endx+j,endy-j));
						MoveTo(out,(float)(endx+j),(float)(endy-j),( z > level ) );
					}
					MoveTo(out,(float)startx,(float)starty,true);
				}
			}

			
			// lift pen and return to home
			out.write("G00 Z90\n");
			out.write("G00 X0 Y0\n");
			// Unpower Steppers
			out.write("M18\n");
			out.close();
		}
		catch(IOException e) {
			DrawbotGUI.getSingleton().Log("<font color='red'>Error saving "+dest+": "+e.getMessage()+"</font>");
		}
		
		// @TODO: Move to DrawbotGUI.getSingleton().ConversionFinished() ?
		DrawbotGUI.getSingleton().Log("<font color='green'>Completed.</font>\n");
		DrawbotGUI.getSingleton().PlayConversionFinishedSound();

		DrawbotGUI.getSingleton().LoadGCode(dest);
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