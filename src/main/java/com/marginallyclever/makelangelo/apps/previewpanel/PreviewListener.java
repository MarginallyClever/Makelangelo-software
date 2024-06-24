package com.marginallyclever.makelangelo.apps.previewpanel;

import com.jogamp.opengl.GL2;

import java.util.EventListener;

/**
 * {@link PreviewListener} instances can register to the {@link PreviewPanel}.
 * {@link PreviewPanel} will call {@code render()} on all PreviewListeners in the order
 * they are registered, and pass to the listeners an OpenGL {@link GL2} render context.
 * @author Dan Royer
 */
public interface PreviewListener extends EventListener {
	/**
	 * Callback from {@link PreviewPanel} that it is time to render to the WYSIWYG display.
	 * @param gl2 the render context
	 */
	void render(GL2 gl2);
}
