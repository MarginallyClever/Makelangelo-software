package com.marginallyclever.internalFormat;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

public class ToolPath {
	final int FLOATS_PER_VERTEX = 3;
	final int NUM_BUFFERS = 1;
	final int BITS_PER_BYTE = 8;
	final int BYTES_PER_FLOAT=(Float.SIZE/BITS_PER_BYTE);  // bits per float / bits per byte
	
	
	protected Color color;
	protected ArrayList<ToolPathSegment> segments;
	protected FloatBuffer vertices;  // optimized rendering for opengl
	protected transient int VBO[];
	protected int vertexArraySize;

	
	public ToolPath() {
		VBO=null;
	}
	
	
	private void createBuffers() {
		GL2 gl2 = GLContext.getCurrentGL().getGL2();
		VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);
	}
	

	public void addPathSegment(ToolPathSegment seg) {
		segments.add(seg);
	}
	
	
	void updateBuffers() {
		Iterator<ToolPathSegment> i = segments.iterator();
		vertexArraySize = 0;
		while(i.hasNext()) {
			vertexArraySize += i.next().getVertexCount();
		}

		// create the buffer
		vertices = FloatBuffer.allocate(vertexArraySize*FLOATS_PER_VERTEX);
		
		// fill the buffer
		//@TODO

		GL2 gl2 = GLContext.getCurrentGL().getGL2();
		
		// bind a buffer
		vertices.rewind();
		if(VBO==null) createBuffers();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
	    // Write out vertex buffer to the currently bound VBO.
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vertexArraySize*FLOATS_PER_VERTEX*BYTES_PER_FLOAT, vertices, GL2.GL_STATIC_DRAW);
	}
	
	
	void draw() {
		if(VBO==null) return;

		GL2 gl2 = GLContext.getCurrentGL().getGL2();
		
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		// Bind the vertex buffer to work with
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
		gl2.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
		
		gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, vertexArraySize);
	}
}
