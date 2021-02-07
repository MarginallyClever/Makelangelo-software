package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.convenience.nodes.NodeConnector;
import com.marginallyclever.convenience.turtle.Turtle;

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
