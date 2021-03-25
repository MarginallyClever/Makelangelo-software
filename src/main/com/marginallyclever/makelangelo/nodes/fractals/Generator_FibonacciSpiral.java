package com.marginallyclever.makelangelo.nodes.fractals;

import java.util.Stack;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.NodeConnectorBoundedInt;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.nodes.TurtleGenerator;

/**
 * generates a fibonacci spiral
 * @author dan royer
 *
 */
public class Generator_FibonacciSpiral extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorInteger inputOrder = new NodeConnectorBoundedInt("Generator_FibonacciSpiral.inputOrder",25,1,7);
	
	private double xMax = 100;
	private double yMax = 100;
	private Stack<Integer> fibonacciSequence;
	
	public Generator_FibonacciSpiral() {
		super();
		inputs.add(inputOrder);
	}
	
	@Override
	public String getName() {
		return Translator.get("Generator_FibonacciSpiral.name");
	}

	private void buildFibonacciSequence(int order) {
		fibonacciSequence = new Stack<Integer>();
		fibonacciSequence.add(1);
		fibonacciSequence.add(1);
		int a = 1;
		int b = 1;
		int c;
		
		while(order>2) {
			c = a+b;
			fibonacciSequence.add(c);
			a=b;
			b=c;
			order--;
		}
	}

	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();

		xMax = inputWidth.getValue();
		yMax = inputHeight.getValue();
		
		// build the Fibonacci sequence.
		buildFibonacciSequence(inputOrder.getValue());
		
		// scale the fractal to fit on the page
		// short side
		double s1 = fibonacciSequence.peek();
		Log.message("s1="+s1);
		double scale1 = Math.min(xMax, yMax) * 2.0 / s1;
		// long side
		double s2 = fibonacciSequence.get(fibonacciSequence.size()-2) + s1;
		Log.message("s2="+s2);
		double scale2 = Math.max(xMax, yMax) * 2.0 / s2;

		if(scale1>scale2) scale1=scale2;
		
		turtle = new Turtle();
		
		// move to starting position
		double shortSide = fibonacciSequence.peek() * scale1 /2.0; 
		Log.message("shortSide="+shortSide);
		if( xMax < yMax ) {
			Log.message("tall thin");
			// tall thin paper, top left corner
			turtle.moveTo(shortSide,yMax);
			turtle.turn(180);
		} else {
			Log.message("short wide");
			// short wide paper, bottom left corner
			turtle.moveTo(-xMax,shortSide);
			turtle.turn(-90);
		}
		
		turtle.penDown();
		
		// do the curve, one square at a time.
		while(!fibonacciSequence.isEmpty()) {
			int o = fibonacciSequence.pop();
			fibonacciCell(turtle,o*scale1);
		}

		outputTurtle.setValue(turtle);
	    return false;
	}


	// L System tree
	private void fibonacciCell(Turtle turtle,double size) {
		// make the square around the cell
		turtle.forward(size);
		turtle.turn(90);
		turtle.forward(size);
		turtle.turn(90);
		double x2 = turtle.getX();
		double y2 = turtle.getY();
		turtle.forward(size);
		turtle.turn(90);
		double x0 = turtle.getX();
		double y0 = turtle.getY();
		turtle.forward(size);
		turtle.turn(90);

		// make the curve
		double x1 = turtle.getX();
		double y1 = turtle.getY();
		
		double dx, dy, px, py, len;
		final int steps = 20;
		int i;
		for(i=0;i<steps;++i) {
			px = (x2-x1) * ((double)i/steps) + x1;
			py = (y2-y1) * ((double)i/steps) + y1;
			dx = px - x0;
			dy = py - y0;
			len = Math.sqrt(dx*dx+dy*dy);
			px = dx*size/len + x0;
			py = dy*size/len + y0;
			turtle.moveTo(px, py);
		}
		turtle.moveTo(x2, y2);
		turtle.turn(90);
	}
}
