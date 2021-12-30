package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = -8598766351908283067L;
	private JLabel label;
	private JComboBox<String> field = new JComboBox<String>();
	private DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)field.getModel();
	
	public SelectOneOfMany(String internalName,String labelKey) {
		super(internalName);
		
		label = new JLabel(labelKey,JLabel.LEADING);

		field.addActionListener((e)-> firePropertyChange(null, field.getSelectedIndex()) );

		this.removeAll();
		this.setBorder(new EmptyBorder(0,0,0,1));
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	public SelectOneOfMany(String internalName,String labelKey,String[] options,int defaultValue) {
		this(internalName,labelKey);
		
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
		field.repaint();// Some times it need it ! but why ? normaly the swing events listener take care of that ...
	}

	public void setNewList(String[] list) {
		model.removeAllElements();
		model.addAll(Arrays.asList(list));
	}
}
