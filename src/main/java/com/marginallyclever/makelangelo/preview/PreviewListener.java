package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.GL2;

/**
 * {@link PreviewListener} instances can register to the {@link PreviewPanel}.
 * {@link PreviewPanel} will call {@code render()} on all PreviewListeners in the order
 * they are registered, and pass to the listeners an OpenGL {@link GL2} render context.
 * @author Dan Royer
 */
public interface PreviewListener {
	/**
	 * Callback from {@link PreviewPanel} that it is time to render to the WYSIWYG display.
	 * @param gl2 the render context
	 */
	public void render(GL2 gl2);
}
