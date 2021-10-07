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
public class Generator_Lissajous extends TurtleGenerator {
	private double WIDTH,HEIGHT;

	private static double delta = Math.PI/6;
	private static int a = 11;
	private static int b = 8; // controls complexity of curve
	private static int numSamples = 2000;
	
	@Override
	public String getName() {
		return Translator.get("LissajousName");
	}

	static public int getA() {
		return a;
	}
	
	static public void setA(int arg0)	{
		a = arg0;
	}

	static public int getB() {
		return b;
	}
	
	static public void setB(int arg0)	{
		b = arg0;
	}

	static public float getDelta() {
		return (float)delta;
	}
	
	static public void setDelta(float arg0)	{
		delta = arg0;
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
		return new Generator_Lissajous_Panel(this);
	}
		
	@Override
	public void generate() {
		// scale the step size so the curve fits on the paper
		WIDTH = myPaper.getMarginWidth()/2.0;
		HEIGHT = myPaper.getMarginHeight()/2.0;

		drawLissajous(true);

		notifyListeners(turtle);
	}
	
	// see https://www.openprocessing.org/sketch/26608/
	// based on code by Javier Romero (http://www.fjromero.com/processing/lissajous/)
	protected void drawLissajous(boolean write) {
		double x,y,t;

		//x = AX*sin(a*t + delta) + screen_width/2;
		//y = BX*sin(b*t) + screen_height/2;
		turtle = new Turtle();
		
		for(int t1=0; t1<=numSamples; ++t1) {
			t = ( Math.PI*2.0 * t1 / (double)numSamples );
			
			x = WIDTH * Math.sin(a*t + delta*Math.PI);
			y = HEIGHT * Math.sin(b*t);
			turtle.moveTo(x, y);
			turtle.penDown();
		}
	}
}
