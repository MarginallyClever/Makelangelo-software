package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.convenience.TransformedImage;
import com.marginallyclever.convenience.nodes.NodeConnector;

public class NodeConnectorTransformedImage extends NodeConnector<TransformedImage> {
	public NodeConnectorTransformedImage(String newName) {
		super(newName);
	}
	
	public NodeConnectorTransformedImage(String newName,TransformedImage d) {
		super(newName,d);
	}
}
