package com.marginallyclever.makelangelo.nodeBasedEditor;

import java.awt.*;

/**
 * Used by any class wanting to add decorations to a {@link NodeGraphViewPanel}.
 */
public interface NodeGraphViewListener {
    /**
     * Called when the {@link NodeGraphViewPanel} has completed painting itself.
     * Useful for then adding highlights and extra annotation.
     * @param g the graphics context used to paint the panel
     * @param panel the caller
     */
    public void paint(Graphics g, NodeGraphViewPanel panel);
}
