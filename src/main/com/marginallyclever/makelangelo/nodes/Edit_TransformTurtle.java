package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.node.NodeConnectorAngle;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodeConnector.NodeConnectorTurtle;

/**
 * Rotate and scale a {@link Turtle}.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class Edit_TransformTurtle extends TurtleGenerator {
	// source of {@link Turtle}
	public NodeConnectorTurtle inputTurtle = new NodeConnectorTurtle(Translator.get("Edit_TransformTurtle.inputTurtle"),new Turtle()); 
	// degrees to rotate ccw
	public NodeConnectorAngle inputAngle = new NodeConnectorAngle(Translator.get("NodeConnectorAngle.inputAngle"),0.0);
	// amount to scale in all directions.  1.0 remains unchanged.
	public NodeConnectorDouble inputScale = new NodeConnectorDouble(Translator.get("NodeConnectorAngle.inputScale"),1.0);
	
	public Edit_TransformTurtle() {
		super();
		inputs.add(inputTurtle);
		inputs.add(inputAngle);
		inputs.add(inputScale);
	}
	
	@Override
	public String getName() {
		return Translator.get("Edit_TransformTurtle.name");
	}

	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle(inputTurtle.getValue());
		// scale
		double n = inputScale.getValue();
		turtle.scale(n,n);
		// rotate
		turtle.rotate(inputAngle.getValue());
		// save new results
		outputTurtle.setValue(turtle);
		
		return false;
	}
}
