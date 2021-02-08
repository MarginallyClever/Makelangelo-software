package com.marginallyclever.core.node;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorBoolean extends NodeConnector<Boolean> {
	public NodeConnectorBoolean(String newName) {
		super(newName);
	}

	public NodeConnectorBoolean(String newName,Boolean d) {
		super(newName,d);
	}
}
