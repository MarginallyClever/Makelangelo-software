package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.select.SelectPanel;

/**
 * All converters have a panel with options.  This is their shared root.
 * @author Dan Royer
 */
public class ImageConverterPanel extends SelectPanel {
	private static final long serialVersionUID = 1L;
	private final ImageConverter myConverter;
	
	public ImageConverterPanel(ImageConverter converter) {
		super();
		myConverter=converter;

		myConverter.getPanelElements().forEach(this::add);
	}
	
	public ImageConverter getConverter() {
		return myConverter;
	}
}
