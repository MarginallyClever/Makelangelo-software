package com.marginallyclever.convenience;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings and variables needed to convert a bitmap file into lines
 * @author danroyer
 *
 */
public class BitmapLayer {
	// bitmap file
	public String filename;
	
	// conversion options
	
	public List<Layer2D> layers;
	
	public BitmapLayer() {
		layers = new ArrayList<Layer2D>();
	}
}
