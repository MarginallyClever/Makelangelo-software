package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * See <a href="https://linuxgazette.net/133/luana.html">Plotting the spirograph equations with 'gnuplot'</a>
 * @author Dan Royer
 *
 */
public class Generator_Spirograph extends TurtleGenerator {
	private static double pScale = 80; // controls complexity of curve
	private static int minorRadius = 2; // controls complexity of curve
	private static int majorRadius = 100; // controls complexity of curve
	private static int numSamples = 2000;
	private static boolean isEpitrochoid = false;

	public Generator_Spirograph() {
		super();

		SelectBoolean field_isEpitrochoid;
		SelectInteger field_majorRadius;
		SelectInteger field_minorRadius;
		SelectDouble field_pScale;
		SelectInteger field_numSamples;

		add(field_isEpitrochoid = new SelectBoolean("Epitrochoid",Translator.get("SpirographEpitrochoid"),Generator_Spirograph.getEpitrochoid()));
		field_isEpitrochoid.addSelectListener(evt->{
			Generator_Spirograph.setEpitrochoid(field_isEpitrochoid.isSelected());
			generate();
		});
		add(field_majorRadius = new SelectInteger("MajorRadius",Translator.get("SpirographMajorRadius"),Generator_Spirograph.getMajorRadius()));
		field_majorRadius.addSelectListener(evt->{
			Generator_Spirograph.setMajorRadius(field_majorRadius.getValue());
			generate();
		});
		add(field_minorRadius = new SelectInteger("MinorRadius",Translator.get("SpirographMinorRadius"),Generator_Spirograph.getMinorRadius()));
		field_minorRadius.addSelectListener(evt->{
			Generator_Spirograph.setMinorRadius(field_minorRadius.getValue());
			generate();
		});
		add(field_pScale = new SelectDouble("PScale",Translator.get("SpirographPScale"),Generator_Spirograph.getPScale()));
		field_pScale.addSelectListener(evt->{
			Generator_Spirograph.setPScale(field_pScale.getValue());
			generate();
		});
		add(field_numSamples = new SelectInteger("NumSamples",Translator.get("SpirographNumSamples"),Generator_Spirograph.getNumSamples()));
		field_numSamples.addSelectListener(evt->{
			Generator_Spirograph.setNumSamples(field_numSamples.getValue());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Spirograph'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}
	
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
	public void generate() {
		Turtle turtle = drawSpirograph();
		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

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
		long period = lcm(majorRadius,minorRadius) / majorRadius;
		double periodRadians = Math.PI*2.0*(double)period / (double)numSamples;
		
		for(float t1 = 0; t1<=numSamples;++t1) {
			t = (float)( t1 * periodRadians );
			x = dRadius*Math.cos(t) + pScale1*Math.cos(dRadius*t/minorRadius);
			y = dRadius*Math.sin(t) - pScale2*Math.sin(dRadius*t/minorRadius);

			turtle.moveTo(x, y);
		}
		
		return turtle;
	}
	
	/**
	 * @return greatest common divider of 'a' and 'b'
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
	 * @return least common multiplier of 'a' and 'b'
	 */
	private static long lcm(long a, long b) {
	    return a * (b / gcd(a, b));
	}
}
