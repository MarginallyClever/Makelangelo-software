package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.node.NodeConnector;

public class NodeConnectorTransformedImage extends NodeConnector<TransformedImage> {
	public NodeConnectorTransformedImage(String newName) {
		super(newName);
	}
	
	public NodeConnectorTransformedImage(String newName,TransformedImage d) {
		super(newName,d);
	}
}
