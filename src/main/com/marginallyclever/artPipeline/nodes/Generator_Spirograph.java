package com.marginallyclever.artPipeline.nodes;

import java.util.ArrayList;

import com.marginallyclever.artPipeline.Node;
import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.panels.Generator_Spirograph_Panel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * See https://linuxgazette.net/133/luana.html
 * @author Dan Royer
 *
 */
public class Generator_Spirograph extends Node {
	private double xMax,xMin,yMax,yMin;
	private double totalScale;

	private static float pScale = 80; // controls complexity of curve
	private static int minorRadius = 2; // controls complexity of curve
	private static int majorRadius = 100; // controls complexity of curve
	private static int numSamples = 2000;
	private static boolean isEpitrochoid = false;
	
	@Override
	public String getName() {
		return Translator.get("SpirographName");
	}

	static public boolean getEpitrochoid() {
		return isEpitrochoid;
	}
	
	static public void setEpitrochoid(boolean arg0)	{
		isEpitrochoid = arg0;
	}

	static public int getMajorRadius() {
		return majorRadius;
	}
	
	static public void setMajorRadius(int arg0)	{
		majorRadius = arg0;
	}

	static public int getMinorRadius() {
		return minorRadius;
	}
	
	static public void setMinorRadius(int arg0) {
		minorRadius = arg0;
	}
	
	static public float getPScale() {
		return pScale;
	}
	
	static public void setPScale(float arg0) {
		pScale = arg0;
	}
	
	static public int getNumSamples() {
		return numSamples;
	}
	
	static public void setNumSamples(int arg0) {
		if(numSamples<1) numSamples=1;
		numSamples = arg0;
	}
	
	@Override
	public NodePanel getPanel() {
		return new Generator_Spirograph_Panel(this);
	}
		
	@Override
	public boolean iterate() {
		xMax = Float.NEGATIVE_INFINITY;
		yMax = Float.NEGATIVE_INFINITY;
		xMin = Float.POSITIVE_INFINITY;
		yMin = Float.POSITIVE_INFINITY;
		
		// generate the spiral once to find the max/min
		totalScale=1;
		
		Turtle turtle = new Turtle();
		drawSpirograph(turtle,false);

		// scale the step size so the spirograph fits on the paper
		double paperWidth  = 100;
		double paperHeight = 100;

		// Spirographs are not always symmetric, So instead of using width, use 2 * the larger xMax or -xMin, yMax or -yMin
		// the other option is to translate the position of the spirograph. Also add a small margin to allow for the
		// Imagemanipulator.EPSILON_MARGIN

		final float MARGIN = 1;
		float drawingWidth = MARGIN;
		float drawingHeight = MARGIN;

		if ( xMax > -xMin) {
			drawingWidth += 2 * xMax;
		} else {
			drawingWidth += 2 * -xMin;
		}

		if ( yMax > -yMin) {
			drawingHeight += 2 * yMax;
		} else {
			drawingHeight += 2 * -yMin;
		}

		double largestX = paperWidth/drawingWidth;
		double largestY = paperHeight/drawingHeight;
		totalScale =  largestX < largestY ? largestX : largestY;

		// draw the spirograph for real this time
		turtle.reset();
		drawSpirograph(turtle,true);

		ArrayList<Turtle> list = new ArrayList<Turtle>();
		list.add(turtle);
		setTurtleResult(list);
		
	    return false;
	}
	
	protected void drawSpirograph(Turtle turtle,boolean write) {
		float x=0,y=0;
		
		float dRadius,pScale1,pScale2;
		
		if(isEpitrochoid) {
			dRadius = majorRadius+minorRadius;
			pScale1 = -pScale;
			pScale2 = pScale;
		} else {
			// hypotrochoid
			dRadius = majorRadius-minorRadius;
			pScale1 = pScale;
			pScale2 = pScale;
		}
		
		float t = 0;

		// move to starting position
		x = dRadius*(float)Math.cos(t) + pScale1*(float)Math.cos(dRadius*t/minorRadius);
		y = dRadius*(float)Math.sin(t) - pScale2*(float)Math.sin(dRadius*t/minorRadius);
		turtle.moveTo(totalScale*x, totalScale*y);
		turtle.penDown();

		// https://www.reddit.com/r/math/comments/27nz3l/how_do_i_calculate_the_periodicity_of_a/
		// https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
		double period = lcm(majorRadius,minorRadius)/majorRadius;
		double periodRadians = Math.PI*2.0*(double)period/(double)numSamples;
		
		for(float t1 = 0; t1<=numSamples;++t1) {
			t = (float)( t1 * periodRadians );
			x = dRadius*(float)Math.cos(t) + pScale1*(float)Math.cos(dRadius*t/minorRadius);
			y = dRadius*(float)Math.sin(t) - pScale2*(float)Math.sin(dRadius*t/minorRadius);

			turtle.moveTo(totalScale*x, totalScale*y);
			// we are calculating max/min
			if(xMin>x) xMin=x;
			if(xMax<x) xMax=x;
			if(yMin>y) yMin=y;
			if(yMax<y) yMax=y;
		}
	}
	
	/**
	 * greatest common divider
	 * @param a
	 * @param b
	 * @return greatest common divider
	 */
	private static long gcd(long a, long b) {
		long temp;
	    while (b > 0) {
	        temp = b;
	        b = a % b; // % is remainder
	        a = temp;
	    }
	    return a;
	}
	
	/**
	 * least common multiplier
	 * @param a
	 * @param b
	 * @return least common multiplier
	 */
	private static long lcm(long a, long b) {
	    return a * (b / gcd(a, b));
	}
}
