package com.marginallyclever.artPipeline.nodeConnector;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnectorInt extends NodeConnector<Integer> {
	static public final String NAME = "Integer";
	
	public NodeConnectorInt() {
		super();
		setType(NAME);
	}
}
