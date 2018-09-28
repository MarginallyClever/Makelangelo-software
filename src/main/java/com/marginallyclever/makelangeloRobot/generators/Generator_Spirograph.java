package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.Translator;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * @see https://linuxgazette.net/133/luana.html
 * @author Admin
 *
 */
public class Generator_Spirograph extends ImageGenerator {
	private float xMax,xMin,yMax,yMin;
	private float totalScale;

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
	public ImageGeneratorPanel getPanel() {
		return new Generator_Spirograph_Panel(this);
	}
		
	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		xMax = Float.NEGATIVE_INFINITY;
		yMax = Float.NEGATIVE_INFINITY;
		xMin = Float.POSITIVE_INFINITY;
		yMin = Float.POSITIVE_INFINITY;
		totalScale=1;

		// generate the spiral once to find the max/min
		drawSpirograph(out, false);
		
		// scale the step size so the spirograph fits on the paper
		float paperWidth = (float)(machine.getPaperWidth()  * machine.getPaperMargin());
		float paperHeight = (float)(machine.getPaperHeight() * machine.getPaperMargin());
		// dr 2018-06-06 I don't know why I needed to add a margin to get it drawing ok.
		final float MARGIN = 20;
		float drawingWidth = MARGIN+(xMax - xMin);
		float drawingHeight = MARGIN+(yMax - yMin);

		float largestX = paperWidth/drawingWidth;
		float largestY = paperHeight/drawingHeight;
		totalScale =  largestX < largestY ? largestX : largestY;
		
		// draw the spirograph for real this time
		drawSpirograph(out, true);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	    
	    return true;
	}
	
	protected void drawSpirograph(Writer output,boolean write) throws IOException {
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
		if(write) {
			// move to starting position
			x = dRadius*(float)Math.cos(t) + pScale1*(float)Math.cos(dRadius*t/minorRadius);
			y = dRadius*(float)Math.sin(t) - pScale2*(float)Math.sin(dRadius*t/minorRadius);

			liftPen(output);
			moveTo(output, totalScale*x, totalScale*y, true);
			lowerPen(output);
		}
		
		// https://www.reddit.com/r/math/comments/27nz3l/how_do_i_calculate_the_periodicity_of_a/
		// https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
		double period = lcm(majorRadius,minorRadius)/majorRadius;
		double periodRadians = Math.PI*2.0*(double)period/(double)numSamples;
		
		for(float t1 = 0; t1<=numSamples;++t1) {
			t = (float)( t1 * periodRadians );
			x = dRadius*(float)Math.cos(t) + pScale1*(float)Math.cos(dRadius*t/minorRadius);
			y = dRadius*(float)Math.sin(t) - pScale2*(float)Math.sin(dRadius*t/minorRadius);

			if(write) moveTo(output, totalScale*x, totalScale*y, false);
			else {
				// we are calculating max/min
				if(xMin>x) xMin=x;
				if(xMax<x) xMax=x;
				if(yMin>y) yMin=y;
				if(yMax<y) yMax=y;
			}
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
	private static long lcm(long a, long b)
	{
	    return a * (b / gcd(a, b));
	}
}
