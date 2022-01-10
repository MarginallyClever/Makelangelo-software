package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ChangeToolPanel extends JPanel {
	private static final long serialVersionUID = 2253826545484233479L;

	public ChangeToolPanel(int toolNumber) {
		super();

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);

		JLabel fieldValue = new JLabel("");
		fieldValue.setOpaque(true);
		fieldValue.setMinimumSize(new Dimension(80, 20));
		fieldValue.setMaximumSize(fieldValue.getMinimumSize());
		fieldValue.setPreferredSize(fieldValue.getMinimumSize());
		fieldValue.setSize(fieldValue.getMinimumSize());
		fieldValue.setBackground(new Color(toolNumber));
		fieldValue.setBorder(new LineBorder(Color.BLACK));
		this.add(fieldValue, c);

		JLabel message = new JLabel(Translator.get("ChangeToolMessage"));
		c.gridx = 1;
		c.gridwidth = 3;
		this.add(message, c);
	}
	
	public void run(JFrame mainFrame) {
		JOptionPane.showMessageDialog(mainFrame, this, Translator.get("ChangeToolTitle"), JOptionPane.PLAIN_MESSAGE);	
	}
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(ChangeToolPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ChangeToolPanel(0));
		frame.pack();
		frame.setVisible(true);
	}
}
