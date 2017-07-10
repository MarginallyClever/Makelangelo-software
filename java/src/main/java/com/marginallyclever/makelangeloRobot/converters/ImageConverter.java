package com.marginallyclever.makelangeloRobot.converters;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangeloRobot.loadAndSave.LoadAndSaveImage;
import com.marginallyclever.makelangeloRobot.ImageManipulator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * Converts a BufferedImage to gcode
 * 
 * Image converters have to be listed in 
 * src/main/resources/META-INF/services/com.marginallyclever.makelangeloRobot.generators.ImageConverter
 * in order to be found by the ServiceLoader.  This is so that you could write an independent plugin and 
 * drop it in the same folder as makelangelo software to be "found" by the software.
 * 
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author Dan Royer
 *
 */
public abstract class ImageConverter extends ImageManipulator implements MakelangeloRobotDecorator {
	TransformedImage sourceImage;
	LoadAndSaveImage loadAndSave;
	boolean keepIterating=false;

	// quickly calculate margin edges for clipping
	double yTop;
	double yBottom;
	double xLeft;
	double xRight;

	
	public void setLoadAndSave(LoadAndSaveImage arg0) {
		loadAndSave = arg0;
	}
	
	/**
	 * set the image to be transformed.
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 */
	public void setImage(TransformedImage img) {
		sourceImage=img;
	}
	
	/**
	 * iterative and non-iterative solvers use this method to restart the conversion process.
	 */
	public void reconvert() {
		loadAndSave.reconvert();
	}
	
	/**
	 * run one "step" of an iterative image conversion process.
	 * @return true if conversion should iterate again.
	 */
	public boolean iterate() {
		return false;
	}
	
	public void stopIterating() {
		keepIterating=false;
	}
	
	/**
	 * for "run once" converters, return do the entire conversion and write to disk.
	 * for iterative solvers, the iteration is now done, write to disk.
	 * @param out the Writer to receive the generated gcode.
	 */
	public void finish(Writer out) throws IOException {}
	
	/**
	 * @return the gui panel with options for this manipulator
	 */
	public JPanel getPanel() {
		return null;
	}

	/**
	 * live preview as the system is converting pictures.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2, MakelangeloRobotSettings settings) {}
	

	
	protected void convertAlongLine(double x0,double y0,double x1,double y1,double stepSize,double channelCutoff,TransformedImage img,Writer out) throws IOException {
		double b;
		double dx=x1-x0;
		double dy=y1-y0;
		double halfStep = stepSize/2.0;
		double r2 = Math.sqrt(dx*dx+dy*dy);
		double steps = r2 / stepSize;
		if(steps<1) steps=1;

		double n,x,y,v;

		boolean wasInside = isInsidePaperMargins(x0, y0);
		boolean isInside;
		boolean penUp,oldPenUp;
		double oldX=x0,oldY=y0;
		if(wasInside) {
			v = img.sample( x0 - halfStep, y0 - halfStep, x0 + halfStep, y0 + halfStep);
			oldPenUp = (v>=channelCutoff);
		} else {
			oldPenUp = false;
		}
		
		lineTo(out, x0, y0, true);
		
		for (b = 0; b <= steps; ++b) {
			n = b / steps;
			x = dx * n + x0;
			y = dy * n + y0;
			isInside=isInsidePaperMargins(x, y);
			if(isInside) {
				v = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);
			} else {
				v = 255;
			}
			penUp = (v>=channelCutoff);
			if(isInside!=wasInside) {
				clipLine(out,oldX,oldY,x,y,oldPenUp,penUp,wasInside,isInside);
			}
			lineTo(out, x, y, penUp);
			if( wasInside && !isInside ) break;  // done
			wasInside=isInside;
			oldX=x;
			oldY=y;
			oldPenUp=penUp;
		}
		lineTo(out, x1, y1, true);
	}

	
	/**
	 * Pen has moved from one side of paper margin to another.  Find the point on the edge that is inside and mark that with a pen up or down (as appropriate)
	 * https://stackoverflow.com/questions/626812/most-elegant-way-to-clip-a-line
	 * @param oldX
	 * @param oldY
	 * @param x
	 * @param y
	 * @param oldPenUp
	 * @param penUp
	 * @throws IOException
	 */
	protected void clipLine(Writer out,double oldX,double oldY,double x,double y,boolean oldPenUp,boolean penUp,boolean wasInside,boolean isInside) throws IOException {
		xLeft   = (machine.getPaperLeft()   * machine.getPaperMargin()*10.0f)-ImageManipulator.MARGIN_EPSILON;
		xRight  = (machine.getPaperRight()  * machine.getPaperMargin()*10.0f)+ImageManipulator.MARGIN_EPSILON;
		yBottom = (machine.getPaperBottom() * machine.getPaperMargin()*10.0f)-ImageManipulator.MARGIN_EPSILON;
		yTop    = (machine.getPaperTop()    * machine.getPaperMargin()*10.0f)+ImageManipulator.MARGIN_EPSILON;
		
		ClippingPoint P0 = new ClippingPoint(oldX,oldY);
		ClippingPoint P1 = new ClippingPoint(x,y);
		
		if(CohenSutherland2DClipper(P0, P1)) {
			// some of the line is inside
			if(isInside) {
				// entering the rectangle
				if(oldPenUp==false) {
					moveTo(out,P0.x,P0.y,false);
				}
			} else {
				// leaving the rectangle
				if(oldPenUp==false) {
					moveTo(out,P1.x,P1.y,false);
				}
			}
		}
	}

	class ClippingPoint {
		double x,y;
		public ClippingPoint(double thisx, double thisy) { 
			x=thisx; 
			y=thisy;
		}
	} 
	
	boolean CohenSutherland2DClipper(ClippingPoint P0,ClippingPoint P1) {
		int outCode0,outCode1; 
		while(true) {
			outCode0 = outCodes(P0);
			outCode1 = outCodes(P1);
			if( rejectCheck(outCode0,outCode1) ) return false;  // whatever portion is left is completely out
			if( acceptCheck(outCode0,outCode1) ) return true;  // whatever portion is left is completely in
			if(outCode0 == 0) {
				double tempCoord;
				int tempCode;
				tempCoord = P0.x;
				P0.x= P1.x;
				P1.x = tempCoord;
				tempCoord = P0.y;
				P0.y= P1.y;
				P1.y = tempCoord;
				tempCode = outCode0; outCode0 = outCode1; outCode1 = tempCode;
			} 
			if( (outCode0 & 1) != 0 ) { 
				P0.x += (P1.x - P0.x)*(yTop - P0.y)/(P1.y - P0.y);
				P0.y = yTop;
			} else if( (outCode0 & 2) != 0 ) { 
				P0.x += (P1.x - P0.x)*(yBottom - P0.y)/(P1.y - P0.y);
				P0.y = yBottom;
			} else if( (outCode0 & 4) != 0 ) { 
				P0.y += (P1.y - P0.y)*(xRight - P0.x)/(P1.x - P0.x);
				P0.x = xRight;
			} else if( (outCode0 & 8) != 0 ) { 
				P0.y += (P1.y - P0.y)*(xLeft - P0.x)/(P1.x - P0.x);
				P0.x = xLeft;
			}
		} 
	} 
	
	private int outCodes(ClippingPoint P) {
		int Code = 0;
		if(P.y > yTop) Code += 1; /* code for above */ 
		else if(P.y < yBottom) Code += 2; /* code for below */

		if(P.x > xRight) Code += 4; /* code for right */
		else if(P.x < xLeft) Code += 8; /* code for left */
		
		return Code;
	}
	
	
	private boolean rejectCheck(int outCode1, int outCode2) {
		return ((outCode1 & outCode2) != 0 );
	} 


	private boolean acceptCheck(int outCode1, int outCode2) {
		return ( (outCode1 == 0) && (outCode2 == 0) );
	}
}
