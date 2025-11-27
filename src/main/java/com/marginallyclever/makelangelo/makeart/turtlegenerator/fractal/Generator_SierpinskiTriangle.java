package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.turtle.Turtle;

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
		Turtle turtle = new Turtle();
        turtle.penDown();
        drawCurve(turtle,order,120);

        // scale turtle to fit paper
        ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
        turtle = action.run(turtle);

		notifyListeners(turtle);
	}


	private void drawCurve(Turtle turtle,int n,double angle) {
		if (n == 0) {
			turtle.forward(1);
			return;
		}

        LSystem system = new LSystem();
        system.addRule("F","F-G+F+G-F");
        system.addRule("G","GG");
        String result = system.generate("F-G-G",n);

        for(char c : result.toCharArray()) {
            switch (c) {
                case 'F':
                case 'G': turtle.forward(1); break;
                case '+': turtle.turn(angle); break;
                case '-': turtle.turn(-angle); break;
                // Ignore other characters
            }
        }
	}
}
