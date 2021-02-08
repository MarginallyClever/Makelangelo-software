package com.marginallyclever.core.node;

import java.lang.reflect.ParameterizedType;

/**
 * {@link NodeConnector} describes the inputs and outputs of each {@link Node}.  One output may connect to many inputs.
 * The {@link NodeConnector#isDirty} flag indicates when the value in this connector is out of date.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class NodeConnector<T> {
	// the name of this connector node, which is also used for the name of each UX dialog element.
	private String name;
	
	// helpful text to explain this input. 
	private String description = "";
	
	// To obtain the name of class T (which is normally erased at run time)
	private final Class<T> persistentClass;

	// The current value.
	private T value;
	
	// Has this peg been updated recently?
	public boolean isDirty;
	
	public String getType() {
		return persistentClass.getSimpleName();
	}

	public void setType(String newTypeName) {}
	
	
	@SuppressWarnings("unchecked")
	protected NodeConnector(String newName) {
		super();
		setName(newName);
		persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	protected NodeConnector(String name,T defaultValue) {
		this(name);
		value = defaultValue;
	}
	
	public void setValue(T v) {
		value=v;
		isDirty=true;
	}
	
	public T getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	private void setName(String newName) {
		this.name = newName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
