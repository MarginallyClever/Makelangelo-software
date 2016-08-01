package com.marginallyclever.generators;

import java.io.IOException;
import java.io.Writer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettings;

/**
 * Generators create gcode from user input.  Fractals might be one example.
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author dan royer
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
	
	/**
	 * live preview as the system is generating.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2, MakelangeloRobotSettings settings) {}
}
