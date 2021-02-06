package com.marginallyclever.artPipeline;

import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;

/**
 * All generators have a panel with options.  This is their shared root.
 * @author Dan Royer
 *
 */
public class NodePanel extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static public MakelangeloRobotPanel makelangeloRobotPanel; 
	
	protected NodePanel() {
		super();
	}
}
