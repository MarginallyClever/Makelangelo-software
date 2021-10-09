package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Base class for all Select.  A Select is a UI panel item the user can control.
 * This system provides consistent look and behavior across all elements in the app.
 * @author Dan Royer
 * @since 7.24.0
 */
public class Select implements FocusListener {
	protected JPanel myPanel= new JPanel();
	private String myName = "";
	
	protected Select(String name) {
		super();
		myName=name;
		myPanel.setLayout(new BorderLayout(2,0));
	}
	
	@Override
	public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {}
	
	public JPanel getPanel() {
		return myPanel;
	}

	public String getMyName() {
		return myName;
	}

	// OBSERVER PATTERN
	
	protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
	
	public void addPropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.add(p);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.remove(p);
	}
	
	public void notifyPropertyChangeListeners(Object oldValue,Object newValue) {
		PropertyChangeEvent evt = new PropertyChangeEvent(this,myName,oldValue,newValue);
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}
}
