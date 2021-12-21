package com.marginallyclever.makelangelo.select;

import java.beans.PropertyChangeEvent;

public abstract interface SelectPanelChangeListener {
	public abstract void selectPanelPropertyChange(PropertyChangeEvent evt);
}
