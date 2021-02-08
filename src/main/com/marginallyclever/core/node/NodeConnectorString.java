package com.marginallyclever.core.node;

import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectString;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorString extends NodeConnector<String> {
	public NodeConnectorString(String newName,String defaultValue) {
		super(newName,defaultValue);
	}
	public NodeConnectorString(String newName) {
		super(newName,"");
	}

	@Override
	public Select getSelect() {
		return new SelectString(this.getName(),this.getValue());
	}
}
