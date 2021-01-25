package com.marginallyclever.artPipeline.converters;

import com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveImage;
import com.marginallyclever.makelangelo.select.SelectPanel;

/**
 * All converters have a panel with options.  This is their shared root.
 * @author Dan Royer
 */
public class ImageConverterPanel extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static LoadAndSaveImage loadAndSaveImage;
	
	protected ImageConverterPanel() {
		super();
	}
}
