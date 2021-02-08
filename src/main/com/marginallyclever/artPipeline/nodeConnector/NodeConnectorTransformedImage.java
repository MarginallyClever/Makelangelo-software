package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.node.NodeConnector;
import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectReadOnlyText;

/**
 * Convenience class
 * @author Dan Royer
 * @since 7.25.0
 */
public class NodeConnectorTransformedImage extends NodeConnector<TransformedImage> {
	public NodeConnectorTransformedImage(String newName) {
		super(newName);
	}
	
	public NodeConnectorTransformedImage(String newName,TransformedImage d) {
		super(newName,d);
	}

	@Override
	public Select getSelect() {
		// TODO in read-only mode, show a thumbnail.
		// TODO in read/write mode, show a thumbnail AND a file selection dialog.
		// TODO SelectTransformedImage(label,source filename)
		return new SelectReadOnlyText("TransformedImage "+getName());
	}
}
