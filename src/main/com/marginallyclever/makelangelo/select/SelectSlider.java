package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A slider to restrict integer values to the range you want. 
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectSlider extends Select {
	private JLabel label;
	private JSlider field;
	private JLabel value;
	private Timer timer=null;
	
	public SelectSlider(String labelText,int top,int bottom,int defaultValue) {
		super();

		label = new JLabel(labelText,JLabel.LEADING);
		value = new JLabel("0",JLabel.TRAILING);
		
		field = new JSlider();
		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		final Select parent = this;
		field.addChangeListener(new ChangeListener() {
        	@Override
			public void stateChanged(ChangeEvent e) {
		        int n = field.getValue();

				if(timer!=null) timer.cancel();
				timer = new Timer("Delayed response");
				timer.schedule(new TimerTask() { 
					public void run() {
						notifyPropertyChangeListeners(new PropertyChangeEvent(parent,"value",null,n));
					}
				}, 100L); // brief delay in case someone is typing fast
				
		        value.setText(Integer.toString(n));
			}
		});
		field.setValue(defaultValue);

		Dimension dim = new Dimension(30,1);
		value.setMinimumSize(dim);
		value.setPreferredSize(dim);
		value.setMaximumSize(dim);
		
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.CENTER);
		panel.add(value,BorderLayout.LINE_END);
	}
	
	public int getValue() {
		return field.getValue();
	}
	
	public void setValue(int v) {
		field.setValue(v);
	}
}
