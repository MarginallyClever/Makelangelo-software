package com.marginallyclever.makelangelo.makeart.turtleGenerator;

import java.awt.geom.Rectangle2D;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Gosper curve fractal.
 * @author Dan Royer
 */
public class Generator_GosperCurve extends TurtleGenerator {
	private static int order = 4; // controls complexity of curve
	
	@Override
	public String getName() {
		return Translator.get("GosperCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_GosperCurve.order = order;
	}
	
	@Override
	public TurtleGeneratorPanel getPanel() {
		return new Generator_GosperCurve_Panel(this);
	}
	
	@Override
	public void generate() {
		double v = Math.min(myPaper.getMarginWidth(),myPaper.getMarginHeight());

		Turtle turtle = new Turtle();
                turtle.penDown();
		gosperA(turtle,order);

		// scale the image to fit on the paper
		Rectangle2D.Double dims = turtle.getBounds();
		double tw = dims.getWidth();
		double th = dims.getHeight();
		if(tw>v) {
			double s = v/tw;
			turtle.scale(s,s);
			th *= s;
			tw *= s;
		}
		if(th>v) {
			double s = v/th;
			turtle.scale(s,s);
			th *= s;
			tw *= s;
		}
		double tx = dims.getX();
		double ty = dims.getY();
		
		turtle.translate(-tx-tw/2, -ty-th/2);

		notifyListeners(turtle);
	}


	// Gosper curve A = A-B--B+A++AA+B-
	private void gosperA(Turtle turtle,int n) {
		if (n == 0) {
			gosperForward(turtle);
			return;
		}
		gosperA(turtle,n-1);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		turtle.turn(60);
		gosperA(turtle,n-1);
		turtle.turn(60);
		turtle.turn(60);
		gosperA(turtle,n-1);
		gosperA(turtle,n-1);
		turtle.turn(60);
		gosperB(turtle,n-1);
		turtle.turn(-60);
	}


	// Gosper curve B = +A-BB--B-A++A+B
	public void gosperB(Turtle turtle,int n) {
		if (n == 0) {
			gosperForward(turtle);
			return;
		}
		turtle.turn(60);
		gosperA(turtle,n-1);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		gosperB(turtle,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		gosperB(turtle,n-1);
		turtle.turn(-60);
		gosperA(turtle,n-1);
		turtle.turn(60);
		turtle.turn(60);
		gosperA(turtle,n-1);
		turtle.turn(60);
		gosperB(turtle,n-1);
	}


	public void gosperForward(Turtle turtle) {
		turtle.forward(1.0);
	}
}
