package com.marginallyclever.convenience.nodes;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorDouble extends NodeConnector<Double> {
	public NodeConnectorDouble(String newName) {
		super(newName);
	}
	
	public NodeConnectorDouble(String newName,Double d) {
		super(newName,d);
	}
}
