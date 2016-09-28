package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.marginallyclever.makelangelo.FloatField;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustMachine extends JPanel implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;

	protected MakelangeloRobot robot;

	protected FloatField machineWidth, machineHeight;
	protected JLabel totalBeltNeeded;
	protected JLabel totalServoNeeded;
	protected JLabel totalStepperNeeded;
	protected FloatField acceleration;
	protected FloatField pulleyDiameter;
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

		int y = 1;
		JPanel p;
		Dimension s;
		
		// adjust machine size
		{
			p = new JPanel(new GridBagLayout());
			this.add(p);
	
			double r = robot.getSettings().getLimitRight() * 10;
			double l = robot.getSettings().getLimitLeft() * 10;
			float w = (float)(r - l);
			float h = (float)(robot.getSettings().getLimitTop() - robot.getSettings().getLimitBottom()) * 10;
			
			machineWidth = new FloatField(w);
			machineHeight = new FloatField(h);
			s = machineHeight.getPreferredSize();
			s.width = 80;
	
			if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) {
				machineWidth.setValue(robot.getSettings().getHardwareProperties().getWidth());
				machineHeight.setValue(robot.getSettings().getHardwareProperties().getHeight());
			}			
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.EAST;
			d.anchor = GridBagConstraints.WEST;
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
	
			if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) {
				p.setVisible(false);
			} else {
				this.add(new JSeparator(SwingConstants.HORIZONTAL));
			}
		}
		
		// adjust pulleys
		{
			p = new JPanel(new GridBagLayout());
			this.add(p);
	
			c = new GridBagConstraints();
			c.ipadx = 5;
			c.ipady = 0;
			c.gridwidth = 1;
	
			double startingDiameter = Math.floor(robot.getSettings().getPulleyDiameter() * 10.0 * 1000.0) / 1000.0;
	
			// pulley diameter
			pulleyDiameter = new FloatField();
			pulleyDiameter.setValue(startingDiameter);
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
			
			if(!robot.getSettings().getHardwareProperties().canChangePulleySize()) {
				p.setVisible(false);
			} else {
				this.add(new JSeparator(SwingConstants.HORIZONTAL));
			}
		}

		// acceleration
		{
			p = new JPanel(new GridBagLayout());
			this.add(p);
			
			acceleration = new FloatField();
			acceleration.setValue(robot.getSettings().getAcceleration());
			s = acceleration.getPreferredSize();
			s.width = 80;
			acceleration.setPreferredSize(s);
			c.gridx = 0;
			c.gridy = 0;
			p.add(new JLabel(Translator.get("AdjustAcceleration")), c);
			d.gridx = 1;
			d.gridy = 0;
			p.add(acceleration, d);
			if(!robot.getSettings().getHardwareProperties().canAccelerate()) {
				p.setVisible(false);
			}
		}

		// Jog motors
		{
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

			if(!robot.getSettings().getHardwareProperties().canInvertMotors()) {
				panel.setVisible(false);
			} else {
				this.add(new JSeparator(SwingConstants.HORIZONTAL));
			}
		}
		
		// flip for glass
		{
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
		}

		// always have one extra empty at the end to push everything up.
		c.weighty = 1;
		p.add(new JLabel(), c);
		
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
		if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) return;
		
		double w = ((Number)machineWidth.getValue()).floatValue();
		double h = ((Number)machineHeight.getValue()).floatValue();
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
		float accel = ((Number)acceleration.getValue()).floatValue();

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
