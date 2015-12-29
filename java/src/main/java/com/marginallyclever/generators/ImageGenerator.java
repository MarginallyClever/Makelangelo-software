package com.marginallyclever.generators;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;

/**
 * Generates gcode from user input.  Fractals might be one example.
 * @author danroyer
 *
 */
public abstract class ImageGenerator extends ImageManipulator implements DrawPanelDecorator {
	public ImageGenerator(Makelangelo gui, MakelangeloRobotSettings mc,
			Translator ms) {
		super(gui, mc, ms);
	}

	/**
	 * @return true if generate succeeded.
	 * @param dest the file where the results will be saved.
	 */
	public boolean generate(final String dest) {
		return false;
	}
	
	public void render(GL2 gl2, MakelangeloRobotSettings settings) {}
}
