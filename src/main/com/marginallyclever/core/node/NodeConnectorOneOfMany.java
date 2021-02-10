package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectOneOfMany;

/**
 * Convenience class.
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorOneOfMany extends NodeConnectorInteger {
	private String [] mySelection;
	
	
	public NodeConnectorOneOfMany(String newName) {
		super(newName);
	}
	
	public NodeConnectorOneOfMany(String newName,Integer d) {
		super(newName,d);
	}
	
	public NodeConnectorOneOfMany(String newName,String [] selection,Integer d) {
		super(newName,d);
		mySelection = selection;
	}

	@Override
	public Select getSelect() {
		SelectOneOfMany s = new SelectOneOfMany(this.getName(),mySelection,this.getValue());
		s.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setValue((int)evt.getNewValue());
			}
		});
		return s;
	}
}
