package com.marginallyclever.internalFormat;

public class ToolPathSegment {
	float x,y,z;  // position
	
	/**
	 * @return number of vertexes needed to render this path segment
	 */
	public int getVertexCount() {
		return 1;
	}
}
