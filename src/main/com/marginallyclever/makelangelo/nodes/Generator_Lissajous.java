package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.NodeConnectorAngle;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * @see <a href='https://linuxgazette.net/133/luana.html'>https://linuxgazette.net/133/luana.html</a>
 * @author Dan Royer
 *
 */
public class Generator_Lissajous extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorAngle inputDelta = new NodeConnectorAngle("Generator_Lissajous.inputDelta",360.0/6.0);
	// controls complexity of curve
	private NodeConnectorInteger inputA = new NodeConnectorInteger("Generator_Lissajous.inputA",200);
	// controls complexity of curve
	private NodeConnectorInteger inputB = new NodeConnectorInteger("Generator_Lissajous.inputB",200);
	// quality of curve
	private NodeConnectorInteger inputSamples = new NodeConnectorInteger("Generator_Lissajous.inputSamples",10000);
	
	// scale the step size so the curve fits on the paper
	// TODO make parameter?
	private double WIDTH=100, HEIGHT=100;

	private double delta;
	private double a,b;
	private int numSamples;

	
	public Generator_Lissajous() {
		super();
		inputs.add(inputDelta);
		inputs.add(inputA);
		inputs.add(inputB);
		inputs.add(inputSamples);
	}

	@Override
	public String getName() {
		return Translator.get("Generator_Lissajous.name");
	}
		
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();

		WIDTH = inputWidth.getValue();
		HEIGHT = inputHeight.getValue();
		
		a = inputA.getValue();
		b = inputB.getValue();
		delta = Math.toRadians(inputDelta.getValue());

		drawLissajous(turtle,true);

		outputTurtle.setValue(turtle);
		
	    return false;
	}
	
	// see https://www.openprocessing.org/sketch/26608/
	// based on code by Javier Romero (http://www.fjromero.com/processing/lissajous/)
	protected void drawLissajous(Turtle turtle,boolean write) {
		double x,y,t;

		//x = AX*sin(a*t + delta) + screen_width/2;
		//y = BX*sin(b*t) + screen_height/2;
		turtle.reset();
		numSamples = inputSamples.getValue();
		
		for(int t1=0; t1<=numSamples; ++t1) {
			t = ( Math.PI*2.0 * (double)t1 / (double)numSamples );
			
			x = WIDTH * Math.sin(a*t + delta*Math.PI);
			y = HEIGHT * Math.sin(b*t);
			turtle.moveTo(x, y);
			turtle.penDown();
		}
	}
}
