package Filters;


import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ProgressMonitor;

import Makelangelo.MachineConfiguration;
import Makelangelo.Makelangelo;
import Makelangelo.Point2D;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
public class Filter_SpiralGenerator extends Filter {
	// file properties
	String dest;
	// processing tools
	int numPoints;
	Point2D[] points = null;
	int scount;
	boolean lastup;
	ProgressMonitor pm;
	
	public void SetDestinationFile(String _dest) {
		dest=_dest;
	}

	
	private void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		tool.WriteMoveTo(out, TX(x), TY(y));
		if(lastup!=up) {
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
	public void Convert(BufferedImage img) throws IOException {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(6); 
		img = bw.Process(img);
		
		// spiralize
		int x,y,i,j;
		final int steps=4;
		double leveladd = 255.0/(steps);
		double level;
		int z=0;

		Makelangelo.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		
		ImageStart(img,out);

		double toolDiameter=tool.GetDiameter()/scale;
		// set absolute coordinates
		out.write("G90;\n");
		tool.WriteChangeTo(out);
		liftPen(out);
		lastup=true;

		//*
		// create a spiral across the image
		// raise and lower the pen to darken the appropriate areas

		float maxr;
		//if(whole_image) {
			// go right to the corners
		//	maxr=Math.sqrt( hh*hh + hw*hw )+1;
		//} else 
		{
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
			float circumference=(float) Math.floor(((d+d-toolDiameter)*Math.PI)/2);

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
		
		// TODO move to GUI
		Makelangelo.getSingleton().Log("<font color='green'>Completed.</font>\n");
		Makelangelo.getSingleton().PlayConversionFinishedSound();
		Makelangelo.getSingleton().LoadGCode(dest);
	}
	
	
	protected void SignName(OutputStreamWriter out) throws IOException {
		TextSetAlign(Align.CENTER);
		TextSetVAlign(VAlign.BOTTOM);
		TextSetPosition(image_width/2, image_height);
		TextCreateMessageNow("Makelangelo #"+MachineConfiguration.getSingleton().GetUID(),out);
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