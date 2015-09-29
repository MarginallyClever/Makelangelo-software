package com.marginallyclever.drawingtools;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;


public class DrawingTool_Pen extends DrawingTool {
	public DrawingTool_Pen(MainGUI gui, MultilingualSupport ms, MakelangeloRobot mc) {
		super(gui, ms, mc);

		diameter = 1.5f;
		zRate = 120;
		zOn = 90;
		zOff = 50;
		toolNumber = 0;
		feedRate = 3500;
		name = "Pen";
	}

	public DrawingTool_Pen(String name2, int tool_id, MainGUI gui, MultilingualSupport ms, MakelangeloRobot mc) {
		super(gui, ms, mc);

		diameter = 1.5f;
		zRate = 120;
		zOn = 90;
		zOff = 50;
		toolNumber = tool_id;
		feedRate = 3500;
		name = name2;
	}

	public void adjust() {
		final JDialog driver = new JDialog(mainGUI.getParentFrame(), translator.get("penToolAdjust"), true);
		driver.setLayout(new GridBagLayout());

		final JTextField penDiameter = new JTextField(Float.toString(getDiameter()), 5);
		final JTextField penFeedRate = new JTextField(Float.toString(feedRate), 5);

		final JTextField penUp = new JTextField(Float.toString(zOff), 5);
		final JTextField penDown = new JTextField(Float.toString(zOn), 5);
		final JTextField penZRate = new JTextField(Float.toString(zRate), 5);
		final JButton buttonTestUp = new JButton(translator.get("penToolTest"));
		final JButton buttonTestDown = new JButton(translator.get("penToolTest"));
		final JButton buttonSave = new JButton(translator.get("Save"));
		final JButton buttonCancel = new JButton(translator.get("Cancel"));

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		d.anchor = GridBagConstraints.WEST;
		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 50;
		int y = 0;

		c.gridx = 0;
		c.gridy = y;
		driver.add(new JLabel(translator.get("penToolDiameter")), c);
		d.gridx = 1;
		d.gridy = y;
		driver.add(penDiameter, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		driver.add(new JLabel(translator.get("penToolMaxFeedRate")), c);
		d.gridx = 1;
		d.gridy = y;
		driver.add(penFeedRate, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		driver.add(new JLabel(translator.get("penToolUp")), c);
		d.gridx = 1;
		d.gridy = y;
		driver.add(penUp, d);
		d.gridx = 2;
		d.gridy = y;
		driver.add(buttonTestUp, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		driver.add(new JLabel(translator.get("penToolDown")), c);
		d.gridx = 1;
		d.gridy = y;
		driver.add(penDown, d);
		d.gridx = 2;
		d.gridy = y;
		driver.add(buttonTestDown, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		driver.add(new JLabel(translator.get("penToolLiftSpeed")), c);
		d.gridx = 1;
		d.gridy = y;
		driver.add(penZRate, d);
		++y;

		c.gridx = 1;
		c.gridy = y;
		driver.add(buttonSave, c);
		c.gridx = 2;
		c.gridy = y;
		driver.add(buttonCancel, c);
		++y;

		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;
		/*
    c.gridheight=4;
    c.gridx=0;  c.gridy=y;
    driver.add(new JTextArea("Adjust the values sent to the servo to\n" +
                 "raise and lower the pen."),c);
		 */
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();

				if (subject == buttonTestUp) {
					mainGUI.sendLineToRobot("G00 Z" + penUp.getText());
				}
				if (subject == buttonTestDown) {
					mainGUI.sendLineToRobot("G00 Z" + penDown.getText());
				}
				if (subject == buttonSave) {
					setDiameter(Float.valueOf(penDiameter.getText()));
					feedRate = Float.valueOf(penFeedRate.getText());
					zRate = Float.valueOf(penZRate.getText());
					zOff = Float.valueOf(penUp.getText());
					zOn = Float.valueOf(penDown.getText());
					machine.saveConfig();
					driver.dispose();
				}
				if (subject == buttonCancel) {
					driver.dispose();
				}
			}
		};

		buttonTestUp.addActionListener(driveButtons);
		buttonTestDown.addActionListener(driveButtons);

		buttonSave.addActionListener(driveButtons);
		buttonCancel.addActionListener(driveButtons);
		driver.getRootPane().setDefaultButton(buttonSave);

		mainGUI.sendLineToRobot("M114");
		driver.pack();
		driver.setVisible(true);
	}

}
