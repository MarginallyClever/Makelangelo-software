package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all Select.  A Select is a UI panel item the user can control.
 * This system provides consistent look and behavior across all elements in the app.
 * @author Dan Royer
 * @since 7.24.0
 */
public abstract class Select extends JPanel {
	private final List<SelectListener> listeners = new ArrayList<>();

	protected Select(String name) {
		super(new BorderLayout(2,0));
		setName(name);
	}

	public void addSelectListener(SelectListener listener) {
		listeners.add(listener);
	}

	public void removeSelectListener(SelectListener listener) {
		listeners.remove(listener);
	}

	protected void fireSelectEvent(Object oldValue,Object newValue) {
		SelectEvent evt = new SelectEvent(this,oldValue,newValue);
		for(SelectListener listener : listeners) {
			listener.selectEvent(evt);
		}
	}
}
