package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.FloatField;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;


public class PanelAdjustPen extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8304907375185637987L;

	protected MakelangeloRobot robot;
	
	protected FloatField penDiameter;
	protected FloatField penFeedRate;
	protected FloatField penUp;
	protected FloatField penDown;
	protected FloatField penZRate;

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
		penDiameter = new FloatField(settings.getDiameter());
		penFeedRate = new FloatField(settings.getMaxFeedRate());
		penUp = new FloatField(settings.getPenUpAngle());
		penDown = new FloatField(settings.getPenDownAngle());
		penZRate = new FloatField(settings.getZRate());
		buttonTestUp = new JButton(Translator.get("penToolTest"));
		buttonTestDown = new JButton(Translator.get("penToolTest"));

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
		c.gridy++;
		
		buttonTestUp.addActionListener(this);
		buttonTestDown.addActionListener(this);
		
		GridBagConstraints cm = new GridBagConstraints();
		selectPenDownColor = new SelectColor(this,"pen down color",robot.getSettings().getPenDownColor());
		this.add(selectPenDownColor,cm);
		c.gridy++;
		selectPenUpColor = new SelectColor(this,"pen up color",robot.getSettings().getPenUpColor());
		this.add(selectPenUpColor,cm);
		c.gridy++;
	}
	
	
	public void actionPerformed(ActionEvent event) {
		Object subject = event.getSource();

		if (subject == buttonTestUp  ) {
			// must match MakelangeloRobotSettings.getPenUpString()
			robot.sendLineToRobot(
				"G00 F" + ((Number)penZRate.getValue()).floatValue() + " Z" + ((Number)penUp.getValue()).floatValue() + ";\n"+
				"G00 F" + ((Number)penFeedRate.getValue()).floatValue() + ";\n"
				);
		}
		if (subject == buttonTestDown) {
			// must match MakelangeloRobotSettings.getPenDownString()
			robot.sendLineToRobot(
					"G00 F" + ((Number)penZRate.getValue()).floatValue() + " Z" + ((Number)penDown.getValue()).floatValue() + ";\n"+
					"G00 F" + ((Number)penFeedRate.getValue()).floatValue() + ";\n"
					);
		}
	}
	
	
	public void save() {
	    MakelangeloRobotSettings settings = robot.getSettings();
		settings.setDiameter(((Number)penDiameter.getValue()).floatValue());
		settings.setMaxFeedRate(((Number)penFeedRate.getValue()).floatValue());
		settings.setZRate(((Number)penZRate.getValue()).floatValue());
		settings.setPenUpAngle(((Number)penUp.getValue()).floatValue());
		settings.setPenDownAngle(((Number)penDown.getValue()).floatValue());
		settings.setPenDownColor(selectPenDownColor.getColor());
		settings.setPenUpColor(selectPenUpColor.getColor());
	}
}
