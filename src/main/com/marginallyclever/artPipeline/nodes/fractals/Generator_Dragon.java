package com.marginallyclever.artPipeline.nodes.fractals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marginallyclever.artPipeline.nodes.TurtleGenerator;
import com.marginallyclever.core.node.NodeConnectorInt;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Dragon fractal
 * @author Dan Royer
 */
public class Generator_Dragon extends TurtleGenerator {
	// controls complexity of curve
	private NodeConnectorInt inputOrder = new NodeConnectorInt("Generator_Dragon.outputTurtle",12);

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
        sequence = new ArrayList<Integer>();
        for (int i = 0; i < inputOrder.getValue(); i++) {
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

		outputTurtle.setValue(turtle);
	    return false;
	}
}
