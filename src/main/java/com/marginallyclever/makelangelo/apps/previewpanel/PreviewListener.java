package com.marginallyclever.makelangelo.apps.previewpanel;

import java.util.EventListener;

/**
 * {@link PreviewListener} instances can register to the {@link PreviewPanel}.
 * {@link PreviewPanel} will call {@code render()} on all PreviewListeners in the order
 * they are registered, and pass to the listeners an OpenGL render context.
 * @author Dan Royer
 */
public interface PreviewListener extends EventListener {
	/**
	 * Callback from {@link PreviewPanel} that it is time to render to the WYSIWYG display.
	 * @param gl the render context
	 */
	void render(RenderContext context);
}
