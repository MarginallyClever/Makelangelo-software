package com.marginallyclever.core.node;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorInt extends NodeConnector<Integer> {
	public NodeConnectorInt(String newName) {
		super(newName);
	}
	
	public NodeConnectorInt(String newName,Integer d) {
		super(newName,d);
	}
}
