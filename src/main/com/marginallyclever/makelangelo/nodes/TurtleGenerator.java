package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.makelangelo.nodeConnector.NodeConnectorTurtle;

/**
 * Subset of nodes which generate a path.
 * @author Dan Royer
 *
 */
abstract public class TurtleGenerator extends Node {
	// results
	protected NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageGenerator.outputTurtle");
	protected NodeConnectorDouble inputWidth = new NodeConnectorDouble("ImageGenerator.inputWidth",100.0);
	protected NodeConnectorDouble inputHeight = new NodeConnectorDouble("ImageGenerator.inputHeight",100.0);
	
	protected TurtleGenerator() {
		super();
		inputs.add(inputWidth);
		inputs.add(inputHeight);
		outputs.add(outputTurtle);
	}
	
	public void setWidth(double w) {
		inputWidth.setValue(w);
	}

	public void setHeight(double h) {
		inputHeight.setValue(h);
	}
}
