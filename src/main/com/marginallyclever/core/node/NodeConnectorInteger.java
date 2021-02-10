package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectInteger;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorInteger extends NodeConnector<Integer> {
	public NodeConnectorInteger(String newName) {
		super(newName);
	}
	
	public NodeConnectorInteger(String newName,Integer d) {
		super(newName,d);
	}

	@Override
	public Select getSelect() {
		SelectInteger s = new SelectInteger(this.getName(),this.getValue());
		s.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setValue((int)evt.getNewValue());
			}
		});
		return s;
	}
}
