package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * {@link TurtleModifierListener} subscribe to {@link TurtleModifierAction}s in order
 * to be notified when the action is complete.
 * @author Dan Royer
 * @since 7.31.0
 */
public abstract interface TurtleModifierListener {
	/**
	 * @param turtle the result of a {@link TurtleModifierAction} being run.
	 */
	public void turtleModifiedEvent(Turtle turtle);
}
