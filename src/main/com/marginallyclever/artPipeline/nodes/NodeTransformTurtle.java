package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.core.node.NodeConnectorAngle;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Rotate and scale a {@link Turtle}.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeTransformTurtle extends TurtleGenerator {
	public NodeConnectorTurtle inputTurtle = new NodeConnectorTurtle(Translator.get("NodeConnectorTurtle.inputTurtle"),new Turtle()); 
	public NodeConnectorAngle inputAngle = new NodeConnectorAngle(Translator.get("NodeConnectorAngle.inputAngle"),0.0);
	public NodeConnectorDouble inputScale = new NodeConnectorDouble(Translator.get("NodeConnectorAngle.inputScale"),1.0);
	
	public NodeTransformTurtle() {
		super();
		inputs.add(inputTurtle);
		inputs.add(inputAngle);
		inputs.add(inputScale);
	}
	
	@Override
	public String getName() {
		return Translator.get("NodeTransformTurtle.name");
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
