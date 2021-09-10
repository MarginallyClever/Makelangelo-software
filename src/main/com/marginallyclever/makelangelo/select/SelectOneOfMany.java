package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectOneOfMany extends Select {
	private JLabel label;
	private JComboBox<String> field = new JComboBox<String>();
	private DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)field.getModel();
	
	public SelectOneOfMany(String labelKey) {
		super();
		
		label = new JLabel(labelKey,JLabel.LEADING);

		final Select parent = this;
		field.addActionListener((e)->{
			notifyPropertyChangeListeners(new PropertyChangeEvent(parent, "value", null, field.getSelectedIndex()));
		});

		panel.removeAll();
		panel.setBorder(new EmptyBorder(0,0,0,1));
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}
	
	public SelectOneOfMany(String labelKey,String[] options,int defaultValue) {
		this(labelKey);
		
		setNewList(options);
		field.setSelectedIndex(defaultValue);
	}
	
	public void removeAll() {
		model.removeAllElements();
	}
	
	public void addItem(String s) {
		model.addElement(s);
	}
	
	public void removeItem(String s) {
		model.removeElement(s);
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

	public void setNewList(String[] list) {
		model.removeAllElements();
		model.addAll(Arrays.asList(list));
	}
}
