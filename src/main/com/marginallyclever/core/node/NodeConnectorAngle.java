package com.marginallyclever.core.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectDouble;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorAngle extends NodeConnectorDouble {
	public NodeConnectorAngle(String newName) {
		super(newName);
	}
	
	public NodeConnectorAngle(String newName,Double d) {
		super(newName,d);
	}

	@Override
	public Double getValue() {
		return super.getValue() % 360;
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
		// TODO a fancy version with a dial that can be turned?
		// TODO a slider for doubles?
		return s;
	}
	
	@Override
	public String getDescription() {
		return super.getDescription()+Translator.get("NodeConnectorAngle.description");
	}
}
