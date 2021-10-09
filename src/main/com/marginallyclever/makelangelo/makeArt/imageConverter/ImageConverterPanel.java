package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.select.SelectPanel;

/**
 * All converters have a panel with options.  This is their shared root.
 * @author Dan Royer
 */
public class ImageConverterPanel extends SelectPanel {
	private static final long serialVersionUID = 1L;
	private ImageConverter myConverter;
	
	protected ImageConverterPanel(ImageConverter converter) {
		super();
		myConverter=converter;
		addPropertyChangeListener(myConverter);
	}
	
	public ImageConverter getConverter() {
		return myConverter;
	}
}
