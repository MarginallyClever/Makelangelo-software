package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

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
		Turtle turtle = new Turtle();
        turtle.penDown();
		gosperA(turtle,order);

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	// Gosper curve A = A-B--B+A++AA+B-
	private void gosperA(Turtle turtle,int n) {
		if (n == 0) {
			turtle.forward(1);
			return;
		}
        LSystem system = new LSystem();
        system.addRule("A","A-B--B+A++AA+B-");
        system.addRule("B","+A-BB--B-A++A+B");
        String result = system.generate("A",order);
        for(char command : result.toCharArray()) {
            switch(command) {
                case 'A':
                case 'B': turtle.forward(1); break;
                case '+': turtle.turn(60); break;
                case '-': turtle.turn(-60); break;
                // Ignore other characters
            }
        }
	}
}
