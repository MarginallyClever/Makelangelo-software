package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.core.node.Node;

/**
 * Subset of nodes which generate a path.
 * @author Dan Royer
 *
 */
abstract public class TurtleGenerator extends Node {
	// results
	protected NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageGenerator.outputTurtle");
	
	protected TurtleGenerator() {
		super();
		outputs.add(outputTurtle);
	}
}
