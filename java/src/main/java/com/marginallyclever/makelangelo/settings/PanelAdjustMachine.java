package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustMachine extends JPanel implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;

	protected MakelangeloRobot robot;

	protected JFormattedTextField machineWidth, machineHeight;
	protected JLabel totalBeltNeeded;
	protected JLabel totalServoNeeded;
	protected JLabel totalStepperNeeded;
	protected JFormattedTextField acceleration;
	protected JFormattedTextField pulleyDiameter;
	protected JCheckBox flipForGlass;


	protected JButton buttonAneg;
	protected JButton buttonApos;
	protected JButton buttonBneg;
	protected JButton buttonBpos;

	protected JCheckBox m1i;
	protected JCheckBox m2i;

	public PanelAdjustMachine( MakelangeloRobot robot) {
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

		double r = robot.getSettings().getLimitRight() * 10;
		double l = robot.getSettings().getLimitLeft() * 10;
		double w = (r - l);
		double h = (robot.getSettings().getLimitTop() - robot.getSettings().getLimitBottom()) * 10;
		NumberFormat nFloat = NumberFormat.getNumberInstance();
		nFloat.setMinimumFractionDigits(1);
		nFloat.setMaximumFractionDigits(3);
		machineWidth = new JFormattedTextField(nFloat);
		machineWidth.setValue(w);
		machineHeight = new JFormattedTextField(nFloat);
		machineHeight.setValue(h);
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("MachineWidth")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(machineWidth, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel("mm"), d);
		y++;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("MachineHeight")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(machineHeight, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel("mm"), d);
		y++;
		
		Dimension s = machineHeight.getPreferredSize();
		s.width = 80;
		machineWidth.setPreferredSize(s);
		machineHeight.setPreferredSize(s);
		machineWidth.addPropertyChangeListener(this);
		machineHeight.addPropertyChangeListener(this);

		// stepper needed
		c.gridx = 0;
		c.gridwidth=1;
		c.gridy = y;
		p.add(new JLabel(Translator.get("StepperLengthNeeded")),c);
		d.gridx = 1;
		d.gridwidth=2;
		d.gridy = y;
		p.add(totalStepperNeeded = new JLabel("?"),d);
		y++;
		// belt needed
		c.gridx = 0;
		c.gridwidth=1;
		c.gridy = y;
		p.add(new JLabel(Translator.get("BeltLengthNeeded")),c);
		d.gridx = 1;
		d.gridwidth=2;
		d.gridy = y;
		p.add(totalBeltNeeded = new JLabel("?"),d);
		y++;
		// servo needed
		c.gridx = 0;
		c.gridwidth=1;
		c.gridy = y;
		p.add(new JLabel(Translator.get("ServoLengthNeeded")),c);
		d.gridx = 1;
		d.gridwidth=2;
		d.gridy = y;
		p.add(totalServoNeeded = new JLabel("?"),d);
		y++;

		// adjust pulleys
		this.add(new JSeparator(SwingConstants.HORIZONTAL));

		p = new JPanel(new GridBagLayout());
		this.add(p);

		c = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 0;
		c.gridwidth = 1;

		double left = Math.floor(robot.getSettings().getPulleyDiameter() * 10.0 * 1000.0) / 1000.0;

		NumberFormat nDouble = NumberFormat.getNumberInstance();
		nDouble.setMinimumFractionDigits(1);
		nDouble.setMaximumFractionDigits(3);

		pulleyDiameter = new JFormattedTextField(nDouble);
		pulleyDiameter.setValue(left);

		y = 2;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		d.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("AdjustPulleySize")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(pulleyDiameter, d);
		d.gridx = 2;
		d.gridy = y;
		p.add(new JLabel(Translator.get("Millimeters")), d);
		y++;

		s = pulleyDiameter.getPreferredSize();
		s.width = 80;
		pulleyDiameter.setPreferredSize(s);

		// acceleration
		acceleration = new JFormattedTextField(nDouble);
		acceleration.setValue(robot.getSettings().getAcceleration());
		s = acceleration.getPreferredSize();
		s.width = 80;
		acceleration.setPreferredSize(s);
		y = 0;
		c.gridx = 0;
		c.gridy = y;
		p.add(new JLabel(Translator.get("AdjustAcceleration")), c);
		d.gridx = 1;
		d.gridy = y;
		p.add(acceleration, d);
		y++;

		// Jog motors
		this.add(new JSeparator());
		JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel);

		buttonAneg = new JButton(Translator.get("JogIn"));
		buttonApos = new JButton(Translator.get("JogOut"));
		m1i = new JCheckBox(Translator.get("Invert"), robot.getSettings().isLeftMotorInverted());

		buttonBneg = new JButton(Translator.get("JogIn"));
		buttonBpos = new JButton(Translator.get("JogOut"));
		m2i = new JCheckBox(Translator.get("Invert"), robot.getSettings().isRightMotorInverted());

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
		flipForGlass.setSelected(robot.getSettings().isReverseForGlass());
		p.add(flipForGlass, c);
		
		updateLengthNeeded();
	}

	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		// jog motors
		if (subject == buttonApos) robot.jogLeftMotorOut();
		if (subject == buttonAneg) robot.jogLeftMotorIn();
		if (subject == buttonBpos) robot.jogRightMotorOut();
		if (subject == buttonBneg) robot.jogRightMotorIn();

		if (subject == m1i || subject == m2i) {
			robot.getSettings().invertLeftMotor(m1i.isSelected());
			robot.getSettings().invertRightMotor(m2i.isSelected());
			robot.getSettings().saveConfig();
			robot.sendConfig();
		}
	}
	
	/**
	 * Calculate length of belt and servo needed based on machine dimensions.
	 */
	protected void updateLengthNeeded() {
		double w = ((Number)machineWidth.getValue()).doubleValue();
		double h = ((Number)machineHeight.getValue()).doubleValue();
		double SAFETY_MARGIN=100;
		
		double mmBeltNeeded=(Math.sqrt(w*w+h*h)+SAFETY_MARGIN); // 10cm safety margin
		double beltNeeded = Math.ceil(mmBeltNeeded*0.001);
		totalBeltNeeded.setText(Double.toString(beltNeeded)+"m");
		
		double mmServoNeeded = (Math.sqrt(w*w+h*h)+SAFETY_MARGIN) + w/2.0; // 10cm safety margin
		double servoNeeded = Math.ceil(mmServoNeeded*0.001);
		totalServoNeeded.setText(Double.toString(servoNeeded)+"m");

		double mmStepperNeeded = w/2.0+SAFETY_MARGIN; // 10cm safety margin
		double stepperNeeded = Math.ceil(mmStepperNeeded*0.001);
		totalStepperNeeded.setText(Double.toString(stepperNeeded)+"m");
	}

	public void save() {
		double mwf = ((Number)machineWidth.getValue()).doubleValue() / 10.0;
		double mhf = ((Number)machineHeight.getValue()).doubleValue() / 10.0;
		double bld   = ((Number)pulleyDiameter.getValue()).doubleValue() / 10.0;
		double accel = ((Number)acceleration.getValue()).doubleValue();

		boolean data_is_sane = true;
		if (mwf <= 0) data_is_sane = false;
		if (mhf <= 0) data_is_sane = false;
		if (bld <= 0) data_is_sane = false;

		if (data_is_sane) {
			robot.getSettings().setReverseForGlass(flipForGlass.isSelected());
			robot.getSettings().setPulleyDiameter(bld);
			robot.getSettings().setMachineSize(mwf, mhf);
			robot.getSettings().setAcceleration(accel);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object subject = evt.getSource();

		if(subject == machineWidth || subject == machineHeight) {
			updateLengthNeeded();
		}
	}
}
