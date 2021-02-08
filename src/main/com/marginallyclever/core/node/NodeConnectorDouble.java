package com.marginallyclever.core.node;

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
		return new SelectDouble(this.getName(),this.getValue());
	}
}
