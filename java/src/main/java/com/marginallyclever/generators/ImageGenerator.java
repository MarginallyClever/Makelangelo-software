package com.marginallyclever.generators;

import java.io.IOException;
import java.io.Writer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettings;

/**
 * Generates gcode from user input.  Fractals might be one example.
 * @author danroyer
 *
 */
public abstract class ImageGenerator extends ImageManipulator implements DrawPanelDecorator {
	/**
	 * @return true if generate succeeded.
	 * @param dest the file where the results will be saved.
	 */
	public boolean generate(Writer out) throws IOException {
		return false;
	}


	public void render(GL2 gl2, MakelangeloRobotSettings settings) {}
}
