package com.marginallyclever.drawingtools;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;


public class DrawingTool_Pen extends DrawingTool implements ActionListener {
	protected JDialog dialog;
	protected JPanel panel;
	
	protected JFormattedTextField penDiameter;
	protected JFormattedTextField penFeedRate;
	protected JFormattedTextField penUp;
	protected JFormattedTextField penDown;
	protected JFormattedTextField penZRate;

	protected JButton buttonTestUp;
	protected JButton buttonTestDown;
	protected JButton buttonSave;
	protected JButton buttonCancel;

	
	public DrawingTool_Pen(Translator ms, MakelangeloRobot robot) {
		super(ms, robot);

		diameter = 1.5f;
		zRate = 50;
		zOn = 90;
		zOff = 50;
		toolNumber = 0;
		feedRate = 3500;
		name = "Pen";
	}

	public DrawingTool_Pen(String name2, int tool_id, Translator ms, MakelangeloRobot robot) {
		super(ms, robot);

		diameter = 1.5f;
		zRate = 120;
		zOn = 90;
		zOff = 50;
		toolNumber = tool_id;
		feedRate = 3500;
		name = name2;
	}

	public JPanel getPanel() {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	    panel.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

	    JPanel p = new JPanel(new GridBagLayout());
	    panel.add(p);
	    
	    NumberFormat nFloat = NumberFormat.getNumberInstance();
	    nFloat.setMaximumFractionDigits(1);
	    
		penDiameter = new JFormattedTextField(nFloat);
		penFeedRate = new JFormattedTextField(nFloat);
		penUp = new JFormattedTextField(nFloat);
		penDown = new JFormattedTextField(nFloat);
		penZRate = new JFormattedTextField(nFloat);
		buttonTestUp = new JButton(Translator.get("penToolTest"));
		buttonTestDown = new JButton(Translator.get("penToolTest"));

		penDiameter.setValue(getDiameter());
		penFeedRate.setValue(feedRate);
		penUp.setValue(zOff);
		penDown.setValue(zOn);
		penZRate.setValue(zRate);

	    Dimension s = buttonTestUp.getPreferredSize();
	    s.width = 80;
	    buttonTestUp.setPreferredSize(s);
	    buttonTestDown.setPreferredSize(s);

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		c.ipadx=5;
	    c.ipady=0;

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		d.anchor = GridBagConstraints.WEST;
		d.fill = GridBagConstraints.HORIZONTAL;
		d.weightx = 50;
		int y = 0;

		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("penToolDiameter")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(penDiameter, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("penToolMaxFeedRate")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(penFeedRate, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("penToolUp")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(penUp, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(buttonTestUp, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("penToolDown")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(penDown, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(buttonTestDown, d);
		++y;

		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("penToolLiftSpeed")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(penZRate, d);
		++y;

		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;
		
		buttonTestUp.addActionListener(this);
		buttonTestDown.addActionListener(this);
		
		return panel;
	}
	
	
	public void actionPerformed(ActionEvent event) {
		Object subject = event.getSource();

		if (subject == buttonTestUp) {
			if(robot.isPortConfirmed()) {
				try {
				robot.getConnection().sendMessage("G00 Z" + penUp.getText());
				} catch(Exception e) {}
			}
		}
		if (subject == buttonTestDown) {
			if(robot.isPortConfirmed()) {
				try {
					robot.getConnection().sendMessage("G00 Z" + penDown.getText());
				} catch(Exception e) {}
			}
		}
	}
	
	public void save() {
		setDiameter(((Number)penDiameter.getValue()).floatValue());
		feedRate = ((Number)penFeedRate.getValue()).floatValue();
		zRate = ((Number)penZRate.getValue()).floatValue();
		zOff = ((Number)penUp.getValue()).floatValue();
		zOn = ((Number)penDown.getValue()).floatValue();
		robot.settings.saveConfig();
	}

}
