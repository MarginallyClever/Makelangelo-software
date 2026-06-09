package com.marginallyclever.makelangelo.editorcontext;

import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.EventListener;

/**
 * Listener for turtle change events
 */
public interface EditorContextListener extends EventListener {
    // received when the turtle changes in the editor context.
    // This is a signal to update the UI and re-render the preview.
    void turtleChanged(Turtle subject);
}
