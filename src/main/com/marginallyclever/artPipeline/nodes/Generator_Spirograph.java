package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_Spirograph_Panel;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorBoolean;
import com.marginallyclever.convenience.nodes.NodeConnectorDouble;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * x(t)=(R-r)*cos(t) + p*cos((R-r)*t/r)
 * y(t)=(R-r)*sin(t) - p*sin((R-r)*t/r)
 * @see <a href='https://linuxgazette.net/133/luana.html'>https://linuxgazette.net/133/luana.html</a>
 * @author Dan Royer
 *
 */
public class Generator_Spirograph extends Node {
	// controls complexity of curve
	private NodeConnectorDouble pScale = new NodeConnectorDouble(80.0);
	// controls complexity of curve
	private NodeConnectorInt minorRadius = new NodeConnectorInt(2);
	// controls complexity of curve
	private NodeConnectorInt majorRadius = new NodeConnectorInt(100);
	// resolution of curve
	private NodeConnectorInt numSamples = new NodeConnectorInt(2000);
	// style
	private NodeConnectorBoolean isEpitrochoid = new NodeConnectorBoolean(false);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	private double xMax,xMin,yMax,yMin;
	private double totalScale;
	
	public Generator_Spirograph() {
		super();
		inputs.add(pScale);
		inputs.add(minorRadius);
		inputs.add(majorRadius);
		inputs.add(numSamples);
		inputs.add(isEpitrochoid);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("SpirographName");
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

		outputTurtle.setValue(turtle);
		
	    return false;
	}
	
	protected void drawSpirograph(Turtle turtle,boolean write) {
		double x=0,y=0;
		
		double dRadius,pScale1,pScale2;
		
		if(isEpitrochoid.getValue()) {
			dRadius = majorRadius.getValue()+minorRadius.getValue();
			pScale1 = -pScale.getValue();
			pScale2 = pScale.getValue();
		} else {
			// hypotrochoid
			dRadius = majorRadius.getValue()-minorRadius.getValue();
			pScale1 = pScale.getValue();
			pScale2 = pScale.getValue();
		}
		
		double t = 0;
		double v = dRadius*t/minorRadius.getValue();

		// https://www.reddit.com/r/math/comments/27nz3l/how_do_i_calculate_the_periodicity_of_a/
		double period = lcm(majorRadius.getValue(),minorRadius.getValue()) / majorRadius.getValue();
		double periodRadians = Math.PI*2.0*period/numSamples.getValue();
		
		for(int t1 = 0; t1<=numSamples.getValue();++t1) {
			t = (double)t1 * periodRadians;
			v = dRadius*t/minorRadius.getValue();
			x = dRadius*Math.cos(t) + pScale1*Math.cos(v);
			y = dRadius*Math.sin(t) - pScale2*Math.sin(v);

			turtle.moveTo(totalScale*x, totalScale*y);
			turtle.penDown();
			// we are calculating max/min
			if(xMin>x) xMin=x;
			if(xMax<x) xMax=x;
			if(yMin>y) yMin=y;
			if(yMax<y) yMax=y;
		}
		turtle.penUp();
	}
	
	/**
	 * greatest common divider
	 * TODO move to convenience?
	 * @see https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
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
	 * @see https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
	 * @param a
	 * @param b
	 * @return least common multiplier
	 */
	private static long lcm(long a, long b) {
	    return a * (b / gcd(a, b));
	}
}