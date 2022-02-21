package com.marginallyclever.nodeBasedEditor;

/**
 * {@link com.marginallyclever.nodeBasedEditor.model.Node}s with this interface can draw to anything that extends
 * Swing's {@link java.awt.Graphics} class.
 * TODO don't drag Swing into the model!
 */
public interface PrintWithGraphics {
    void print(java.awt.Graphics g);
}
