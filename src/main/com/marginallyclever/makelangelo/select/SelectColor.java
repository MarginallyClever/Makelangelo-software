package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.marginallyclever.convenience.ColorRGB;

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
	 * @param parent a component (JFrame, JPanel) that owns the color selection dialog
	 * @param labelValue
	 * @param defaultValue
	 */
	public SelectColor(Component parent,String labelValue,ColorRGB defaultValue) {
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
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(parent, label.getText(), field.getBackground());
				field.setBackground(c);
				setChanged();
				notifyObservers();
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
		setChanged();
		notifyObservers();
	}
}
