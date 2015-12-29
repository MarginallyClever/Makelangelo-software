package com.marginallyclever.filters;

import java.awt.image.BufferedImage;

import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;

/**
 * Modifies a BufferedImage
 * @author danroyer
 *
 */
public abstract class ImageFilter extends ImageManipulator {
	  public ImageFilter(Makelangelo gui, MakelangeloRobotSettings mc,
			Translator ms) {
		super(gui, mc, ms);
	  }

	  /**
	   * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	   * @return the altered image
	   */
	  public BufferedImage filter(BufferedImage img) {
	    return img;
	  }
}
