package com.marginallyclever.core.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.marginallyclever.core.ColorRGB;

/**
 * A color selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectColor extends Select {
	private JLabel label;
	private JLabel field;
	private JButton chooseButton;
	
	/**
	 * 
	 * @param parentComponent a component (JFrame, JPanel) that owns the color selection dialog
	 * @param labelValue
	 * @param defaultValue
	 */
	public SelectColor(Component parentComponent,String labelValue,ColorRGB defaultValue) {
		super();
		
		label = new JLabel(labelValue,JLabel.LEADING);

		field = new JLabel("");
		field.setOpaque(true);
		field.setMinimumSize(new Dimension(80,20));
		field.setMaximumSize(field.getMinimumSize());
		field.setPreferredSize(field.getMinimumSize());
		field.setSize(field.getMinimumSize());
		field.setBackground(new Color(defaultValue.toInt()));
		field.setBorder(new LineBorder(Color.BLACK));

		chooseButton = new JButton("...");
		final Select parent = this;
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(parentComponent, label.getText(), field.getBackground());
				field.setBackground(c);
				notifyPropertyChangeListeners(new PropertyChangeEvent(parent,"value",null,null));
			}
		});

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(field,BorderLayout.LINE_END);
		
		panel.add(label,BorderLayout.LINE_START);
		panel.add(panel2,BorderLayout.CENTER);
		panel.add(chooseButton,BorderLayout.LINE_END);
	}
	
	public ColorRGB getColor() {
		Color c = field.getBackground();
		return new ColorRGB(c.getRed(),c.getGreen(),c.getBlue());
	}
	
	public void setColor(ColorRGB c) {
		field.setBackground(new Color(c.red,c.green,c.blue));
		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"color",null,null));
	}
}
