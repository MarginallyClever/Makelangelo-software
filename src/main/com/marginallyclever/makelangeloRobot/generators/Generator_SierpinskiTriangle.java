package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import com.marginallyclever.makelangelo.Translator;

/**
 * see https://en.wikipedia.org/wiki/Sierpi%C5%84ski_arrowhead_curve
 * @author Dan Royer 2016-12-12
 *
 */
public class Generator_SierpinskiTriangle extends ImageGenerator {
	private Turtle turtle;
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
	public boolean generate(Writer out) throws IOException {
		imageStart(out);

		xMax = (float)(machine.getPaperWidth() * machine.getPaperMargin())/2.0f;
		yMax = (float)(machine.getPaperHeight() * machine.getPaperMargin())/2.0f;
		xMin = -xMax;
		yMin = -yMax;

		turtle = new Turtle();
		
		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = (float)Math.tan(Math.toRadians(30))*(xx < yy ? xx : yy)*2;
		float jj = (float)Math.asin(Math.toRadians(30))*(xx < yy ? xx : yy);
		liftPen(out);
		// move to starting position
		if(xMax>yMax) {
			turtle.setX(-jj);
			turtle.setY(yMin);
		} else {
			turtle.setX(xMax);
			turtle.setY(-jj);
			turtle.turn(90);
		}
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		if( (order&1) == 0 ) {
			drawCurve(out, order, maxSize,-60);
		} else {
			turtle.turn(60);
			drawCurve(out, order, maxSize,-60);
		}

		imageEnd(out);
		
		return true;
	}


	private void drawCurve(Writer output, int n, float distance,float angle) throws IOException {
		if (n == 0) {
			turtleMove(output,distance);
			return;
		}
		
		drawCurve(output,n-1,distance/2.0f,-angle);
		turtle.turn(angle);
		drawCurve(output,n-1,distance/2.0f,angle);
		turtle.turn(angle);
		drawCurve(output,n-1,distance/2.0f,-angle);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
