package com.marginallyclever.basictypes;

import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;

/**
 * Generates gcode from user input.  Fractals might be one example.
 * @author danroyer
 *
 */
public abstract class ImageGenerator extends ImageManipulator {
	  public ImageGenerator(MainGUI gui, MakelangeloRobot mc,
			MultilingualSupport ms) {
		super(gui, mc, ms);
		// TODO Auto-generated constructor stub
	  }

	/**
	  * @return true if generate succeeded.
	  */
	  public boolean generate() {
		  return false;
	  }
}
