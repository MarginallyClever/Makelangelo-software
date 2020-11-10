package com.marginallyclever.artPipeline.generators;

import java.util.Stack;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * generates a fibonacci spiral
 * @author danroyer
 *
 */
public class Generator_FibonacciSpiral extends ImageGenerator {
	private float xMax = 70;
	private float yMax = 70;
	private static int order = 7; // controls complexity of curve

	private Stack<Integer> fibonacciSequence;
	
	@Override
	public String getName() {
		return Translator.get("FibonacciSpiralName");
	}

	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_FibonacciSpiral_Panel(this);
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<3) order=1;
		Generator_FibonacciSpiral.order = order;
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
	public boolean generate() {
		xMax = (float)(machine.getMarginWidth () /2.0f);
		yMax = (float)(machine.getMarginHeight() /2.0f);
		Log.message("xMax="+xMax);
		Log.message("yMax="+yMax);
		
		// build the Fibonacci sequence.
		buildFibonacciSequence(order);
		
		// scale the fractal to fit on the page
		// short side
		float s1 = fibonacciSequence.peek();
		Log.message("s1="+s1);
		float scale1 = Math.min(xMax, yMax) * 2.0f / s1;
		// long side
		float s2 = fibonacciSequence.get(fibonacciSequence.size()-2) + s1;
		Log.message("s2="+s2);
		float scale2 = Math.max(xMax, yMax) * 2.0f / s2;

		if(scale1>scale2) scale1=scale2;
		
		turtle = new Turtle();
		
		// move to starting position
		float shortSide = fibonacciSequence.peek() * scale1 /2.0f; 
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
			fibonacciCell(o*scale1);
		}
			    
	    return true;
	}


	// L System tree
	private void fibonacciCell(float size) {
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
