package com.marginallyclever.core.select;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * Base class for all {@link Select}.  A {@link Select} is a UI panel item with whom the user can interact.
 * This system provides consistent look and behavior across all elements in the app.  A {@link SelectPanel}
 * filled with {@link Select} should look and behave consistently.
 * @author Dan Royer
 * @since 7.24.0
 */
public abstract class Select implements FocusListener {
	private JPanel panel = new JPanel(new BorderLayout(2,0));
	private ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
	
	protected Select() {}
	
	@Override
	public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.add(p);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.remove(p);
	}
	
	public void notifyPropertyChangeListeners(PropertyChangeEvent evt) {
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}
}
