package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.marginallyClever.convenience.ColorRGB;

/**
 * A color selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectColor extends Select {
	private static final long serialVersionUID = 8898817786685388020L;
	
	private JLabel label;
	private BackgroundPaintedButton chooseButton;
	
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

		chooseButton.addActionListener(new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(parentComponent, label.getText(), chooseButton.getBackground());
				if ( c != null ){
					chooseButton.setBackground(c);
					firePropertyChange(null,c);
				}
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
