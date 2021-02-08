package com.marginallyclever.core.node;

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
		return new SelectBoolean(this.getName(),this.getValue());
	}
}
