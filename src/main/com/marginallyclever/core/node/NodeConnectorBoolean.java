package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectBoolean;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorBoolean extends NodeConnector<Boolean> {
	public NodeConnectorBoolean(String newName) {
		super(newName);
	}

	public NodeConnectorBoolean(String newName,Boolean d) {
		super(newName,d);
	}

	@Override
	public Select getSelect() {
		SelectBoolean s = new SelectBoolean(this.getName(),this.getValue());
		s.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setValue((boolean)evt.getNewValue());
			}
		});
		return s;
	}
}
