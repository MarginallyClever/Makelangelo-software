package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for all Select.  A Select is a UI panel item the user can control.
 * This system provides consistent look and behavior across all elements in the app.
 * @author Dan Royer
 * @since 7.24.0
 */
public abstract class Select extends JPanel {
	protected Select(String name) {
		super(new BorderLayout(2,0));
		setName(name);
	}

	protected void firePropertyChange(Object oldValue,Object newValue) {
		firePropertyChange("value",oldValue,newValue);
	}
}
