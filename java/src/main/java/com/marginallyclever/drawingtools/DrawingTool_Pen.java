package com.marginallyclever.drawingtools;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;


public class DrawingTool_Pen extends DrawingTool implements ActionListener {
	protected JDialog dialog;
	protected JPanel panel;
	protected JTextField penDiameter;
	protected JTextField penFeedRate;

	protected JTextField penUp;
	protected JTextField penDown;
	protected JTextField penZRate;
	protected JButton buttonTestUp;
	protected JButton buttonTestDown;
	protected JButton buttonSave;
	protected JButton buttonCancel;

	
	public DrawingTool_Pen(Makelangelo gui, Translator ms, MakelangeloRobotSettings mc) {
		super(gui, ms, mc);

		diameter = 1.5f;
		zRate = 50;
		zOn = 90;
		zOff = 50;
		toolNumber = 0;
		feedRate = 3500;
		name = "Pen";
	}

	public DrawingTool_Pen(String name2, int tool_id, Makelangelo gui, Translator ms, MakelangeloRobotSettings mc) {
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
		dialog = new JDialog(mainGUI.getParentFrame(), translator.get("penToolAdjust"), true);
		panel = getPanel();
		dialog.add(panel);

		dialog.pack();
		dialog.setVisible(true);
	}
	
	public JPanel getPanel() {
		panel = new JPanel(new GridBagLayout());

		penDiameter = new JTextField(Float.toString(getDiameter()), 5);
		penFeedRate = new JTextField(Float.toString(feedRate), 5);
		penUp = new JTextField(Float.toString(zOff), 5);
		penDown = new JTextField(Float.toString(zOn), 5);
		penZRate = new JTextField(Float.toString(zRate), 5);
		buttonTestUp = new JButton(translator.get("penToolTest"));
		buttonTestDown = new JButton(translator.get("penToolTest"));

	    Dimension s = buttonTestUp.getPreferredSize();
	    s.width = 80;
	    buttonTestUp.setPreferredSize(s);
	    buttonTestDown.setPreferredSize(s);

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		c.ipadx=2;
		d.ipadx=2;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		d.anchor = GridBagConstraints.WEST;
		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 50;
		int y = 0;

		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(translator.get("penToolDiameter")), c);
		d.gridx = 1;
		d.gridy = y;
		panel.add(penDiameter, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(translator.get("penToolMaxFeedRate")), c);
		d.gridx = 1;
		d.gridy = y;
		panel.add(penFeedRate, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(translator.get("penToolUp")), c);
		d.gridx = 1;
		d.gridy = y;
		panel.add(penUp, d);
		d.gridx = 2;
		d.gridy = y;
		panel.add(buttonTestUp, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(translator.get("penToolDown")), c);
		d.gridx = 1;
		d.gridy = y;
		panel.add(penDown, d);
		d.gridx = 2;
		d.gridy = y;
		panel.add(buttonTestDown, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(translator.get("penToolLiftSpeed")), c);
		d.gridx = 1;
		d.gridy = y;
		panel.add(penZRate, d);
		++y;

		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;
		
		buttonTestUp.addActionListener(this);
		buttonTestDown.addActionListener(this);
		
		return panel;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if (subject == buttonTestUp) {
			mainGUI.sendLineToRobot("G00 Z" + penUp.getText());
		}
		if (subject == buttonTestDown) {
			mainGUI.sendLineToRobot("G00 Z" + penDown.getText());
		}
	}
	
	public void save() {
		setDiameter(Float.valueOf(penDiameter.getText()));
		feedRate = Float.valueOf(penFeedRate.getText());
		zRate = Float.valueOf(penZRate.getText());
		zOff = Float.valueOf(penUp.getText());
		zOn = Float.valueOf(penDown.getText());
		machine.saveConfig();
	}

}
