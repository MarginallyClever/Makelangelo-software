package com.marginallyclever.artPipeline;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class ImageManipulator {	
	public static final float MARGIN_EPSILON = 0.01f;
	
	// pen position optimizing
	public Turtle turtle = new Turtle();
	// threading
	protected ProgressMonitor pm;
	protected SwingWorker<Void, Void> swingWorker;
	// helpers
	protected MakelangeloRobotSettings machine;

	
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


	protected boolean isInsidePaperMargins(double x,double y) {
		if( x < machine.getMarginLeft()  -ImageManipulator.MARGIN_EPSILON) return false;
		if( x > machine.getMarginRight() +ImageManipulator.MARGIN_EPSILON) return false;
		if( y < machine.getMarginBottom()-ImageManipulator.MARGIN_EPSILON) return false;
		if( y > machine.getMarginTop()   +ImageManipulator.MARGIN_EPSILON) return false;
		return true;
	}

	
	/**
	 * Clip the line P0-P1 to the paper margins.
	 * https://stackoverflow.com/questions/626812/most-elegant-way-to-clip-a-line
	 * @param P0 start of line
	 * @param P1 end of line
	 * @return true if some of the line remains, false if the entire line is cut.
	 */
	protected boolean clipLineToPaperMargin(Point2D P0,Point2D P1) {
		double xLeft   = machine.getMarginLeft()  -ImageManipulator.MARGIN_EPSILON;
		double xRight  = machine.getMarginRight() +ImageManipulator.MARGIN_EPSILON;
		double yBottom = machine.getMarginBottom()-ImageManipulator.MARGIN_EPSILON;
		double yTop    = machine.getMarginTop()   +ImageManipulator.MARGIN_EPSILON;
		
		int outCode0,outCode1; 
		while(true) {
			outCode0 = outCodes(P0,xLeft,xRight,yTop,yBottom);
			outCode1 = outCodes(P1,xLeft,xRight,yTop,yBottom);
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
	
	private int outCodes(Point2D P,double xLeft,double xRight,double yTop,double yBottom) {
		int code = 0;
		if(P.y > yTop) code += 1; /* code for above */ 
		else if(P.y < yBottom) code += 2; /* code for below */

		if(P.x > xRight) code += 4; /* code for right */
		else if(P.x < xLeft) code += 8; /* code for left */
		
		return code;
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
