package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.EventListener;

/**
 * {@link TurtleModifierListener} subscribe to {@link TurtleModifierAction}s in order
 * to be notified when the action is complete.
 * @author Dan Royer
 * @since 7.31.0
 */
public interface TurtleModifierListener extends EventListener {
	/**
	 * @param turtle the result of a {@link TurtleModifierAction} being run.
	 */
	void turtleModifiedEvent(Turtle turtle);
}
