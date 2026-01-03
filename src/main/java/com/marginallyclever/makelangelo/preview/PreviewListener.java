package com.marginallyclever.makelangelo.preview;

import java.awt.*;

/**
 * {@link PreviewListener} instances can register to the {@link OpenGLPanel}.
 * {@link OpenGLPanel} will call {@code render()} on all PreviewListeners in the order
 * they are registered, and pass to the listeners a render context.
 * @author Dan Royer
 */
public interface PreviewListener {
	/**
	 * Callback from {@link OpenGLPanel} that it is time to render to the WYSIWYG display.
	 *
	 * @param graphics the render context
	 */
	void render(Graphics graphics);
}
