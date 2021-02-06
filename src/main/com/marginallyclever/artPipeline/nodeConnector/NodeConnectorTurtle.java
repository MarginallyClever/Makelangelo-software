package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.convenience.turtle.Turtle;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorTurtle extends NodeConnector<Turtle> {
	static public final String NAME = "Turtle";
	
	public NodeConnectorTurtle() {
		super();
		setType(NAME);
	}
}
