package com.marginallyclever.filters;

import java.awt.image.BufferedImage;

import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;

/**
 * Modifies a BufferedImage
 * @author danroyer
 *
 */
public abstract class ImageFilter extends ImageManipulator {
	public ImageFilter(Makelangelo gui, MakelangeloRobotSettings mc) {
		super(gui, mc);
	}

	/**
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @return the altered image
	 */
	public BufferedImage filter(BufferedImage img) {
		return img;
	}
}
