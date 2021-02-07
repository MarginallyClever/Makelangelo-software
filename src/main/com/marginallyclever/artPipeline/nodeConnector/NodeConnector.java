package com.marginallyclever.artPipeline.nodeConnector;

import java.lang.reflect.ParameterizedType;

/**
 * {@link NodeConnector} describes the inputs and outputs of each {@link Node}.  One output may connect to many inputs.
 * The {@link NodeConnector#isDirty} flag indicates when the value in this connector is out of date.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnector<T> {
	private final Class<T> persistentClass;
	
	public String getType() {
		return persistentClass.getSimpleName();
	}

	public void setType(String name) {}
	
	// The current value.
	private T value;
	
	// Has this peg been updated recently?
	public boolean isDirty;
	
	@SuppressWarnings("unchecked")
	public NodeConnector() {
		super();
		persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	public NodeConnector(T defaultValue) {
		this();
		value = defaultValue;
	}
	
	public void setValue(T v) {
		value=v;
		isDirty=true;
	}
	
	public T getValue() {
		return value;
	}
}
