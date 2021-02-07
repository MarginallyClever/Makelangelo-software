package com.marginallyclever.convenience.nodes;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorString extends NodeConnector<String> {
	public NodeConnectorString(String newName,String defaultValue) {
		super(newName,defaultValue);
	}
	public NodeConnectorString(String newName) {
		super(newName,"");
	}
}
