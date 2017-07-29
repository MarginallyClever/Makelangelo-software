package com.marginallyclever.makelangeloRobot;


import java.io.IOException;
import java.io.Writer;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class ImageManipulator {	
	public static final float MARGIN_EPSILON = 0.01f;
	
	// pen position optimizing
	protected boolean lastUp;
	
	// threading
	protected ProgressMonitor pm;
	protected SwingWorker<Void, Void> swingWorker;

	// helpers
	protected MakelangeloRobotSettings machine;

	// quickly calculate margin edges for clipping
	double yTop;
	double yBottom;
	double xLeft;
	double xRight;
	
	
	public void setSwingWorker(SwingWorker<Void, Void> p) {
		swingWorker = p;
	}

	public void setProgressMonitor(ProgressMonitor p) {
		pm = p;
	}
	
	public void setRobot(MakelangeloRobot robot) {
		machine = robot.getSettings();
	}


	/**
	 * @return the translated name of the manipulator.
	 */
	public String getName() {
		return "Unnamed";
	}


	/**
	 * insert the machine-specific preamble at the start of the gcode file.
	 * @param img
	 * @param out
	 * @throws IOException
	 */
	public void imageStart(Writer out) throws IOException {	
		setAbsoluteMode(out);
	}


	protected void liftPen(Writer out) throws IOException {
		if(lastUp) return;
		machine.writePenUp(out);
		lastUp = true;
	}


	protected void lowerPen(Writer out) throws IOException {
		if(!lastUp) return;
		machine.writePenDown(out);
		lastUp = false;
	}

	protected void setAbsoluteMode(Writer out) throws IOException {
		machine.writeAbsoluteMode(out);
	}

	protected void setRelativeMode(Writer out) throws IOException {
		machine.writeRelativeMode(out);
	}


	/**
	 * Create the gcode that will move the robot to a new position.  It does not translate from image space to paper space.
	 * @param out where to write the gcode
	 * @param x new coordinate
	 * @param y new coordinate
	 * @param up new pen state
	 * @throws IOException on write failure
	 */
	protected void moveTo(Writer out, double x, double y, boolean up) throws IOException {
		if(isInsidePaperMargins(x,y)) {
			machine.writeMoveTo(out, (float) x, (float) y,lastUp);
		}
		if(lastUp != up) {
			if (up) liftPen(out);
			else lowerPen(out);
		}
	}


	/**
	 * This is a special case of moveTo() that only works when every line on the paper is a straight line.
	 * @param out where to write the gcode
	 * @param x new coordinate
	 * @param y new coordinate
	 * @param up new pen state
	 * @throws IOException on write failure
	 */
	protected void lineTo(Writer out, double x, double y, boolean up) throws IOException {
		if(lastUp != up) {
			moveTo(out,x,y,up);
		}
	}


	protected boolean isInsidePaperMargins(double x,double y) {
		if( x < (machine.getPaperLeft()   * machine.getPaperMargin()*10.0f)-MARGIN_EPSILON) return false;
		if( x > (machine.getPaperRight()  * machine.getPaperMargin()*10.0f)+MARGIN_EPSILON) return false;
		if( y < (machine.getPaperBottom() * machine.getPaperMargin()*10.0f)-MARGIN_EPSILON) return false;
		if( y > (machine.getPaperTop()    * machine.getPaperMargin()*10.0f)+MARGIN_EPSILON) return false;
		return true;
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

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
