package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Hilbert Curve fractal.
 * @author Dan Royer
 */
@Deprecated(since = "7.68.0")
public class Generator_HilbertCurve extends TurtleGenerator {
	private float turtleStep = 10.0f;
	private static int order = 4; // controls complexity of curve

	public Generator_HilbertCurve() {
		super();

		SelectSlider fieldOrder;
		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),8,1,Generator_HilbertCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Hilbert_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
		fieldOrder.addSelectListener(evt->{
			Generator_HilbertCurve.setOrder(Math.max(1,fieldOrder.getValue()));
			generate();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("HilbertCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_HilbertCurve.order = order;
	}

	@Override
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double v = Math.min(rect.getWidth(),rect.getHeight());
		double xMin = -v;

		Turtle turtle = new Turtle();
		turtleStep = (float) ((v - xMin) / (Math.pow(2, order)));

		// move to starting position
		turtle.jumpTo(
				-v + turtleStep / 2,
				-v + turtleStep / 2);
		turtle.penDown();
		hilbert(turtle,order);

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	// Hilbert curve
	private void hilbert(Turtle turtle,int n) {
		if (n == 0) return;
		turtle.turn(90);
		treblih(turtle, n - 1);
		turtle.forward(turtleStep);
		turtle.turn(-90);
		hilbert(turtle, n - 1);
		turtle.forward(turtleStep);
		hilbert(turtle, n - 1);
		turtle.turn(-90);
		turtle.forward(turtleStep);
		treblih(turtle, n - 1);
		turtle.turn(90);
	}


	// evruc trebliH
	public void treblih(Turtle turtle,int n) {
		if (n == 0) return;
		turtle.turn(-90);
		hilbert(turtle, n - 1);
		turtle.forward(turtleStep);
		turtle.turn(90);
		treblih(turtle, n - 1);
		turtle.forward(turtleStep);
		treblih(turtle, n - 1);
		turtle.turn(90);
		turtle.forward(turtleStep);
		hilbert(turtle, n - 1);
		turtle.turn(-90);
	}
}
