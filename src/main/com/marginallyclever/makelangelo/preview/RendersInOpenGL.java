package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.GL2;

/**
 * Classes which implement {@link RendersInOpenGL} can register with the {@link OpenGLPanel} to be notified when
 * it is their turn to draw into the 3D view.
 * @author Dan Royer
 *
 */
public abstract interface RendersInOpenGL {
	/**
	 * Be careful when modifying the gl2 state!
	 * 
	 * @param gl2 the object with which contains all primitive drawing commands.
	 */
	public void render(GL2 gl2);
}
