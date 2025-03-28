package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dragon fractal
 * @author Dan Royer
 */
@Deprecated(since = "7.68.0")
public class Generator_Dragon extends TurtleGenerator {
	private static int order = 12; // controls complexity of curve
	private final SelectSlider fieldOrder;

	private final List<Integer> sequence = new ArrayList<>();

	public Generator_Dragon() {
		super();

		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),16,0,Generator_Dragon.getOrder()));
		fieldOrder.addSelectListener(evt->{
			order = Math.max(1,fieldOrder.getValue());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Dragon_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	@Override
	public String getName() {
		return Translator.get("DragonName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int value) {
		if(value<1) value=1;
		order = value;
	}

	@Override
	public void generate() {
		Turtle turtle = new Turtle();

		// create the sequence of moves
        sequence.clear();
        for (int i = 0; i < order; i++) {
            List<Integer> copy = new ArrayList<>(sequence);
            Collections.reverse(copy);
            sequence.add(1);
            for (Integer turn : copy) {
                sequence.add(-turn);
            }
        }
        
		// move to starting position
		turtle.jumpTo(myPaper.getCenterX(),myPaper.getCenterY());

        turtle.penDown();
		// draw the fractal
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtle.forward(1);
        }

		// scale turtle to fit paper
		ResizeTurtleToPaperAction action = new ResizeTurtleToPaperAction(myPaper,false,null);
		turtle = action.run(turtle);

        notifyListeners(turtle);
	}
}
