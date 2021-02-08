package com.marginallyclever.core.node;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectDouble;
import com.marginallyclever.makelangelo.Translator;

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
		// TODO a fancy version with a dial that can be turned?
		// TODO a slider for doubles?
		return new SelectDouble(this.getName(),this.getValue());
	}
	
	@Override
	public String getDescription() {
		return super.getDescription()+Translator.get("NodeConnectorAngle.description");
	}
}
