package com.marginallyclever.makelangelo.preview;

import com.jogamp.opengl.GL2;

/**
 * PreviewListener classes are waiting for their turn to render to the WYSIWYG interface in Makelangelo software.
 * @author Dan Royer
 *
 */
public abstract interface PreviewListener {
	public void render(GL2 gl2);
}
