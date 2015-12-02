package com.marginallyclever.basictypes;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;

/**
 * Generates gcode from user input.  Fractals might be one example.
 * @author danroyer
 *
 */
public abstract class ImageGenerator extends ImageManipulator {
	  public ImageGenerator(Makelangelo gui, MakelangeloRobot mc,
			MultilingualSupport ms) {
		super(gui, mc, ms);
	  }

	/**
	  * @return true if generate succeeded.
	  * @param dest the file where the results will be saved.
	  */
	  public boolean generate(final String dest) {
		  return false;
	  }
}
