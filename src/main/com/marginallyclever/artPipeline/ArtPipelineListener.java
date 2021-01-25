package com.marginallyclever.artPipeline;

import com.marginallyclever.convenience.turtle.Turtle;

/**
 * Whomsoever uses this listener is waiting to know when the pipeline has prepared a new Turtle
 * @author Dan Royer
 *
 */
public interface ArtPipelineListener {
	/**
	 * Called whenever the pipeline is done processing a Turtle.
	 * @param t
	 */
	public void turtleFinished(Turtle t);
}
