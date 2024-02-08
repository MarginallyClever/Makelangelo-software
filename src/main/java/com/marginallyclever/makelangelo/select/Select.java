package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;

/**
 * Base class for all Select.  A Select is a UI panel item the user can control.
 * This system provides consistent look and behavior across all elements in the app.
 * @author Dan Royer
 * @since 7.24.0
 */
public abstract class Select extends JPanel {
	private final EventListenerList listeners = new EventListenerList();

	protected Select(String name) {
		super(new BorderLayout(2,0));
		setName(name);
	}

	public void addSelectListener(SelectListener listener) {
		listeners.add(SelectListener.class,listener);
	}

	public void removeSelectListener(SelectListener listener) {
		listeners.remove(SelectListener.class,listener);
	}

	protected void fireSelectEvent(Object oldValue,Object newValue) {
		SelectEvent evt = new SelectEvent(this,oldValue,newValue);
		for(SelectListener listener : listeners.getListeners(SelectListener.class)) {
			listener.selectEvent(evt);
		}
	}
}
