package com.marginallyclever.makelangelo.nodes.fractals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.NodeConnectorBoundedInt;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.nodes.TurtleGenerator;

/**
 * Dragon fractal
 * @author Dan Royer
 */
public class Generator_Dragon extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorInteger inputOrder = new NodeConnectorBoundedInt("Generator_Dragon.inputOrder",15,1,5);

	private List<Integer> sequence;

	
	public Generator_Dragon() {
		super();
		inputs.add(inputOrder);
	}
	
	@Override
	public String getName() {
		return Translator.get("Generator_Dragon.name");
	}
		
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();

		// create the sequence of moves
		int count = inputOrder.getValue();
        sequence = new ArrayList<Integer>();
        for (int i = 0; i < count; i++) {
            List<Integer> copy = new ArrayList<Integer>(sequence);
            Collections.reverse(copy);
            sequence.add(1);
            for (Integer turn : copy) {
                sequence.add(-turn);
            }
        }
        System.out.println("Count = "+count);
        
		// move to starting position
        turtle.penDown();
		// draw the fractal
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtle.forward(1);
        }  

		outputTurtle.setValue(turtle);
	    return false;
	}
}
