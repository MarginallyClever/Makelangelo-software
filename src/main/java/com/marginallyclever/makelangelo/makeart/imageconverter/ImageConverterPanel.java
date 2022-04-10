package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.select.SelectPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * All converters have a panel with options.  This is their shared root.
 * @author Dan Royer
 */
public abstract class ImageConverterPanel extends SelectPanel {
	private static final long serialVersionUID = 1L;
	private final ImageConverter myConverter;
	
	protected ImageConverterPanel(ImageConverter converter) {
		super();
		myConverter=converter;
	}
	
	public ImageConverter getConverter() {
		return myConverter;
	}

	// Observer pattern

	private final List<ImageConverterPanelListener> listeners = new ArrayList<ImageConverterPanelListener>();
	public void addImageConverterPanelListener(ImageConverterPanelListener l) {
		listeners.add(l);
	}
	public void removeImageConverterPanelListener(ImageConverterPanelListener l) {
		listeners.remove(l);
	}

	protected void fireRestartConversion() {
		for(ImageConverterPanelListener l : listeners) {
			l.reconvert(this);
		}
	}
}
