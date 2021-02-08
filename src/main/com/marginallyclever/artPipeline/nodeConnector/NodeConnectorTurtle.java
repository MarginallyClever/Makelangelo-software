package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.core.node.NodeConnector;
import com.marginallyclever.core.turtle.Turtle;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorTurtle extends NodeConnector<Turtle> {
	public NodeConnectorTurtle(String newName) {
		super(newName);
	}

	public NodeConnectorTurtle(String newName,Turtle d) {
		super(newName,d);
	}
}
