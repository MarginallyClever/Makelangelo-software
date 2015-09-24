package com.marginallyclever.basictypes;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;

/**
 * Converts a BufferedImage to gcode
 * @author danroyer
 *
 */
public abstract class ImageConverter extends ImageManipulator {
	public ImageConverter(MainGUI gui, MakelangeloRobot mc,
			MultilingualSupport ms) {
		super(gui, mc, ms);
		// TODO Auto-generated constructor stub
	}

	/**
	   * Called by filters that create GCODE from nothing.  Fractals might be one example.
	   * @return true if conversion succeeded.
	   */
	public boolean convert(BufferedImage img) throws IOException {
		  return false;
	}
}
