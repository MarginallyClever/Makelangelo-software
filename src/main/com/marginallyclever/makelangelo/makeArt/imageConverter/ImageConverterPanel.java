package com.marginallyclever.makelangelo.makeArt.imageConverter;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectPanelChangeListener;

/**
 * All converters have a panel with options.  This is their shared root.
 * @author Dan Royer
 */
public abstract class ImageConverterPanel extends SelectPanel {
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
