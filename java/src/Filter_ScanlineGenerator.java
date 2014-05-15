

import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ProgressMonitor;


public class Filter_ScanlineGenerator extends Filter {
	String dest;
	int numPoints;
	Point2D[] points = null;
	int scount;
	boolean lastup;
	ProgressMonitor pm;
	float previous_x,previous_y;

	
	Filter_ScanlineGenerator(String _dest) {
		dest=_dest;
	}
	
	
	private void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		float x2 = TX(x);
		float y2 = TY(y);
		
		if(up==lastup) {
			previous_x=x2;
			previous_y=y2;
		} else {
			tool.WriteMoveTo(out,previous_x,previous_y);
			tool.WriteMoveTo(out,x2,y2);
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
	}

	
	private int TakeImageSample(BufferedImage img,int x,int y) {
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
	public void Process(BufferedImage img) throws IOException {
		int i;
		int x,y;
		double leveladd = 255.0/2.0;
		double level=leveladd;
		int z=0;

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