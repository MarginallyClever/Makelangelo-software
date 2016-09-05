package com.marginallyclever.makelangelo.settings;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * Properties of each Makelangelo version.  These are non-configurable features unique to each machine.
 * @author Dan Royer
 *
 */
public interface MakelangeloVersionProperties {
	public boolean canChangeMachineSize();
	public boolean canAccelerate();
	public boolean canChangePulleySize();
	
	/**
	 * custom look & feel for each version
	 * @param gl2
	 */
	public void render(GL2 gl2,MakelangeloRobot robot);
}
