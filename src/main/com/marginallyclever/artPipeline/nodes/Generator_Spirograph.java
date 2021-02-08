package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnectorBoolean;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.node.NodeConnectorInt;
import com.marginallyclever.core.turtle.Turtle;
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
	private NodeConnectorDouble inputPScale = new NodeConnectorDouble("Generator_Spirograph.inputPScale",80.0);
	// controls complexity of curve
	private NodeConnectorInt inputMinorRadius = new NodeConnectorInt("Generator_Spirograph.inputMinorRadius",2);
	// controls complexity of curve
	private NodeConnectorInt inputMajorRadius = new NodeConnectorInt("Generator_Spirograph.inputMajorRadius",100);
	// resolution of curve
	private NodeConnectorInt inputNumSamples = new NodeConnectorInt("Generator_Spirograph.inputNumSamples",2000);
	// style
	private NodeConnectorBoolean inputIsEpitrochoid = new NodeConnectorBoolean("Generator_Spirograph.inputIsEpitrochoid",false);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageConverter.outputTurtle");
	
	private double xMax,xMin,yMax,yMin;
	private double totalScale;
	
	public Generator_Spirograph() {
		super();
		inputs.add(inputPScale);
		inputs.add(inputMinorRadius);
		inputs.add(inputMajorRadius);
		inputs.add(inputNumSamples);
		inputs.add(inputIsEpitrochoid);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("SpirographName");
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
		
		if(inputIsEpitrochoid.getValue()) {
			dRadius = inputMajorRadius.getValue()+inputMinorRadius.getValue();
			pScale1 = -inputPScale.getValue();
			pScale2 = inputPScale.getValue();
		} else {
			// hypotrochoid
			dRadius = inputMajorRadius.getValue()-inputMinorRadius.getValue();
			pScale1 = inputPScale.getValue();
			pScale2 = inputPScale.getValue();
		}
		
		double t = 0;
		double v = dRadius*t/inputMinorRadius.getValue();

		// https://www.reddit.com/r/math/comments/27nz3l/how_do_i_calculate_the_periodicity_of_a/
		double period = lcm(inputMajorRadius.getValue(),inputMinorRadius.getValue()) / inputMajorRadius.getValue();
		double periodRadians = Math.PI*2.0*period/inputNumSamples.getValue();
		
		for(int t1 = 0; t1<=inputNumSamples.getValue();++t1) {
			t = (double)t1 * periodRadians;
			v = dRadius*t/inputMinorRadius.getValue();
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
