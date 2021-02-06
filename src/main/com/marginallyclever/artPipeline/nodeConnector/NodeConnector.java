package com.marginallyclever.artPipeline.nodeConnector;

/**
 * {@link NodeConnector} describes the inputs and outputs of each {@link Node}.  One output may connect to many inputs.
 * The {@link NodeConnector#isDirty} flag indicates when the value in this connector is out of date.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnector<T> {
	private String typeName;

	public String getType() {
		return typeName;
	}

	public void setType(String name) {
		this.typeName = name;
	}
	
	// The current value.
	private T value;
	
	// Has this peg been updated recently?
	public boolean isDirty;
	
	public NodeConnector() {
		super();
	}
	
	public void setValue(T v) {
		value=v;
		isDirty=true;
	}
	
	public T getValue() {
		return value;
	}
}
