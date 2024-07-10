package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A color selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectColor extends Select {
	private final BackgroundPaintedButton chooseButton;
	
	/**
	 * @param parentComponent a component (JFrame, JPanel) that owns the color selection dialog
	 * @param labelValue
	 * @param defaultValue
	 */
	public SelectColor(String internalName, String labelValue, Color defaultValue, final Component parentComponent) {
		super(internalName);

		JLabel label = new JLabel(labelValue,JLabel.LEADING);
		label.setName(internalName+".label");

		chooseButton = new BackgroundPaintedButton("");
		chooseButton.setName(internalName+".button");
		chooseButton.setOpaque(true);
		chooseButton.setMinimumSize(new Dimension(80,20));
		chooseButton.setMaximumSize(chooseButton.getMinimumSize());
		chooseButton.setPreferredSize(chooseButton.getMinimumSize());
		chooseButton.setSize(chooseButton.getMinimumSize());
		chooseButton.setBackground(defaultValue);
		chooseButton.setBorder(new LineBorder(Color.BLACK));
		chooseButton.addActionListener(e -> {
			Color c = JColorChooser.showDialog(parentComponent, label.getText(), chooseButton.getBackground());
			if ( c != null ){
				chooseButton.setBackground(c);
				firePropertyChange("color",null,c);
				fireSelectEvent(null,c);
			}
		});

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(chooseButton,BorderLayout.LINE_END);
		
		this.add(label,BorderLayout.LINE_START);
		this.add(panel2,BorderLayout.CENTER);
		this.add(chooseButton,BorderLayout.LINE_END);
	}
	
	public Color getColor() {
		return chooseButton.getBackground();
	}
	
	public void setColor(Color c) {
		chooseButton.setBackground(c);
	}
}
