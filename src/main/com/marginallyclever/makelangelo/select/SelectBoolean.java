package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.makelangelo.Translator;

/**
 * A JCheckBox that sets itself up to format true/false. 
 * @author Dan Royer
 * @since 7.8.0
 */
public class SelectBoolean extends Select {
	private JLabel label;
	private JCheckBox field;
	
	public SelectBoolean(String labelKey,boolean arg0) {
		super();
		
		label = new JLabel(labelKey,SwingConstants.LEFT);
		
		field = new JCheckBox();
		field.setSelected(arg0);
		
		field.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				hasChanged();
				notifyObservers();
			}
		});

		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}
	
	public boolean isSelected() {
		return field.isSelected();
	}

	public void setSelected(boolean b) {
		// TODO Auto-generated method stub
		
	}
}
