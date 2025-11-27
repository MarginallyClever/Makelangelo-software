package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Hilbert Curve fractal.
 * @author Dan Royer
 */
@Deprecated(since = "7.68.0")
public class Generator_HilbertCurve extends TurtleGenerator {
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
        Turtle turtle = new Turtle();
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
        LSystem system = new LSystem();
        system.addRule("A","+BF-AFA-FB+");
        system.addRule("B","-AF+BFB+FA-");
        String result = system.generate("A", n);

        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(90); break;
                case '-': turtle.turn(-90); break;
                // Ignore other characters
            }
        }
	}
}
