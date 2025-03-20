package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Gosper curve fractal.
 * @author Dan Royer
 */
@Deprecated(since = "7.68.0")
public class Generator_GosperCurve extends TurtleGenerator {
	private static int order = 4; // controls complexity of curve

	public Generator_GosperCurve() {
		super();

		SelectSlider fieldOrder;
		add(fieldOrder = new SelectSlider("order",
				Translator.get("HilbertCurveOrder")// As this is the same concept and translation value but this translation key is confusing as we are in GosperCurve_Panle ...
				,6,1,Generator_GosperCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Gosper_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));

		fieldOrder.addSelectListener(evt-> {
			order = Math.max(1, fieldOrder.getValue());
			generate();
		});
	}
	
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
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double v = Math.min(rect.getWidth(),rect.getHeight());

		Turtle turtle = new Turtle();
		turtle.jumpTo(myPaper.getCenterX(),myPaper.getCenterY());
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

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

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
