package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * See https://linuxgazette.net/133/luana.html
 * @author Dan Royer
 *
 */
public class Generator_Spirograph extends TurtleGenerator {
	private static double pScale = 80; // controls complexity of curve
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
	
	static public double getPScale() {
		return pScale;
	}
	
	static public void setPScale(double arg0) {
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
	public TurtleGeneratorPanel getPanel() {
		return new Generator_Spirograph_Panel(this);
	}
		
	@Override
	public void generate() {
		Turtle turtle = drawSpirograph();

		notifyListeners(turtle);
	}
	
	protected Turtle drawSpirograph() {
		Turtle turtle = new Turtle();
		
		double x=0,y=0;
		double dRadius,pScale1,pScale2;
		
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
		
		double t = 0;

		// move to starting position
		x = dRadius*Math.cos(t) + pScale1*Math.cos(dRadius*t/minorRadius);
		y = dRadius*Math.sin(t) - pScale2*Math.sin(dRadius*t/minorRadius);
		turtle.moveTo(x, y);
		turtle.penDown();

		// https://www.reddit.com/r/math/comments/27nz3l/how_do_i_calculate_the_periodicity_of_a/
		// https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
		double period = lcm(majorRadius,minorRadius)/majorRadius;
		double periodRadians = Math.PI*2.0*(double)period/(double)numSamples;
		
		for(float t1 = 0; t1<=numSamples;++t1) {
			t = (float)( t1 * periodRadians );
			x = dRadius*Math.cos(t) + pScale1*Math.cos(dRadius*t/minorRadius);
			y = dRadius*Math.sin(t) - pScale2*Math.sin(dRadius*t/minorRadius);

			turtle.moveTo(x, y);
		}
		
		return turtle;
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
