package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

public class Generator_GosperCurve extends ImageGenerator {
	private Turtle turtle;
	private double turtleStep = 10.0f;
	private double xMax = 0;
	private double xMin = 0;
	private double yMax = 0;
	private double yMin = 0;
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
	public ImageGeneratorPanel getPanel() {
		return new Generator_GosperCurve_Panel(this);
	}
	
	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);

		double v = Math.min((machine.getPaperWidth()  * machine.getPaperMargin()),
				           (machine.getPaperHeight() * machine.getPaperMargin()));

		turtle = new Turtle();
		turtleStep = 10;
		turtle.setX(0);
		turtle.setY(0);
		xMax = 0;
		xMin = 0;
		yMax = 0;
		yMin = 0;
		GosperA(null, order);

		// scale the image to fit on the paper
		double w = xMax-xMin;
		double h = yMax-yMin;
		if(w>h) {
			double f = v/w;
			h*=f;
			turtleStep*=f;
			xMax*=f;
			xMin*=f;
			yMax*=f;
			yMin*=f;
		} else {
			double f = v/h;
			w*=f;
			turtleStep*=f;
			xMax*=f;
			xMin*=f;
			yMax*=f;
			yMin*=f;
		}
		// adjust the start position to center the image
		double x = (xMax+xMin)/-2;
		double y = (yMax+yMin)/-2;
		
		// move to starting position
		turtle.setX(x);
		turtle.setY(y);
		liftPen(out);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		GosperA(out, order);
		imageEnd(out);
	    
	    return true;
	}


	// Gosper curve A = A-B--B+A++AA+B-
	private void GosperA(Writer output, int n) throws IOException {
		if (n == 0) {
			turtle_goForward(output);
			return;
		}
		GosperA(output,n-1);
		turtle.turn(-60);
		GosperB(output,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		GosperB(output,n-1);
		turtle.turn(60);
		GosperA(output,n-1);
		turtle.turn(60);
		turtle.turn(60);
		GosperA(output,n-1);
		GosperA(output,n-1);
		turtle.turn(60);
		GosperB(output,n-1);
		turtle.turn(-60);
	}


	// Gosper curve B = +A-BB--B-A++A+B
	public void GosperB(Writer output, int n) throws IOException {
		if (n == 0) {
			turtle_goForward(output);
			return;
		}
		turtle.turn(60);
		GosperA(output,n-1);
		turtle.turn(-60);
		GosperB(output,n-1);
		GosperB(output,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		GosperB(output,n-1);
		turtle.turn(-60);
		GosperA(output,n-1);
		turtle.turn(60);
		turtle.turn(60);
		GosperA(output,n-1);
		turtle.turn(60);
		GosperB(output,n-1);
	}


	public void turtle_goForward(Writer output) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(turtleStep);
		if(output!=null) moveTo(output, turtle.getX(), turtle.getY(), false);
		if(xMax<turtle.getX()) xMax=turtle.getX();
		if(xMin>turtle.getX()) xMin=turtle.getX();
		if(yMax<turtle.getY()) yMax=turtle.getY();
		if(yMin>turtle.getY()) yMin=turtle.getY();
	}
}
