package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all Select.  A Select is a UI panel item the user can control.
 * This system provides consistent look and behavior across all elements in the app.
 * @author Dan Royer
 * @since 7.24.0
 */
public abstract class Select extends JPanel {
	private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();
		
	protected Select(String name) {
		super(new BorderLayout(2,0));
		setName(name);
	}
	
	// OBSERVER PATTERN
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener p) {	
		propertyChangeListeners.add(p);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.remove(p);
	}
	
	protected void firePropertyChange(Object oldValue,Object newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent(this,this.getName(),oldValue,newValue);
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}
}
