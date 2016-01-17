package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;

public class PanelAdjustMachine extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;

	protected Translator translator;
	protected MakelangeloRobot robot;

	protected JFormattedTextField mw, mh;
	protected JFormattedTextField acceleration;
	protected JFormattedTextField pulleyDiameterLeft, pulleyDiameterRight;
	protected JCheckBox flipForGlass;


	protected JButton buttonAneg;
	protected JButton buttonApos;
	protected JButton buttonBneg;
	protected JButton buttonBpos;

	protected JCheckBox m1i;
	protected JCheckBox m2i;

	public PanelAdjustMachine(Translator translator, MakelangeloRobot robot) {
		this.translator = translator;
		this.robot = robot;

		this.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		// this.setLayout(new GridLayout(0,1,8,8));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 0;

		// adjust machine size
		JPanel p = new JPanel(new GridBagLayout());
		this.add(p);

		c.gridwidth = 3;
		p.add(new JLabel("1\" = 25.4mm", SwingConstants.CENTER), c);
		c.gridwidth = 1;

		int y = 1;

		c.anchor = GridBagConstraints.EAST;
		d.anchor = GridBagConstraints.WEST;

		double r = robot.settings.getLimitRight();
		double l = robot.settings.getLimitLeft();
		double w = (r - l) * 10;
		double h = (robot.settings.getLimitTop() - robot.settings.getLimitBottom()) * 10;
		NumberFormat nFloat = NumberFormat.getNumberInstance();
		nFloat.setMinimumFractionDigits(1);
		nFloat.setMaximumFractionDigits(3);
		mw = new JFormattedTextField(nFloat);
		mw.setValue(w);
		mh = new JFormattedTextField(nFloat);
		mh.setValue(h);
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("MachineWidth")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(mw, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel("mm"), d);
		y++;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("MachineHeight")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(mh, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel("mm"), d);
		y++;

		Dimension s = mh.getPreferredSize();
		s.width = 80;
		mw.setPreferredSize(s);
		mh.setPreferredSize(s);

		// adjust pulleys
		this.add(new JSeparator(SwingConstants.HORIZONTAL));

		p = new JPanel(new GridBagLayout());
		this.add(p);

		c = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 0;
		c.gridwidth = 3;
		p.add(new JLabel(Translator.get("AdjustPulleySize"), SwingConstants.CENTER), c);
		c.gridwidth = 1;

		double left = Math.floor(robot.settings.getPulleyDiameterLeft() * 10.0 * 1000.0) / 1000.0;
		double right = Math.floor(robot.settings.getPulleyDiameterRight() * 10.0 * 1000.0) / 1000.0;

		NumberFormat nDouble = NumberFormat.getNumberInstance();
		nDouble.setMinimumFractionDigits(1);
		nDouble.setMaximumFractionDigits(3);

		pulleyDiameterLeft = new JFormattedTextField(nDouble);
		pulleyDiameterLeft.setValue(left);
		pulleyDiameterRight = new JFormattedTextField(nDouble);
		pulleyDiameterRight.setValue(right);

		y = 2;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		d.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("Left")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(pulleyDiameterLeft, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel(Translator.get("Millimeters")), d);
		y++;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("Right")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(pulleyDiameterRight, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel(Translator.get("Millimeters")), d);

		s = pulleyDiameterLeft.getPreferredSize();
		s.width = 80;
		pulleyDiameterLeft.setPreferredSize(s);
		pulleyDiameterRight.setPreferredSize(s);

		// Jog motors
		this.add(new JSeparator());
		JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel);

		buttonAneg = new JButton(Translator.get("JogIn"));
		buttonApos = new JButton(Translator.get("JogOut"));
		m1i = new JCheckBox(Translator.get("Invert"), robot.settings.isMotor1Backwards());

		buttonBneg = new JButton(Translator.get("JogIn"));
		buttonBpos = new JButton(Translator.get("JogOut"));
		m2i = new JCheckBox(Translator.get("Invert"), robot.settings.isMotor2Backwards());

		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel(Translator.get("Left")), c);
		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel(Translator.get("Right")), c);

		c.gridx = 1;
		c.gridy = 0;
		panel.add(buttonAneg, c);
		c.gridx = 1;
		c.gridy = 1;
		panel.add(buttonBneg, c);

		c.gridx = 2;
		c.gridy = 0;
		panel.add(buttonApos, c);
		c.gridx = 2;
		c.gridy = 1;
		panel.add(buttonBpos, c);

		c.gridx = 3;
		c.gridy = 0;
		panel.add(m1i, c);
		c.gridx = 3;
		c.gridy = 1;
		panel.add(m2i, c);

		buttonApos.addActionListener(this);
		buttonAneg.addActionListener(this);

		buttonBpos.addActionListener(this);
		buttonBneg.addActionListener(this);

		m1i.addActionListener(this);
		m2i.addActionListener(this);

		// acceleration
		this.add(new JSeparator());
		p = new JPanel(new GridBagLayout());
		this.add(p);

		acceleration = new JFormattedTextField(nDouble);
		acceleration.setValue(robot.settings.getAcceleration());
		s = acceleration.getPreferredSize();
		s.width = 80;
		acceleration.setPreferredSize(s);
		y = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		d.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("AdjustAcceleration")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(acceleration, d);
		y++;

		// flip for glass
		this.add(new JSeparator());
		p = new JPanel(new GridBagLayout());
		this.add(p);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		flipForGlass = new JCheckBox(Translator.get("FlipForGlass"));
		flipForGlass.setSelected(robot.settings.isReverseForGlass());
		p.add(flipForGlass, c);
	}

	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		// jog motors
		if (subject == buttonApos)
			robot.sendLineToRobot("D00 L400");
		if (subject == buttonAneg)
			robot.sendLineToRobot("D00 L-400");
		if (subject == buttonBpos)
			robot.sendLineToRobot("D00 R400");
		if (subject == buttonBneg)
			robot.sendLineToRobot("D00 R-400");

		if (subject == m1i || subject == m2i) {
			robot.settings.setMotor1Backwards(m1i.isSelected());
			robot.settings.setMotor2Backwards(m2i.isSelected());
			robot.settings.saveConfig();
			robot.sendConfig();
		}
	}

	public void save() {
		double mwf = ((Number)mw.getValue()).doubleValue() / 10.0;
		double mhf = ((Number)mh.getValue()).doubleValue() / 10.0;
		boolean data_is_sane = true;
		if (mwf <= 0)
			data_is_sane = false;
		if (mhf <= 0)
			data_is_sane = false;

		double bld   = ((Number)pulleyDiameterLeft .getValue()).doubleValue() / 10.0;
		double brd   = ((Number)pulleyDiameterRight.getValue()).doubleValue() / 10.0;
		double accel = ((Number)acceleration       .getValue()).doubleValue();

		if (bld <= 0)
			data_is_sane = false;
		if (brd <= 0)
			data_is_sane = false;

		if (data_is_sane) {
			robot.settings.setReverseForGlass(flipForGlass.isSelected());
			robot.settings.setPulleyDiameter(bld, brd);
			robot.settings.setMachineSize(mwf, mhf);
			robot.settings.setAcceleration(accel);
		}
	}
}
