package com.marginallyclever.artPipeline.nodeConnector;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorDouble extends NodeConnector<Double> {
	static public final String NAME = "Double";
	
	public NodeConnectorDouble() {
		super();
		setType(NAME);
	}
}
