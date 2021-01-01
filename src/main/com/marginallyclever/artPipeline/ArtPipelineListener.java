package com.marginallyclever.artPipeline;

import com.marginallyclever.convenience.Turtle;

public interface ArtPipelineListener {
	/**
	 * Called whenever the pipeline is done processing a Turtle.
	 * @param t
	 */
	public void turtleFinished(Turtle t);
}
