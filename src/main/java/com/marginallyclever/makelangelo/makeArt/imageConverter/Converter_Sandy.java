package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Sandy extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_Sandy.class);
	private static int blockScale=150;
	private static int direction=0;
	private String [] directionChoices = new String[] { 
		Translator.get("top right"),
		Translator.get("top left"), 
		Translator.get("bottom left"), 
		Translator.get("bottom right"), 
		Translator.get("center")
	};
	
	public Converter_Sandy() {}
	
	@Override
	public String getName() {
		return Translator.get("SandyNoble.title");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("rings")) setScale((int)evt.getNewValue());
		if(evt.getPropertyName().equals("direction")) setDirection((int)evt.getNewValue());
	}
	
	@Override
	public void finish() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);

		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		double yBottom = myPaper.getPaperBottom();
		double yTop    = myPaper.getPaperTop();
		double xLeft   = myPaper.getPaperLeft();
		double xRight  = myPaper.getPaperRight();

		double pBottom = myPaper.getMarginBottom() +1.0;
		double pTop    = myPaper.getMarginTop()    -1.0;
		double pLeft   = myPaper.getMarginLeft()   +1.0;
		double pRight  = myPaper.getMarginRight()  -1.0;

		double cx,cy;
		double last_x=0,last_y=0;

		boolean wasDrawing=false;
		
		switch(direction) {
		case 0:		cx = xRight;	cy = yTop;		last_x = pRight; 	last_y = pTop;		break;
		case 1:		cx = xLeft;		cy = yTop;		last_x = pLeft; 	last_y = pTop;		break;
		case 2:		cx = xLeft;		cy = yBottom;	last_x = pLeft; 	last_y = pBottom;	break;
		case 3:		cx = xRight;	cy = yBottom;	last_x = pRight; 	last_y = pBottom;	break;
		default:	cx = 0;			cy = 0;			last_x = 0;      	last_y = 0;			break;
		}

		double x, y, z, scaleZ;

		double dx = xRight - xLeft; 
		double dy = yTop - yBottom;
		double rMax = Math.sqrt(dx*dx+dy*dy);
		double rMin = 0;

		double rStep = (rMax-rMin)/(double)blockScale;
		double r;
		double t_dir=1;
		double pulseFlip=1;
		double t,t_step;
		double flipSum;
		double pulseSize = rStep*0.5 - 0.5;//r_step * 0.6 * scale_z;

		turtle = new Turtle();
		turtle.lock();
		logger.debug("Sandy started.");
		//Thread.dumpStack();
		
		try {
			// make concentric circles that get bigger and bigger.
			for(r=rMin;r<rMax;r+=rStep) {
				// go around in a circle
				t=0;
				t_step = 1.0/r;
				flipSum=0;
				// go around the circle
				for(t=0;t<Math.PI*2;t+=t_step) {
					dx = Math.cos(t_dir *t);
					dy = Math.sin(t_dir *t);
					x = cx + dx * r;
					y = cy + dy * r;
					if(!isInsidePaperMargins(x,y)) {
						if(wasDrawing) {
							turtle.jumpTo(last_x,last_y);
							wasDrawing=false;
						}
						continue;
					}
	
					last_x=x;
					last_y=y;
					// read a block of the image and find the average intensity in this block
					z = img.sample( x-pulseSize/2.0, y-pulseSize/2.0,x+pulseSize/2.0,y +pulseSize/2.0 );
					// scale the intensity value
					if(z<0) z=0;
					if(z>255) z=255;
					scaleZ = (255.0 -  z) / 255.0;
	
					if(wasDrawing == false) {
						turtle.jumpTo(last_x,last_y);
						wasDrawing=true;
					}
	
					turtle.moveTo(	x + dx * pulseSize*pulseFlip,
									y + dy * pulseSize*pulseFlip);
					
					flipSum+=scaleZ;
					if(flipSum >= 1) {
						flipSum-=1;
						pulseFlip = -pulseFlip;
						turtle.moveTo(	x + dx * pulseSize*pulseFlip,
										y + dy * pulseSize*pulseFlip);
					}
				}
				t_dir=-t_dir;
			}
		} catch(Exception e) {
			logger.error("Sandy failed", e);
		} finally {
			turtle.unlock();
			logger.debug("Sandy finished.");
		}
	}

	public int getScale() {
		return blockScale;
	}
	public void setScale(int value) {
		if(value<1) value=1;
		blockScale=value;
	}
	public String [] getDirections() {
		return directionChoices;
	}
	public int getDirectionIndex() {
		return direction;
	}
	public void setDirection(int value) {
		if(value<0) value=0;
		if(value>=directionChoices.length) value = directionChoices.length-1;
		direction = value;
	}
}


/**
 * This file is part of Makelangelo.
 *
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */