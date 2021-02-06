package com.marginallyclever.artPipeline.nodeConnector;

import com.marginallyclever.convenience.TransformedImage;

public class NodeConnectorTransformedImage extends NodeConnector<TransformedImage> {
	static public final String NAME = "TransformedImage";
	
	public NodeConnectorTransformedImage() {
		super();
		setType(NAME);
	}
}
