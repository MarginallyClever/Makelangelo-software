package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * See <a href="https://linuxgazette.net/133/luana.html">...</a>
 * @author Dan Royer
 *
 */
public class Generator_Lissajous extends TurtleGenerator {
	private double WIDTH,HEIGHT;

	private static double delta = Math.PI/6;
	private static int a = 11;
	private static int b = 8; // controls complexity of curve
	private static int numSamples = 2000;

	public Generator_Lissajous() {
		super();
		SelectSlider field_a;
		SelectSlider field_b;
		SelectSlider field_numSamples;
		SelectSlider field_delta;

		add(field_a = new SelectSlider("a",Translator.get("LissajousA"),100,1,Generator_Lissajous.getA()));
		field_a.addSelectListener(evt->{
			setA(field_a.getValue());
			generate();
		});
		add(field_b = new SelectSlider("b",Translator.get("LissajousB"),100,1,Generator_Lissajous.getB()));
		field_b.addSelectListener(evt->{
			setB(field_b.getValue());
			generate();
		});
		add(field_delta = new SelectSlider("delta",Translator.get("LissajousDelta"),1000,0,(int)(Generator_Lissajous.getDelta()*1000.0)));
		field_delta.addSelectListener(evt->{
			setDelta(field_delta.getValue());
			generate();
		});
		add(field_numSamples = new SelectSlider("samples",Translator.get("SpirographNumSamples"),2000,50,Generator_Lissajous.getNumSamples()));
		field_numSamples.addSelectListener(evt->{
			setNumSamples(field_numSamples.getValue());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Lissajous_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}
	
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
		numSamples = Math.max(1,arg0);
	}

	@Override
	public void generate() {		
		// scale the step size so the curve fits on the paper
		WIDTH = myPaper.getMarginWidth()/2.0;
		HEIGHT = myPaper.getMarginHeight()/2.0;

		Turtle turtle = drawLissajous();

		notifyListeners(turtle);
	}

	/**
	 * see <a href="https://www.openprocessing.org/sketch/26608/">...</a>
	 * based on <a href="http://www.fjromero.com/processing/lissajous/">code by Javier Romero</a>
	 */
	protected Turtle drawLissajous() {
		double x,y,t;

		//x = AX*sin(a*t + delta) + screen_width/2;
		//y = BX*sin(b*t) + screen_height/2;
		Turtle turtle = new Turtle();
		
		for(int t1=0; t1<=numSamples; ++t1) {
			t = ( Math.PI*2.0 * t1 / (double)numSamples );
			
			x = WIDTH * Math.sin(a*t + delta*Math.PI);
			y = HEIGHT * Math.sin(b*t);
			turtle.moveTo(x, y);
			turtle.penDown();
		}
		return turtle;
	}
}
