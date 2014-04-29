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
	float w2,h2,scale;
	ProgressMonitor pm;
	DrawingTool tool;

	
	Filter_Spiral(String _dest) {
		dest=_dest;
	}


	private void liftPen(BufferedWriter out) throws IOException {
		tool.WriteOff(out);
	}
	
	private void lowerPen(BufferedWriter out) throws IOException {
		tool.WriteOn(out);
	}

	
	private void MoveTo(BufferedWriter out,float x,float y,boolean up) throws IOException {
		tool.WriteMoveTo(out,
						 (x-w2)*scale,
						 -(y-h2)*scale);
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
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
	public void Process(BufferedImage img) throws IOException {
		int x,y,i,j;
		double steps=4;
		double leveladd = 255.0/(steps+1);
		double level;
		int z=0;
		
		image_height = img.getHeight();
		image_width = img.getWidth();
		w2=image_width/2;
		h2=image_height/2;

		MachineConfiguration mc = MachineConfiguration.getSingleton();
		scale=10f;
		if(image_width>image_height) {
			scale*=(float)mc.GetPaperWidth()/(float)image_width;
		} else {
			scale*=(float)mc.GetPaperHeight()/(float)image_height;
		}
		scale *= mc.paper_margin;

		tool = mc.GetCurrentTool();
		double toolDiameter=tool.GetDiameter()/scale;

		
		Makelangelo.getSingleton().Log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(dest));
		out.write(MachineConfiguration.getSingleton().GetConfigLine()+";\n");
		out.write(MachineConfiguration.getSingleton().GetBobbinLine()+";\n");
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
		j=0;
		while(r>0) {
			d=r*2;
			if(j==steps) j=0;
			++j;
			level = leveladd*j;
			// find circumference of current circle
			float circumference=(float) Math.floor(((d+(d-toolDiameter))*Math.PI)/2);

			for(i=0;i<=circumference;++i) {
				f = i/circumference;
				//fx = hw + (Math.cos(Math.PI*2.0*f)*(d-f));
				fx = w2 + (float)(Math.cos(Math.PI*2.0*f)*d);
				//fy = hh + (Math.sin(Math.PI*2.0*f)*(d-f));
				fy = h2 + (float)(Math.sin(Math.PI*2.0*f)*d);
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
			Makelangelo.getSingleton().Log("<font color='yellow'>d="+d+","+circumference+"</font>\n");
		}
		
		tool.WriteOff(out);
		
		SignName(out);
		
		// already home
		out.close();
		
		// TODO move to GUI
		Makelangelo.getSingleton().Log("<font color='green'>Completed.</font>\n");
		Makelangelo.getSingleton().PlayConversionFinishedSound();
		Makelangelo.getSingleton().LoadGCode(dest);
	}
	
	
	protected void SignName(BufferedWriter out) throws IOException {
		CreateMessageNow("Makelangelo #"+MachineConfiguration.getSingleton().GetUID(),out);
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