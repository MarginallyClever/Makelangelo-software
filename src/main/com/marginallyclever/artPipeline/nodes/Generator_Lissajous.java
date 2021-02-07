package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_Lissajous_Panel;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorDouble;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * See https://linuxgazette.net/133/luana.html
 * @author Dan Royer
 *
 */
public class Generator_Lissajous extends Node {
	// controls complexity of curve
	private NodeConnectorDouble inputDelta = new NodeConnectorDouble(1.0/6.0);
	// controls complexity of curve
	private NodeConnectorInt inputA = new NodeConnectorInt(200);
	// controls complexity of curve
	private NodeConnectorInt inputB = new NodeConnectorInt(200);
	// quality of curve
	private NodeConnectorInt inputSamples = new NodeConnectorInt(2000);

	// scale the step size so the curve fits on the paper
	// TODO make parameter?
	private final double WIDTH=100, HEIGHT=100;

	private double delta;
	private double a,b;
	private int numSamples = 2000;

	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	public Generator_Lissajous() {
		super();
		inputs.add(inputA);
		inputs.add(inputB);
		inputs.add(inputSamples);
		outputs.add(outputTurtle);
	}

	@Override
	public String getName() {
		return Translator.get("LissajousName");
	}

	@Override
	public NodePanel getPanel() {
		return new Generator_Lissajous_Panel(this);
	}
		
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		a = inputA.getValue();
		b = inputB.getValue();
		delta = inputDelta.getValue();

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
		
		for(int t1=0; t1<=numSamples; ++t1) {
			t = ( Math.PI*2.0 * t1 / (double)numSamples );
			
			x = WIDTH * Math.sin(a*t + delta*Math.PI);
			y = HEIGHT * Math.sin(b*t);
			turtle.moveTo(x, y);
			turtle.penDown();
		}
	}
}
