package com.marginallyclever.makelangelo.nodes;

import com.marginallyclever.core.node.Node;
import com.marginallyclever.makelangelo.nodeConnector.NodeConnectorTurtle;

/**
 * Subset of nodes which generate a path.
 * @author Dan Royer
 *
 */
abstract public class TurtleGenerator extends Node {
	// results
	public NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageGenerator.outputTurtle");
	
	protected TurtleGenerator() {
		super();
		outputs.add(outputTurtle);
	}
}
