package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Koch Curve fractal
 * @author Dan Royer
 */
@Deprecated(since = "7.68.0")
public class Generator_KochCurve extends TurtleGenerator {
	private static int order = 4; // controls complexity of curve

	public Generator_KochCurve() {
		super();

		SelectSlider fieldOrder;

		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),7,1,Generator_HilbertCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Koch_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));

		fieldOrder.addSelectListener(evt->{
			setOrder(fieldOrder.getValue());
			generate();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("KochTreeName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_KochCurve.order = order;
	}

	@Override
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double v = Math.min(rect.getWidth(),rect.getHeight());
		double xMin = -v;
		double yMin = -v;

		Turtle turtle = new Turtle();
		
		double xx = v - xMin;
		double yy = v - yMin;
		double maxSize = Math.max(xx, yy);
		
		// move to starting position
		if(myPaper.getPaperWidth() > myPaper.getPaperHeight()) {
			turtle.moveTo(-v,0);
		} else {
			turtle.moveTo(0,-v);
			turtle.turn(90);
		}
		
		turtle.penDown();
		drawTriangle(turtle,order, maxSize);

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	// L System tree
	private void drawTriangle(Turtle turtle,int n, double distance) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		drawTriangle(turtle,n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			drawTriangle(turtle,n-1,distance/3.0f);
			turtle.turn(120);
			drawTriangle(turtle,n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtle.forward(distance/3.0f);
		}
		drawTriangle(turtle,n-1,distance/3.0f);
	}
}
