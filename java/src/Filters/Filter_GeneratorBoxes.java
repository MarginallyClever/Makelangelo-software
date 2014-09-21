package Filters;


import java.awt.image.BufferedImage;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;


public class Filter_GeneratorBoxes extends Filter {
	public String GetName() { return "Boxxy"; }

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
	
	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	protected int TakeImageSampleBlock(BufferedImage img,int x0,int y0,int x1,int y1) {
		// point sampling
		int value=0;
		int sum=0;
		
		if(x0<0) x0=0;
		if(x1>image_width-1) x1 = image_width-1;
		if(y0<0) y0=0;
		if(y1>image_height-1) y1 = image_height-1;

		for(int y=y0;y<y1;++y) {
			for(int x=x0;x<x1;++x) {
				value += pointSample(img,x, y);
				++sum;
			}
		}

		if(sum==0) return 255;
		
		return value/sum;
	}
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 * @param img the image to convert.
	 */
	public void Convert(BufferedImage img) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.Process(img);

		// Open the destination file
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		// Set up the conversion from image space to paper space, select the current tool, etc.
		ImageStart(img,out);
		// "please change to tool X and press any key to continue"
		tool.WriteChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		
		// figure out how many lines we're going to have on this image.
		int steps = (int)Math.ceil(tool.GetDiameter()/(1.75*scale));
		if(steps<1) steps=1;

		int blockSize=(int)(steps*5);
		float halfstep = (float)blockSize/2.0f;
		
		// from top to bottom of the image...
		int x,y,z,i=0;
		for(y=0;y<image_height;y+=blockSize) {
			++i;
			if((i%2)==0) {
				// every even line move left to right
				//MoveTo(file,x,y,pen up?)]
				for(x=0;x<image_width-blockSize;x+=blockSize) {
					// read a block of the image and find the average intensity in this block
					z=TakeImageSampleBlock(img,x,(int)(y-halfstep),x+blockSize,(int)(y+halfstep));
					// scale the intensity value
					float scale_z = (255.0f-(float)z)/255.0f;
					float pulse_size = (halfstep-1) * scale_z;
					if( pulse_size>1) {
						// draw a circle.  the diameter is relative to the intensity.
						MoveTo(out,x+halfstep-pulse_size,y+halfstep-pulse_size,true);
						MoveTo(out,x+halfstep+pulse_size,y+halfstep-pulse_size,false);
						MoveTo(out,x+halfstep+pulse_size,y+halfstep+pulse_size,false);
						MoveTo(out,x+halfstep-pulse_size,y+halfstep+pulse_size,false);
						MoveTo(out,x+halfstep-pulse_size,y+halfstep-pulse_size,false);
						MoveTo(out,x+halfstep-pulse_size,y+halfstep-pulse_size,true);
					}
				}
			} else {
				// every odd line move right to left
				//MoveTo(file,x,y,pen up?)]
				for(x=image_width;x>=blockSize;x-=blockSize) {
					// read a block of the image and find the average intensity in this block
					z=TakeImageSampleBlock(img,x-blockSize,(int)(y-halfstep),x,(int)(y+halfstep));
					// scale the intensity value
					float scale_z = (255.0f-(float)z)/255.0f;
					float pulse_size = (halfstep-1) * scale_z;
					if( pulse_size>1) {
						// draw a circle.  the diameter is relative to the intensity.
						MoveTo(out,x-halfstep-pulse_size,y+halfstep-pulse_size,true);
						MoveTo(out,x-halfstep+pulse_size,y+halfstep-pulse_size,false);
						MoveTo(out,x-halfstep+pulse_size,y+halfstep+pulse_size,false);
						MoveTo(out,x-halfstep-pulse_size,y+halfstep+pulse_size,false);
						MoveTo(out,x-halfstep-pulse_size,y+halfstep-pulse_size,false);
						MoveTo(out,x-halfstep-pulse_size,y+halfstep-pulse_size,true);
					}
				}
			}
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