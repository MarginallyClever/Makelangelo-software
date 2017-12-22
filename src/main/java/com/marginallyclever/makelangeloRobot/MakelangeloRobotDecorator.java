package com.marginallyclever.makelangeloRobot;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * DrawDecorator adds features to DrawPanel. An example is the Draw panel shows
 * WYSIWYG progress, while the filters might show conversion while-you-wait
 * updates.
 *
 * @author danroyer
 * @since 7.1.4
 */
public abstract interface MakelangeloRobotDecorator {
	void render(GL2 gl2, MakelangeloRobotSettings machine);
}
