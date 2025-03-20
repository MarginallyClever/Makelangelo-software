package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * See <a href="https://en.wikipedia.org/wiki/Sierpi%C5%84ski_arrowhead_curve">Wikipedia</a>
 * @author Dan Royer
 * @since 2016-12-12
 */
@Deprecated(since = "7.68.0")
public class Generator_SierpinskiTriangle extends TurtleGenerator {
	private static int order = 4; // controls complexity of curve

	public Generator_SierpinskiTriangle() {
		super();

		SelectSlider field_order;

		add(field_order = new SelectSlider("order",Translator.get("HilbertCurveOrder"),10,1,Generator_SierpinskiTriangle.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Sierpi%C5%84ski_triangle'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
		field_order.addSelectListener(evt-> {
			Generator_SierpinskiTriangle.setOrder(Math.max(1, field_order.getValue()));
			generate();
		});
	}

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
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xMax = rect.getWidth() / 2.0f;
		double yMax = rect.getHeight() / 2.0f;
		double xMin = -xMax;
		double yMin = -yMax;

		Turtle turtle = new Turtle();
		
		double xx = xMax - xMin;
		double yy = yMax - yMin;
		double maxSize = Math.tan(Math.toRadians(30)) * (Math.min(xx, yy)) * 2;
		double jj = Math.asin(Math.toRadians(30)) * (Math.min(xx, yy));

		// move to starting position
		if(xMax > yMax) {
			turtle.moveTo(-jj, yMin);
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

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
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
