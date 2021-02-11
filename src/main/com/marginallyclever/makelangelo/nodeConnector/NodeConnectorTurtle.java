package com.marginallyclever.makelangelo.nodeConnector;

import com.marginallyclever.core.node.NodeConnector;
import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectReadOnlyText;
import com.marginallyclever.core.turtle.Turtle;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorTurtle extends NodeConnector<Turtle> {
	public NodeConnectorTurtle(String newName) {
		super(newName);
	}

	public NodeConnectorTurtle(String newName,Turtle d) {
		super(newName,d);
	}

	@Override
	public Select getSelect() {
		// TODO in read-only mode, show a turtle icon
		// TODO in read/write mode, show a turtle icon
		// TODO SelectTransformedImage(label,source filename)
		return new SelectReadOnlyText("Turtle "+getName());
	}
}
