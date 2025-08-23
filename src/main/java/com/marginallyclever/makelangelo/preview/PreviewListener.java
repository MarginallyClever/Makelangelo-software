package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.GL3;

/**
 * {@link PreviewListener} instances can register to the {@link OpenGLPanel}.
 * {@link OpenGLPanel} will call {@code render()} on all PreviewListeners in the order
 * they are registered, and pass to the listeners an OpenGL {@link GL3} render context.
 * @author Dan Royer
 */
public interface PreviewListener {
	/**
	 * Callback from {@link OpenGLPanel} that it is time to render to the WYSIWYG display.
	 *
	 * @param shader the render context
	 */
	void render(ShaderProgram shader,GL3 gl);
	default void dispose() {}
}
