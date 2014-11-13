package Filters;


import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import Makelangelo.MachineConfiguration;
import Makelangelo.C3;



/** 
 * @author Dan
 */
public class Filter_GeneratorRGB extends Filter {	
	// The palette color mask has to match the tool index in the machine configuration
	C3 [] palette = new C3[] {
		new C3(0,0,0),
		new C3(255,0,0),
		new C3(0,255,0),
		new C3(0,0,255),
		new C3(255,255,255),
	};

	OutputStreamWriter out;
	int step1;
	int step2;
	int step4;
	int palette_mask;
	C3 [] error=null;
	C3 [] nexterror=null;
	int stepw=0,steph=0;
	int direction=1;
	


	public String GetName() { return "Red Green Blue"; }

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	protected void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
		tool.WriteMoveTo(out, TX(x), TY(y));
	}
	
	C3 QuantizeColor(C3 c) {
		C3 closest = palette[0];

	    for (C3 n : palette) 
	      if (n.diff(c) < closest.diff(c))
	        closest = n;

	    return closest;
	}
	
	private void DitherDirection(BufferedImage img,int y,C3[] error,C3[] nexterror,int direction) throws IOException {
		int w = stepw;
		C3 oldPixel = new C3(0,0,0);
		C3 newPixel = new C3(0,0,0);
		C3 quant_error = new C3(0,0,0);
		int start, end, x;

		for(x=0;x<w;++x) nexterror[x].set(0,0,0);
		
		if(direction>0) {
			start=0;
			end=w;
		} else {
			start=w-1;
			end=-1;
		}
		
		// @TODO: make this a parameter
		boolean draw_filled=false;
		
		// for each x from left to right
		for(x=start;x!=end;x+=direction) {
			// oldpixel := pixel[x][y]
			//oldPixel.set( new C3(img.getRGB(x, y)).add(error[x]) );
			oldPixel.set( new C3(TakeImageSampleBlock(img,x*step4,y*step4,x*step4+step4,y*step4+step4)).add(error[x]) );
			// newpixel := find_closest_palette_color(oldpixel)
			newPixel = QuantizeColor(oldPixel);

			// pixel[x][y] := newpixel
			if(newPixel.diff(palette[palette_mask])==0) {
				// draw a circle.  the diameter is relative to the intensity.
				if(draw_filled) {
					MoveTo(out,x*step4+step2-step2,y*step4+step2-step2,true);
					MoveTo(out,x*step4+step2+step2,y*step4+step2-step2,false);
					MoveTo(out,x*step4+step2+step2,y*step4+step2+step2,false);
					MoveTo(out,x*step4+step2-step2,y*step4+step2+step2,false);
					MoveTo(out,x*step4+step2-step2,y*step4+step2-step2,false);
					MoveTo(out,x*step4+step2+step1,y*step4+step2-step1,false);
					MoveTo(out,x*step4+step2+step1,y*step4+step2+step1,false);
					MoveTo(out,x*step4+step2-step1,y*step4+step2+step1,false);
					MoveTo(out,x*step4+step2-step1,y*step4+step2-step1,false);
					MoveTo(out,x*step4+step2      ,y*step4+step2      ,false);
					MoveTo(out,x*step4+step2      ,y*step4+step2      ,true);
				} else {
					MoveTo(out,x*step4+step2-step1,y*step4+step2-step1,true);
					MoveTo(out,x*step4+step2+step1,y*step4+step2-step1,false);
					MoveTo(out,x*step4+step2+step1,y*step4+step2+step1,false);
					MoveTo(out,x*step4+step2-step1,y*step4+step2+step1,false);
					MoveTo(out,x*step4+step2-step1,y*step4+step2-step1,false);
					MoveTo(out,x*step4+step2-step1,y*step4+step2-step1,true);
				}
			}
			
			// quant_error := oldpixel - newpixel
			quant_error.set( oldPixel.sub( newPixel ) );
			// pixel[x+1][y  ] += 7/16 * quant_error
			// pixel[x-1][y+1] += 3/16 * quant_error
			// pixel[x  ][y+1] += 5/16 * quant_error
			// pixel[x+1][y+1] += 1/16 * quant_error
				nexterror[x          ].add(quant_error.mul(5.0/16.0));
			if(x+direction>=0 && x+direction < w) {
				    error[x+direction].add(quant_error.mul(7.0/16.0));
				nexterror[x+direction].add(quant_error.mul(1.0/16.0));
			}
			if(x-direction>=0 && x-direction < w) {
				nexterror[x-direction].add(quant_error.mul(3.0/16.0));
			}
		}
	}

	
	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	protected C3 TakeImageSampleBlock(BufferedImage img,int x0,int y0,int x1,int y1) {
		// point sampling
		C3 value = new C3(0,0,0);
		int sum=0;
		
		if(x0<0) x0=0;
		if(x1>image_width-1) x1 = image_width-1;
		if(y0<0) y0=0;
		if(y1>image_height-1) y1 = image_height-1;

		for(int y=y0;y<y1;++y) {
			for(int x=x0;x<x1;++x) {
				value.add(new C3(img.getRGB(x, y)));
				++sum;
			}
		}

		if(sum==0) return new C3(255,255,255);
		
		return value.mul(1.0f/sum);
	}
	
	
	protected void Scan(int tool_index,BufferedImage img) throws IOException {
		palette_mask=tool_index;
		
		// "please change to tool X and press any key to continue"
		tool = MachineConfiguration.getSingleton().GetTool(tool_index);
		tool.WriteChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		int y;
		
		for(y=0;y<error.length;++y) {
			error[y] = new C3(0,0,0);
			nexterror[y] = new C3(0,0,0);
		}

		direction=1;
		for(y=0;y<steph;++y) {
			DitherDirection(img,y,error,nexterror,direction);
			
			direction = -direction;
			C3 [] tmp = error;
			error=nexterror;
			nexterror=tmp;
		}
	}
	
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		// Open the destination file
		out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		// Set up the conversion from image space to paper space, select the current tool, etc.
		ImageStart(img,out);
		
		// figure out how many lines we're going to have on this image.
		int steps = (int)Math.ceil(tool.GetDiameter()/(1.0*scale));
		if(steps<1) steps=1;

		step4 = (int)(steps*4.0);
		step2 = (int)(step4*4.0/5);
		step1 = (int)(step4*2.0/5);
		
		// set up the error buffers for floyd/steinberg dithering
		stepw=(int)Math.ceil((float)image_width/step4);
		steph=(int)Math.ceil((float)image_height/step4);
		error=new C3[stepw];
		nexterror=new C3[stepw];
		
		try{
			Scan(0,img);  // black
			Scan(1,img);  // red
			Scan(2,img);  // green
			Scan(3,img);  // blue
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		liftPen(out);
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