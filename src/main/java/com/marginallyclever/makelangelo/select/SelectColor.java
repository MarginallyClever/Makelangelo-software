package com.marginallyclever.makelangelo.select;

import com.marginallyclever.convenience.ColorRGB;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.Serial;

/**
 * A color selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectColor extends Select {
	@Serial
	private static final long serialVersionUID = 8898817786685388020L;
	
	private final JLabel label;
	private final BackgroundPaintedButton chooseButton;
	
	/**
	 * @param parentComponent a component (JFrame, JPanel) that owns the color selection dialog
	 * @param labelValue
	 * @param defaultValue
	 */
	public SelectColor(String internalName,String labelValue,ColorRGB defaultValue,final Component parentComponent) {
		super(internalName);
		
		label = new JLabel(labelValue,JLabel.LEADING);

		chooseButton = new BackgroundPaintedButton("");
		chooseButton.setOpaque(true);
		chooseButton.setMinimumSize(new Dimension(80,20));
		chooseButton.setMaximumSize(chooseButton.getMinimumSize());
		chooseButton.setPreferredSize(chooseButton.getMinimumSize());
		chooseButton.setSize(chooseButton.getMinimumSize());
		chooseButton.setBackground(new Color(defaultValue.toInt()));
		chooseButton.setBorder(new LineBorder(Color.BLACK));

		chooseButton.addActionListener(e -> {
			Color c = JColorChooser.showDialog(parentComponent, label.getText(), chooseButton.getBackground());
			if ( c != null ){
				chooseButton.setBackground(c);
				firePropertyChange(null,c);
			}
		});

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(chooseButton,BorderLayout.LINE_END);
		
		this.add(label,BorderLayout.LINE_START);
		this.add(panel2,BorderLayout.CENTER);
		this.add(chooseButton,BorderLayout.LINE_END);
	}
	
	public ColorRGB getColor() {
		Color c = chooseButton.getBackground();
		return new ColorRGB(c.getRed(),c.getGreen(),c.getBlue());
	}
	
	public void setColor(ColorRGB c) {
		chooseButton.setBackground(new Color(c.red,c.green,c.blue));
	}
}
