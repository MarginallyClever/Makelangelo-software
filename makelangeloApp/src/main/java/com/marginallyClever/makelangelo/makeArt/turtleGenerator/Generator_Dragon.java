package com.marginallyClever.makelangelo.makeArt.turtleGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.turtle.Turtle;

/**
 * Dragon fractal
 * @author Dan Royer
 */
public class Generator_Dragon extends TurtleGenerator {
	private static int order = 12; // controls complexity of curve

	private List<Integer> sequence;

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
	public TurtleGeneratorPanel getPanel() {
		return new Generator_Dragon_Panel(this);
	}
		
	@Override
	public void generate() {
		Turtle turtle = new Turtle();

		// create the sequence of moves
        sequence = new ArrayList<Integer>();
        for (int i = 0; i < order; i++) {
            List<Integer> copy = new ArrayList<Integer>(sequence);
            Collections.reverse(copy);
            sequence.add(1);
            for (Integer turn : copy) {
                sequence.add(-turn);
            }
        }
        
		// move to starting position
        turtle.penDown();
		// draw the fractal
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtle.forward(1);
        }  

        notifyListeners(turtle);
	}
}
