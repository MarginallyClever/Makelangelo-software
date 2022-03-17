package com.marginallyClever.makelangelo.select;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Base class for all Select.  A Select is a UI panel item the user can control.
 * This system provides consistent look and behavior across all elements in the app.
 * @author Dan Royer
 * @since 7.24.0
 */
public class Select extends JPanel {
	private static final long serialVersionUID = 5289951183273734129L;	
	private List<PropertyChangeListener> propertyChangeListeners = null;
		
	protected Select(String name) {
		super(new BorderLayout(2,0));
		setName(name);
	}
	
	// OBSERVER PATTERN
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener p) {	
		if ( propertyChangeListeners == null ){
			// some Look and Feel (like "com.sun.java.swing.plaf.gtk.GTKLookAndFeel") can run this override method before the class is fully initialized ...
			propertyChangeListeners = new ArrayList<>();
		}
		propertyChangeListeners.add(p);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener p) {
		if ( propertyChangeListeners == null ){
			propertyChangeListeners = new ArrayList<>();
		}
		propertyChangeListeners.remove(p);
	}
	
	protected void firePropertyChange(Object oldValue,Object newValue) {
		if ( propertyChangeListeners == null ){
			propertyChangeListeners = new ArrayList<>();
		}
		PropertyChangeEvent evt = new PropertyChangeEvent(this,this.getName(),oldValue,newValue);
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}
}
