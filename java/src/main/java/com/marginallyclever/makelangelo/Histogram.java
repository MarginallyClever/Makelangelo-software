package com.marginallyclever.makelangelo;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Histogram of an image with 8 bits red, 8 bits green, and 8 bits blue.
 * @author danroyer
 * @since 7.1.4-SNAPSHOT?
 */
public class Histogram {
	public char [] red = new char[256];
	public char [] green = new char[256];
	public char [] blue = new char[256];

	public Histogram() {}
	
	public void getHistogramOf(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		int x, y;
		
		for(y=0;y<h;++y) {
			for(x=0;x<w;++x) {
				Color c = new Color(img.getRGB(x,y));
				red[c.getRed()]++;
				green[c.getGreen()]++;
				blue[c.getBlue()]++;
			}
		}
	}
}
