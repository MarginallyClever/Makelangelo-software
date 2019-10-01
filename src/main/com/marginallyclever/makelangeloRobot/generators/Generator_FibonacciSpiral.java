package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * generates a fibonacci spiral
 * @author danroyer
 *
 */
public class Generator_FibonacciSpiral extends ImageGenerator {
	private Turtle turtle;
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
		//System.out.println("add 1");
		fibonacciSequence.add(1);
		//System.out.println("add 1");
		int a = 1;
		int b = 1;
		int c;
		
		while(order>2) {
			c = a+b;
			fibonacciSequence.add(c);
			//System.out.println("add "+c);
			a=b;
			b=c;
			order--;
		}
	}

	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);

		xMax = (float)(machine.getPaperWidth () * machine.getPaperMargin() /2.0f);
		yMax = (float)(machine.getPaperHeight() * machine.getPaperMargin() /2.0f);
		System.out.println("xMax="+xMax);
		System.out.println("yMax="+yMax);
		
		// build the Fibonacci sequence.
		buildFibonacciSequence(order);
		
		// scale the fractal to fit on the page
		// short side
		float s1 = fibonacciSequence.peek();
		System.out.println("s1="+s1);
		float scale1 = Math.min(xMax, yMax) * 2.0f / s1;
		// long side
		float s2 = fibonacciSequence.get(fibonacciSequence.size()-2) + s1;
		System.out.println("s2="+s2);
		float scale2 = Math.max(xMax, yMax) * 2.0f / s2;

		if(scale1>scale2) scale1=scale2;
		
		turtle = new Turtle();
		
		liftPen(out);
		// move to starting position
		float shortSide = fibonacciSequence.peek() * scale1 /2.0f; 
		System.out.println("shortSide="+shortSide);
		if( xMax < yMax ) {
			System.out.println("tall thin");
			// tall thin paper, top left corner
			turtle.setX(shortSide);
			turtle.setY(yMax);
			turtle.turn(180);
		} else {
			System.out.println("short wide");
			// short wide paper, bottom left corner
			turtle.setX(-xMax);
			turtle.setY(shortSide);
			turtle.turn(-90);
		}
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		
		// do the curve, one square at a time.
		while(!fibonacciSequence.isEmpty()) {
			int o = fibonacciSequence.pop();
			float size = o*scale1;
			fibonacciCell(out, size);
		}
		
		imageEnd(out);
	    
	    return true;
	}


	// L System tree
	private void fibonacciCell(Writer output, float size) throws IOException {
		// make the square around the cell
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
		float x2 = turtle.getX();
		float y2 = turtle.getY();
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
		float x0 = turtle.getX();
		float y0 = turtle.getY();
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);

		// make the curve
		float x1 = turtle.getX();
		float y1 = turtle.getY();
		
		float dx, dy, px, py, len;
		final int steps = 20;
		int i;
		for(i=0;i<steps;++i) {
			px = (x2-x1) * ((float)i/steps) + x1;
			py = (y2-y1) * ((float)i/steps) + y1;
			dx = px - x0;
			dy = py - y0;
			len = (float)Math.sqrt(dx*dx+dy*dy);
			px = dx*size/len + x0;
			py = dy*size/len + y0;
			moveTo(output, px, py, false);
		}
		turtle.setX(x2);
		turtle.setY(y2);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
