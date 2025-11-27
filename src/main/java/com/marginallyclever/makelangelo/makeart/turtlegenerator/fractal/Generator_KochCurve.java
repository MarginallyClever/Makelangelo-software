package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

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
		Turtle turtle = new Turtle();
		turtle.penDown();
		drawTriangle(turtle,order);

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	// L System tree
	private void drawTriangle(Turtle turtle,int n) {
        if (n == 0) {
            turtle.forward(1);
            return;
        }

        LSystem system = new LSystem();
        //system.addRule("F","F+F-F-F+F");
        system.addRule("F","F+F--F+F");

        String result = system.generate("F", n);
        for(char command : result.toCharArray()) {
            switch(command) {
                case 'F': turtle.forward(1); break;
                case '+': turtle.turn(60); break;
                case '-': turtle.turn(-60); break;
                // Ignore other characters
            }
        }
	}
}
