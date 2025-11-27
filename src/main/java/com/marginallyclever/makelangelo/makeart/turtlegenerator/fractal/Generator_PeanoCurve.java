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
public class Generator_PeanoCurve extends TurtleGenerator {
	private static int order = 4; // controls complexity of curve

	public Generator_PeanoCurve() {
		super();

		SelectSlider fieldOrder;
		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),8,1, Generator_PeanoCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Peano_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
		fieldOrder.addSelectListener(evt->{
			Generator_PeanoCurve.setOrder(Math.max(1,fieldOrder.getValue()));
			generate();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("PeanoCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_PeanoCurve.order = order;
	}

	@Override
	public void generate() {
		Turtle turtle = new Turtle();
        turtle.penDown();
		peano(turtle,order);

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	// Hilbert curve
	private void peano(Turtle turtle, int n) {
        if (n == 0) return;
        // Define the Peano curve rules
        LSystem lSystem = new LSystem();
        lSystem.addRule("X", "XFYFX+F+YFXFY−F−XFYFX");
        lSystem.addRule("Y", "YFXFY−F−XFYFX+F+YFXFY");
        String result = lSystem.generate("X", order);

        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(90); break;
                case '−': turtle.turn(-90); break;
                // Ignore other characters
            }
        }
    }
}
