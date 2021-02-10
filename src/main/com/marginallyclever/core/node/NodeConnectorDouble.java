package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectDouble;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorDouble extends NodeConnector<Double> {
	public NodeConnectorDouble(String newName) {
		super(newName);
	}
	
	public NodeConnectorDouble(String newName,Double d) {
		super(newName,d);
	}

	@Override
	public Select getSelect() {
		SelectDouble s = new SelectDouble(this.getName(),this.getValue());
		s.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setValue((double)evt.getNewValue());
			}
		});
		return s;
	}
}
