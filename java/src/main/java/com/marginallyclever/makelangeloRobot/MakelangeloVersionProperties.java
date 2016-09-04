package com.marginallyclever.makelangeloRobot;

import com.jogamp.opengl.GL2;

/**
 * Properties of each Makelangelo version.  These are non-configurable features unique to each machine.
 * @author Dan Royer
 *
 */
public interface MakelangeloVersionProperties {
	public boolean canChangeMachineWidth();
	public boolean canAccelerate();
	
	/**
	 * custom look & feel for each version
	 * @param gl2
	 */
	public void render(GL2 gl2,MakelangeloRobot robot);
}
