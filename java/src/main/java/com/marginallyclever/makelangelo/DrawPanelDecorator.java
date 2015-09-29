package com.marginallyclever.makelangelo;

import com.jogamp.opengl.GL2;

/**
 * DrawDecorator adds features to DrawPanel. An example is the Draw panel shows
 * WYSIWYG progress, while the filters might show conversion while-you-wait
 * updates.
 *
 * @author danroyer
 * @since 7.1.4
 */
public interface DrawPanelDecorator {
  void render(GL2 gl2, MakelangeloRobot machine);
}
