package com.marginallyclever.makelangeloRobot.generators;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * see https://en.wikipedia.org/wiki/Sierpi%C5%84ski_arrowhead_curve
 * @author Dan Royer 2016-12-12
 *
 */
public class Generator_SierpinskiTriangle extends ImageGenerator {
	private float xMax, xMin, yMax, yMin;
	private float maxSize;
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
	public ImageGeneratorPanel getPanel() {
		return new Generator_SierpinskiTriangle_Panel(this);
	}
	
	@Override
	public boolean generate() {
		xMax = (float)(machine.getPaperWidth() * machine.getPaperMargin())/2.0f;
		yMax = (float)(machine.getPaperHeight() * machine.getPaperMargin())/2.0f;
		xMin = -xMax;
		yMin = -yMax;

		turtle = new Turtle();
		
		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = (float)Math.tan(Math.toRadians(30))*(xx < yy ? xx : yy)*2;
		float jj = (float)Math.asin(Math.toRadians(30))*(xx < yy ? xx : yy);

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
			drawCurve(order, maxSize,-60);
		} else {
			turtle.turn(60);
			drawCurve(order, maxSize,-60);
		}

		return true;
	}


	private void drawCurve(int n, float distance,float angle) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		
		drawCurve(n-1,distance/2.0f,-angle);
		turtle.turn(angle);
		drawCurve(n-1,distance/2.0f,angle);
		turtle.turn(angle);
		drawCurve(n-1,distance/2.0f,-angle);
	}
}
