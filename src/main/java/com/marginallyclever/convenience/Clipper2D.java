package com.marginallyclever.convenience;

import javax.vecmath.Point2d;

/**
 * Convenience methods for clipping shapes in 2D
 * @author Dan Royer
 *
 */
public class Clipper2D {
	/**
	 * Clip the line P0-P1 to the rectangle (rMax,rMin).<br>
	 * See also https://stackoverflow.com/questions/626812/most-elegant-way-to-clip-a-line
	 * @param P0 start of line.
	 * @param P1 end of line.
	 * @param rMax maximum extent of rectangle
	 * @param rMin minimum extent of rectangle
	 * @return true if some of the line remains, false if the entire line is cut.
	 */
	public static boolean clipLineToRectangle(Point2d P0, Point2d P1, Point2d rMax, Point2d rMin) {
		double xLeft   = rMin.x;
		double xRight  = rMax.x;
		double yBottom = rMin.y;
		double yTop    = rMax.y;
		
		int outCode0,outCode1; 
		
		while(true) {
			outCode0 = outCodes(P0,xLeft,xRight,yTop,yBottom);
			outCode1 = outCodes(P1,xLeft,xRight,yTop,yBottom);
			if( rejectCheck(outCode0,outCode1) ) return false;  // completely out
			if( acceptCheck(outCode0,outCode1) ) return true;  // completely in
			if(outCode0 == 0) {
				break;
			} 
			if( (outCode0 & 1) != 0 ) { 
				P0.x += (P1.x - P0.x)*(yTop    - P0.y)/(P1.y - P0.y);
				P0.y = yTop;
			} else if( (outCode0 & 2) != 0 ) { 
				P0.x += (P1.x - P0.x)*(yBottom - P0.y)/(P1.y - P0.y);
				P0.y = yBottom;
			} else if( (outCode0 & 4) != 0 ) { 
				P0.y += (P1.y - P0.y)*(xRight  - P0.x)/(P1.x - P0.x);
				P0.x = xRight;
			} else if( (outCode0 & 8) != 0 ) { 
				P0.y += (P1.y - P0.y)*(xLeft   - P0.x)/(P1.x - P0.x);
				P0.x = xLeft;
			}
		} 
		while(true) {
			outCode0 = outCodes(P0,xLeft,xRight,yTop,yBottom);
			outCode1 = outCodes(P1,xLeft,xRight,yTop,yBottom);
			if( rejectCheck(outCode0,outCode1) ) return false;  // completely out
			if( acceptCheck(outCode0,outCode1) ) return true;  // completely in
			if(outCode1 == 0) {
				break;
			}
			if( (outCode1 & 1) != 0 ) { 
				P1.x += (P0.x - P1.x)*(yTop    - P1.y)/(P0.y - P1.y);
				P1.y = yTop;
			} else if( (outCode1 & 2) != 0 ) { 
				P1.x += (P0.x - P1.x)*(yBottom - P1.y)/(P0.y - P1.y);
				P1.y = yBottom;
			} else if( (outCode1 & 4) != 0 ) { 
				P1.y += (P0.y - P1.y)*(xRight  - P1.x)/(P0.x - P1.x);
				P1.x = xRight;
			} else if( (outCode1 & 8) != 0 ) { 
				P1.y += (P0.y - P1.y)*(xLeft   - P1.x)/(P0.x - P1.x);
				P1.x = xLeft;
			}
		}
		return true;  // partially in
	}
	
	private static int outCodes(Point2d P,double xLeft,double xRight,double yTop,double yBottom) {
		int code = 0;
		     if(P.y > yTop   ) code += 1; // code for above
		else if(P.y < yBottom) code += 2; // code for below
		     if(P.x > xRight ) code += 4; // code for right
		else if(P.x < xLeft  ) code += 8; // code for left
		
		return code;
	}
	
	
	private static boolean rejectCheck(int outCode1, int outCode2) {
		return (outCode1 & outCode2) != 0;
	} 


	private static boolean acceptCheck(int outCode1, int outCode2) {
		return outCode1==0 && outCode2==0;
	}
}
