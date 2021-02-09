package com.marginallyclever.core.node;

import com.marginallyclever.core.select.Select;

/**
 * {@link NodeConnector} describes the inputs and outputs of each {@link Node}.  One output may connect to many inputs.
 * The {@link NodeConnector#isDirty} flag indicates when the value in this connector is out of date.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
abstract public class NodeConnector<T> {
	// the name of this connector node, which is also used for the name of each UX dialog element.
	private String name;
	
	// helpful text to explain this input. 
	private String description = "<i>No description available</i>";
	

	// The current value.
	private T value;
	
	// Has this peg been updated recently?
	public boolean isDirty;

	public void setType(String newTypeName) {}
	
	
	protected NodeConnector(String newName) {
		super();
		setName(newName);
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

	/**
	 * Get the {@link Select} component that best represents this {@link NodeConnector}. 
	 */
	abstract public Select getSelect();
}
