package com.marginallyclever.convenience.nodes;

import com.marginallyclever.makelangelo.robot.MakelangeloRobotPanel;
import com.marginallyclever.makelangelo.select.SelectPanel;

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
