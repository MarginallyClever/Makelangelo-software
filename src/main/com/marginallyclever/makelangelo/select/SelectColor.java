package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;

public class SelectColor extends Select {
	private JPanel panel;
	private JLabel label;
	private JLabel field;
	private JButton chooseButton;
	
	public SelectColor(JComponent parent,String labelValue,ColorRGB defaultValue) {
		super();
		
		label = new JLabel(labelValue,SwingConstants.LEFT);

		field = new JLabel("");
		field.setOpaque(true);
		field.setMinimumSize(new Dimension(80,20));
		field.setMaximumSize(field.getMinimumSize());
		field.setPreferredSize(field.getMinimumSize());
		field.setSize(field.getMinimumSize());
		field.setBackground(new Color(defaultValue.toInt()));

		field.setBorder(new LineBorder(Color.BLACK));

		chooseButton = new JButton(Translator.get("Choose"));
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(parent, label.getText(), field.getBackground());
				field.setBackground(c);
			}
		});
		
		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.CENTER);
		panel.add(chooseButton,BorderLayout.LINE_END);
	}
	
	public ColorRGB getColor() {
		Color c = field.getBackground();
		return new ColorRGB(c.getRed(),c.getGreen(),c.getBlue());
	}
}
