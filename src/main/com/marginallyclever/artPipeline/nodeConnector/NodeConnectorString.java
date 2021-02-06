package com.marginallyclever.artPipeline.nodeConnector;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorString extends NodeConnector<String> {
	static public final String NAME = "String";
	
	public NodeConnectorString() {
		super();
		setType(NAME);
	}
}
