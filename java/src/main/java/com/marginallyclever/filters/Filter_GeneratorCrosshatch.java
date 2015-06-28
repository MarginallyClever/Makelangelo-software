package com.marginallyclever.filters;


import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
public class Filter_GeneratorCrosshatch extends Filter {
	
	@Override
	public String getName() { return translator.get("Crosshatch"); }
	
	public Filter_GeneratorCrosshatch(MainGUI gui,MachineConfiguration mc,MultilingualSupport ms) {
		super(gui,mc,ms);
	}
	
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	@Override
	public void convert(BufferedImage img) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI,machine,translator,255); 
		img = bw.process(img);

		mainGUI.log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
        try(
        final OutputStream fileOutputStream = new FileOutputStream(dest);
        final Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        ) {

            imageStart(img, out);

            // set absolute coordinates
            out.write("G00 G90;\n");
            tool.writeChangeTo(out);
            liftPen(out);

            convertImageSpace(img, out);
//		ConvertPaperSpace(img,out);

            liftPen(out);
            signName(out);
            moveTo(out, 0, 0, true);
        }
    }
	
	double xStart,yStart;
	double xEnd,yEnd;
	double paperWidth,paperHeight;
		
	protected int sampleScale(BufferedImage img,double x0,double y0,double x1,double y1) {
		return sample(img,
				(x0-xStart)/(xEnd-xStart) * image_width,
				image_height - (y1-yStart)/(yEnd-yStart) * image_height,
				(x1-xStart)/(xEnd-xStart) * image_width,
				image_height - (y0-yStart)/(yEnd-yStart) * image_height
				);
	}
	
	protected void convertPaperSpace(BufferedImage img, Writer out) throws IOException {
		double leveladd = 255.0/6.0;
		double level=leveladd;
		
		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin
		
		paperWidth = machine.getPaperWidth();
		paperHeight = machine.getPaperHeight();
		
		xStart = -paperWidth/2.0;
		yStart = xStart * (double)image_height/(double)image_width;

		if(yStart < -(paperHeight/2.0)) {
			xStart *= (-(paperHeight/2.0)) / yStart;			
			yStart = -(paperHeight/2.0);
		}

		xStart *= 10.0* machine.paperMargin;
		yStart *= 10.0* machine.paperMargin;
		xEnd = -xStart;
		yEnd = -yStart;
		
		previous_x=0;
		previous_y=0;
		
		double stepSize = tool.getDiameter()*3.0;
		double halfStep = stepSize/2.0;
		double x,y;
		
		for(y=yStart;y<yEnd;y+=stepSize) {
			moveToPaper(out,xStart,y,true);
			for(x=xStart;x<xEnd;x+=stepSize) {
				int v = sampleScale(img,x-halfStep,y-halfStep,x+halfStep,y+halfStep);
				moveToPaper(out,x,y,v>=level);
			}
			moveToPaper(out,xEnd,y,true);
		}
		level += leveladd;
		for(x=xStart;x<xEnd;x+=stepSize) {
			moveToPaper(out,x,yStart,true);
			for(y=yStart;y<yEnd;y+=stepSize) {
				int v = sampleScale(img,x-halfStep,y-halfStep,x+halfStep,y+halfStep);
				moveToPaper(out,x,y,v>=level);
			}
			moveToPaper(out,x,yEnd,true);
		}
		

		double x2;
		
		
		level += leveladd;
		x=xStart;
		do {
			x2=x;
			moveToPaper(out,x2,yStart,true);
			for(y=yStart;y<yEnd;y+=stepSize,x2-=stepSize) {
				if(x2<xStart) {
					moveToPaper(out,xStart,y-stepSize,true);
					break;
				}
				if(x2>xEnd) continue;
				int v = sampleScale(img,x2-halfStep,y-halfStep,x2+halfStep,y+halfStep);
				moveToPaper(out,x2,y,v>=level);
			}
			if(x2>=xStart && x2 <xEnd)
				moveToPaper(out,x2,yEnd,true);
			
			x+=stepSize;
		} while(x2<xEnd);

		level += leveladd;
		x=xEnd;
		do {
			x2=x;
			moveToPaper(out,x2,yStart,true);
			for(y=yStart;y<yEnd;y+=stepSize,x2+=stepSize) {
				if(x2<xStart) continue;
				if(x2>xEnd) {
					moveToPaper(out,xEnd,y-=stepSize,true);
					break;
				}
				int v = sampleScale(img,x2-halfStep,y-halfStep,x2+halfStep,y+halfStep);
				moveToPaper(out,x2,y,v>=level);
			}
			if(x2>=xStart && x2 <xEnd)
				moveToPaper(out,x2,yEnd,true);
			
			x-=stepSize;
		} while(x2>xStart);
		/*
		moveToPaper(out,xStart,yStart,false);
		moveToPaper(out,xEnd  ,yStart,false);
		moveToPaper(out,xEnd  ,yEnd  ,false);
		moveToPaper(out,xStart,yEnd  ,false);
		moveToPaper(out,xStart,yStart,false);
		moveToPaper(out,xStart,yStart,true);
		*/
	}
	
	
	protected void convertImageSpace(BufferedImage img, Writer out) throws IOException {
		int i,j,x,y,z=0;
		double leveladd = 255.0/6.0;
		double level=leveladd;
		
		int steps = (int)Math.ceil(2.5*tool.getDiameter()/scale);
		if(steps<1) steps=1;
		
		mainGUI.log("<font color='green'>Generating layer 1</font>\n");
		// create horizontal lines across the image
		// raise and lower the pen to darken the appropriate areas
		i=0;
		for(y=0;y<image_height;y+=steps) {
			++i;
			if((i%2)==0) {
				moveTo(out,(float)          0,(float)y,true);
				for(x=0;x<image_width;++x) {
					z=sample3x3(img,x,y);
					moveTo(out,(float)x,(float)y,( z >= level ));
				}
				moveTo(out,(float)image_width,(float)y,true);
			} else {
				moveTo(out,(float)image_width,(float)y,true);
				for(x=image_width-1;x>=0;--x) {
					z=sample3x3(img,x,y);
					moveTo(out,(float)x,(float)y,( z >= level ));
				}
				moveTo(out,(float)          0,(float)y,true);
			}
		}
		level+=leveladd;


		mainGUI.log("<font color='green'>Generating layer 2</font>\n");
		// create vertical lines across the image
		// raise and lower the pen to darken the appropriate areas
		i=0;
		for(x=0;x<image_width;x+=steps) {
			++i;
			if((i%2)==0) {
				moveTo(out,(float)x,(float)0           ,true);
				for(y=0;y<image_height;++y) {
					z=sample3x3(img,x,y);
					moveTo(out,(float)x,(float)y,( z >= level ));
				}
				moveTo(out,(float)x,(float)image_height,true);
			} else {
				moveTo(out,(float)x,(float)image_height,true);
				for(y=image_height-1;y>=0;--y) {
					z=sample3x3(img,x,y);
					moveTo(out,(float)x,(float)y,( z >= level ));
				}
				moveTo(out,(float)x,(float)0           ,true);
			}
		}
		level+=leveladd;


		mainGUI.log("<font color='green'>Generating layer 3</font>\n");
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
				moveTo(out,(float)startx,(float)starty,true);
				for(j=0;j<=delta;++j) {
					z=sample3x3(img,startx+j,starty+j);
					moveTo(out,(float)(startx+j),(float)(starty+j),( z >= level ) );
				}
				moveTo(out,(float)endx,(float)endy,true);
			} else {
				moveTo(out,(float)endx,(float)endy,true);
				for(j=0;j<=delta;++j) {
					z=sample3x3(img,endx-j,endy-j);
					moveTo(out,(float)(endx-j),(float)(endy-j),( z >= level ) );
				}
				moveTo(out,(float)startx,(float)starty,true);
			}
			++i;
		}
		level+=leveladd;


		mainGUI.log("<font color='green'>Generating layer 4</font>\n");
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
				moveTo(out,(float)startx,(float)starty,true);
				for(j=0;j<=delta;++j) {
					z=sample3x3(img,startx-j,starty+j);
					moveTo(out,(float)(startx-j),(float)(starty+j),( z > level ) );
				}
				moveTo(out,(float)endx,(float)endy,true);
			} else {
				moveTo(out,(float)endx,(float)endy,true);
				for(j=0;j<delta;++j) {
					z=sample3x3(img,endx+j,endy-j);
					moveTo(out,(float)(endx+j),(float)(endy-j),( z > level ) );
				}
				moveTo(out,(float)startx,(float)starty,true);
			}
		}
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
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */