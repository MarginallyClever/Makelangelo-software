package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectOneOfMany extends Select {
	private JLabel label;
	private JComboBox<String> field;

	public SelectOneOfMany(String labelKey,String[] options,int defaultValue) {
		super();
		
		label = new JLabel(labelKey,JLabel.LEADING);
		field = new JComboBox<String>(options); 
		field.setSelectedIndex(defaultValue);

		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setChanged();
				notifyObservers();
			}
		});

		panel.setBorder(new EmptyBorder(0,0,0,1));
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}
	
	public void setReadOnly() {
		field.setEditable(false);
	}
	
	public String getSelectedItem() {
		return (String)field.getSelectedItem();
	}
	
	public int getSelectedIndex() {
		return field.getSelectedIndex();
	}

	public void setSelectedIndex(int index) {
		field.setSelectedIndex(index);
	}
}
