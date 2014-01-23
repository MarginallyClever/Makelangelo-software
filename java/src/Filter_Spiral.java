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
class Filter_Spiral extends Filter {
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
	String bobbin_line, config_line;
	
	
	Filter_Spiral(String _dest,double _scale,double _margin) {
		dest=_dest;
		scale=_scale;
		margin=_margin;
	}
	

	public void SetConfigLine(String str) { config_line=str; }
	public void SetBobbinLine(String str) { bobbin_line=str; }
	public void SetPaperLimits(double _paper_top, double _paper_bottom, double _paper_left, double _paper_right) {}
	public void SetMachineLimits(double _limit_top, double _limit_bottom, double _limit_left, double _limit_right) {}

	
	private double RoundOff(double value) {
		return value;//Math.round(value * 1000) / 1000;
	}
	
	private void MoveTo(BufferedWriter out,double x,double y,boolean up) throws IOException {
		String command="G00 X"+RoundOff((x-w2)*iscale) + " Y" + RoundOff((h2-y)*iscale)+";\n";
		if(up) {
			previous_command=command;
		}
		if(lastup!=up && !up) {
			out.write(previous_command);
		}
		if(!up) {
			out.write(command);
		}
		if(lastup!=up) {
			if(up) {
				out.write("G00 Z90 F80;\n");  // slowly raise the pen.
				out.write("G00 F1250;\n");
			} else {
				out.write("G00 Z0 F80;\n");  // slowly lower the pen.
				out.write("G00 F1250;\n");
			}
		}
		lastup=up;
	}
	
	private int TakeImageSample(BufferedImage img,int x,int y) {
		image_height = img.getHeight();
		image_width = img.getWidth();
		
		// point sampling
		//return decode(img.getRGB(x,y));

		// 3x3 sampling
		int c=0;
		int values[]=new int[9];
		int weights[]=new int[9];
		if(y>0) {
			if(x>0) {
				values[c]=decode(img.getRGB(x-1, y-1));
				weights[c]=1;
				c++;
			}
			values[c]=decode(img.getRGB(x, y-1));
			weights[c]=2;
			c++;

			if(x<image_width-1) {
				values[c]=decode(img.getRGB(x+1, y-1));
				weights[c]=1;
				c++;
			}
		}

		if(x>0) {
			values[c]=decode(img.getRGB(x-1, y));
			weights[c]=2;
			c++;
		}
		values[c]=decode(img.getRGB(x, y));
		weights[c]=4;
		c++;
		if(x<image_width-1) {
			values[c]=decode(img.getRGB(x+1, y));
			weights[c]=2;
			c++;
		}

		if(y<image_height-1) {
			if(x>0) {
				values[c]=decode(img.getRGB(x-1, y+1));
				weights[c]=1;
				c++;
			}
			values[c]=decode(img.getRGB(x, y+1));
			weights[c]=2;
			c++;
	
			if(x<image_width-1) {
				values[c]=decode(img.getRGB(x+1, y+1));
				weights[c]=1;
				c++;
			}
		}
		
		int value=0,j;
		int sum=0;
		for(j=0;j<c;++j) {
			value+=values[j]*weights[j];
			sum+=weights[j];
		}
		
		return value/sum;
	}
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	public void Process(BufferedImage img) {
		image_height = img.getHeight();
		image_width = img.getWidth();
		int x,y,i,j;
		iscale=1.0/scale;
		w2=image_width/2;
		h2=image_height/2;
		double steps=4;
		double leveladd = 255.0/(steps+1);
		double level;
		int z=0;
		
		DrawbotGUI.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(dest));
			out.write(config_line+";\n");
			out.write(bobbin_line+";\n");
			// change to tool 0
			out.write("M06 T0;\n");
			// set absolute coordinates
			// lift pen
			out.write("G00 G90 Z90;\n");
			lastup=true;
			previous_command="";
			//*
			// create a spiral across the image
			// raise and lower the pen to darken the appropriate areas

			double hh=(image_height/2.0);
			double hw=(image_width/2.0);
			double maxr;
			//if(whole_image) {
				// go right to the corners
			//	maxr=Math.sqrt( hh*hh + hw*hw )+1;
			//} else 
			{
				// do the largest circle that still fits in the image.
				maxr = (hh>hw) ? hw : hh;
			}
			maxr/=2;
			DrawbotGUI.getSingleton().Log("<font color='yellow'>Maxd="+maxr+"</font>\n");
			double r=maxr, d, f;
			double fx,fy;
			j=0;
			while(r>0) {
				d=r*2;
				if(j==steps) j=0;
				++j;
				level = leveladd*j;
				// find circumference of current circle
				double circumference=Math.floor(((d+d-1)*Math.PI)/2);

				for(i=0;i<=circumference;++i) {
					f = i/circumference;
					//fx = hw + (Math.cos(Math.PI*2.0*f)*(d-f));
					fx = hw + (Math.cos(Math.PI*2.0*f)*d);
					//fy = hh + (Math.sin(Math.PI*2.0*f)*(d-f));
					fy = hh + (Math.sin(Math.PI*2.0*f)*d);
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
				r-=0.5;
				DrawbotGUI.getSingleton().Log("<font color='yellow'>d="+d+","+circumference+"</font>\n");
			}
			// lift pen 
			out.write("G00 Z90;\n");
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