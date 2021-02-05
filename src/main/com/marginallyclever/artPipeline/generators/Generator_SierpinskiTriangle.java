package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * see https://en.wikipedia.org/wiki/Sierpi%C5%84ski_arrowhead_curve
 * @author Dan Royer 2016-12-12
 *
 */
public class Generator_SierpinskiTriangle extends TurtleGenerator {
	private double xMax, xMin, yMax, yMin;
	private double maxSize;
	private static int order = 4; // controls complexity of curve
	
	@Override
	public String getName() {
		return Translator.get("SierpinskiTriangleName");
	}


	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_SierpinskiTriangle.order = order;
	}
	
	@Override
	public TurtleGeneratorPanel getPanel() {
		return new Generator_SierpinskiTriangle_Panel(this);
	}
	
	@Override
	public Turtle generate() {
		Turtle turtle = new Turtle();
		
		xMax = 100;
		yMax = -100;
		xMin = -xMax;
		yMin = -yMax;

		turtle = new Turtle();
		
		double xx = xMax - xMin;
		double yy = yMax - yMin;
		maxSize = Math.tan(Math.toRadians(30))*(xx < yy ? xx : yy)*2;
		double jj = Math.asin(Math.toRadians(30))*(xx < yy ? xx : yy);

		// move to starting position
		if(xMax>yMax) {
			turtle.moveTo(-jj,yMin);
		} else {
			turtle.moveTo(xMax,-jj);
			turtle.turn(90);
		}
		turtle.penDown();
		// do the curve
		if( (order&1) == 0 ) {
			drawCurve(turtle,order, maxSize,-60);
		} else {
			turtle.turn(60);
			drawCurve(turtle,order, maxSize,-60);
		}

		return turtle;
	}


	private void drawCurve(Turtle turtle,int n, double distance,double angle) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		
		drawCurve(turtle,n-1,distance/2.0f,-angle);
		turtle.turn(angle);
		drawCurve(turtle,n-1,distance/2.0f,angle);
		turtle.turn(angle);
		drawCurve(turtle,n-1,distance/2.0f,-angle);
	}
}
