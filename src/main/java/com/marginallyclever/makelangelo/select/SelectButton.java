package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;



/**
 * A button that does nothing until you attach an observer.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectButton extends Select {
	private final List<ActionListener> actionListenerList = new ArrayList<>();
	private final JButton button;

	public SelectButton(String internalName,AbstractAction action) {
		super(internalName);
		button = new JButton(action);
	}
	
	public SelectButton(String internalName,String labelText) {
		super(internalName);
		
		button = new JButton(labelText);
		button.addActionListener((e) -> {
			fireActionEvent();
		});

		this.add(button,BorderLayout.CENTER);
	}
	
	public void doClick() {
		if(button!=null) button.doClick();
	}
	
	public void setText(String label) {
		if(button!=null) button.setText(label);
	}
	
	public void setEnabled(boolean b) {
		if(button!=null) button.setEnabled(b);
	}

	public void setForeground(Color fg) {
		if(button!=null) button.setForeground(fg);
	}

	public void addActionListener(ActionListener l) {
		actionListenerList.add(l);
	}

	public void removeActionListener(ActionListener l) {
		actionListenerList.remove(l);
	}

	public void fireActionEvent() {
		for(ActionListener l : actionListenerList) {
			l.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,getName()));
		}
	}
}
