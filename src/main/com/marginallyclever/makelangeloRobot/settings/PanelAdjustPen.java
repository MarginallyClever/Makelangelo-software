package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.convenience.GridBagConstraintsLabel;
import com.marginallyclever.convenience.GridBagConstraintsValue;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;


public class PanelAdjustPen extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8304907375185637987L;

	protected MakelangeloRobot robot;
	
	protected SelectFloat penDiameter;
	protected SelectFloat maxFeedRate;
	protected SelectFloat currentFeedRate;
	protected SelectFloat penUp;
	protected SelectFloat penDown;
	protected SelectFloat penZRate;

	protected JButton buttonTestUp;
	protected JButton buttonTestDown;
	protected JButton buttonSave;
	protected JButton buttonCancel;
	
	protected SelectColor selectPenDownColor;
	protected SelectColor selectPenUpColor;

	
	public PanelAdjustPen(MakelangeloRobot robot) {
		this.robot = robot;

		this.setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

	    JPanel p = new JPanel(new GridBagLayout());
	    this.add(p);
	    
	    MakelangeloRobotSettings settings = robot.getSettings();
		penDiameter = new SelectFloat(settings.getPenDiameter());
		maxFeedRate = new SelectFloat(settings.getPenUpFeedRate());
		currentFeedRate = new SelectFloat(settings.getPenDownFeedRate());
		penUp = new SelectFloat(settings.getPenUpAngle());
		penDown = new SelectFloat(settings.getPenDownAngle());
		penZRate = new SelectFloat(settings.getZRate());
		buttonTestUp = new JButton(Translator.get("penToolTest"));
		buttonTestDown = new JButton(Translator.get("penToolTest"));

	    Dimension s = buttonTestUp.getPreferredSize();
	    s.width = 80;
	    buttonTestUp.setPreferredSize(s);
	    buttonTestDown.setPreferredSize(s);

		GridBagConstraints labelStyle = new GridBagConstraintsLabel();
		GridBagConstraints valueStyle = new GridBagConstraintsValue();

		int y = 0;

		labelStyle.gridy = y;
		p.add(new JLabel(Translator.get("penToolDiameter")), labelStyle);
		valueStyle.gridx = 1;
		valueStyle.gridy = y;
		p.add(penDiameter, valueStyle);
		++y;

		labelStyle.gridy = y;
		p.add(new JLabel(Translator.get("penToolMaxFeedRate")), labelStyle);
		valueStyle.gridx = 1;
		valueStyle.gridy = y;
		p.add(maxFeedRate, valueStyle);
		valueStyle.gridx = 2;
		valueStyle.gridy = y;
		p.add(new JLabel(Translator.get("Rate")), valueStyle);
		++y;

		labelStyle.gridx = 0;
		labelStyle.gridy = y;
		p.add(new JLabel(Translator.get("Speed")), labelStyle);
		valueStyle.gridx = 1;
		valueStyle.gridy = y;
		p.add(currentFeedRate, valueStyle);
		valueStyle.gridx = 2;
		valueStyle.gridy = y;
		p.add(new JLabel(Translator.get("Rate")), valueStyle);
		++y;

		labelStyle.gridx = 0;
		labelStyle.gridy = y;
		p.add(new JLabel(Translator.get("penToolUp")), labelStyle);
		valueStyle.gridx = 1;
		valueStyle.gridy = y;
		p.add(penUp, valueStyle);
		valueStyle.gridx = 2;
		valueStyle.gridy = y;
		p.add(buttonTestUp, valueStyle);
		++y;

		labelStyle.gridx = 0;
		labelStyle.gridy = y;
		p.add(new JLabel(Translator.get("penToolDown")), labelStyle);
		valueStyle.gridx = 1;
		valueStyle.gridy = y;
		p.add(penDown, valueStyle);
		valueStyle.gridx = 2;
		valueStyle.gridy = y;
		p.add(buttonTestDown, valueStyle);
		++y;

		labelStyle.gridx = 0;
		labelStyle.gridy = y;
		p.add(new JLabel(Translator.get("penToolLiftSpeed")), labelStyle);
		valueStyle.gridx = 1;
		valueStyle.gridy = y;
		p.add(penZRate, valueStyle);
		valueStyle.gridx = 2;
		valueStyle.gridy = y;
		p.add(new JLabel(Translator.get("RateDeg")), valueStyle);
		++y;

		//labelStyle.gridwidth = 2;
		//labelStyle.insets = new Insets(0, 5, 5, 5);
		//labelStyle.anchor = GridBagConstraints.WEST;
		//labelStyle.gridy=y;
		//++y;
		
		buttonTestUp.addActionListener(this);
		buttonTestDown.addActionListener(this);
		
		GridBagConstraints cm = new GridBagConstraints();
		selectPenDownColor = new SelectColor(this,"pen down color",robot.getSettings().getPenDownColor());
		this.add(selectPenDownColor,cm);
		labelStyle.gridy=y;
		++y;
		selectPenUpColor = new SelectColor(this,"pen up color",robot.getSettings().getPenUpColor());
		this.add(selectPenUpColor,cm);
		labelStyle.gridy=y;
		++y;
	}
	
	
	public void actionPerformed(ActionEvent event) {
		Object subject = event.getSource();

		if (subject == buttonTestUp  ) {
			// must match MakelangeloRobotSettings.getPenUpString()
			robot.sendLineToRobot(
				"G00 F" + ((Number)penZRate.getValue()).floatValue() + " Z" + ((Number)penUp.getValue()).floatValue() + ";\n"+
				"G00 F" + ((Number)maxFeedRate.getValue()).floatValue() + ";\n"
				);
		}
		if (subject == buttonTestDown) {
			// must match MakelangeloRobotSettings.getPenDownString()
			robot.sendLineToRobot(
					"G01 F" + ((Number)penZRate.getValue()).floatValue() + " Z" + ((Number)penDown.getValue()).floatValue() + ";\n"+
					"G01 F" + ((Number)currentFeedRate.getValue()).floatValue() + ";\n"
					);
		}
	}
	
	
	public void save() {
	    MakelangeloRobotSettings settings = robot.getSettings();
		settings.setDiameter(((Number)penDiameter.getValue()).floatValue());
		settings.setMaxFeedRate(((Number)maxFeedRate.getValue()).floatValue());
		settings.setCurrentFeedRate(((Number)currentFeedRate.getValue()).floatValue());
		settings.setZRate(((Number)penZRate.getValue()).floatValue());
		settings.setPenUpAngle(((Number)penUp.getValue()).floatValue());
		settings.setPenDownAngle(((Number)penDown.getValue()).floatValue());
		settings.setPenDownColor(selectPenDownColor.getColor());
		settings.setPenDownColorDefault(selectPenDownColor.getColor());
		settings.setPenUpColor(selectPenUpColor.getColor());
	}
}
