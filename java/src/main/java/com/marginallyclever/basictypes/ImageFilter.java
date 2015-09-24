package com.marginallyclever.basictypes;

import java.awt.image.BufferedImage;

import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;

/**
 * Manipulates a BufferedImage
 * @author danroyer
 *
 */
public abstract class ImageFilter extends ImageManipulator {
	  public ImageFilter(MainGUI gui, MakelangeloRobot mc,
			MultilingualSupport ms) {
		super(gui, mc, ms);
		// TODO Auto-generated constructor stub
	  }

	  /**
	   * Process should be called by filters that modify a bufferedimage.  Think photoshop filters.
	   *
	   * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	   * @return the altered image
	   */
	  public BufferedImage filter(BufferedImage img) {
	    return img;
	  }
}
