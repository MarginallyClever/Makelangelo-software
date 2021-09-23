package com.marginallyclever.makelangeloRobot.settings;

import java.beans.PropertyChangeEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustMachine extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected MakelangeloRobot robot;

	protected SelectDouble machineWidth, machineHeight;
	protected SelectDouble totalBeltNeeded;
	protected SelectDouble totalServoNeeded;
	protected SelectDouble totalStepperNeeded;
	protected SelectDouble acceleration;

	protected SelectButton buttonAneg;
	protected SelectButton buttonApos;
	protected SelectButton buttonBneg;
	protected SelectButton buttonBpos;


	public PanelAdjustMachine(MakelangeloRobot robot) {
		super();
		
		this.robot = robot;

		// adjust machine size
		{
			float w = (float)(robot.getSettings().getLimitRight() - robot.getSettings().getLimitLeft());
			float h = (float)(robot.getSettings().getLimitTop() - robot.getSettings().getLimitBottom());
			
			machineWidth = new SelectDouble(Translator.get("MachineWidth"),w);
			machineHeight = new SelectDouble(Translator.get("MachineHeight"),h);

			add(machineWidth);
			//add(new JLabel("mm"));
			add(machineHeight);
			//add(new JLabel("mm"));
			//machineWidth.setPreferredSize(s);
			//machineHeight.setPreferredSize(s);
	
			add(totalStepperNeeded = new SelectDouble(Translator.get("StepperLengthNeeded"),0));
			add(totalBeltNeeded = new SelectDouble(Translator.get("BeltLengthNeeded"),0));
			add(totalServoNeeded = new SelectDouble(Translator.get("ServoLengthNeeded"),0));

			totalStepperNeeded.setReadOnly();
			totalBeltNeeded.setReadOnly();
			totalServoNeeded.setReadOnly();

			if(!robot.getSettings().getHardwareProperties().canChangeMachineSize()) {
				machineWidth.setReadOnly();
				machineHeight.setReadOnly();
			}
			//this.add(new JSeparator(SwingConstants.HORIZONTAL));
		}
		
		// Acceleration
		{
			acceleration = new SelectDouble(Translator.get("AdjustAcceleration"),(float)robot.getSettings().getMaxAcceleration());

			if(robot.getSettings().getHardwareProperties().canAccelerate()) {
				add(acceleration);
			}
		}

		// Jog motors
		{
			add(new SelectReadOnlyText(Translator.get("Left")));
	
			add(buttonAneg = new SelectButton(Translator.get("JogIn")));
			add(buttonApos = new SelectButton(Translator.get("JogOut")));

			add(new SelectReadOnlyText(Translator.get("Right")));
			add(buttonBneg = new SelectButton(Translator.get("JogIn")));
			add(buttonBpos = new SelectButton(Translator.get("JogOut")));
	
			if(!robot.getSettings().getHardwareProperties().canInvertMotors()) {
				interiorPanel.setVisible(false);
			}
		}
		finish();
		updateLengthNeeded();
	}
	
	/**
	 * Calculate length of belt and cables needed based on machine dimensions.
	 */
	protected void updateLengthNeeded() {
		double w = machineWidth.getValue();
		double h = machineHeight.getValue();
		double SAFETY_MARGIN=100;
		
		double mmBeltNeeded=(Math.sqrt(w*w+h*h)+SAFETY_MARGIN); // 10cm safety margin
		double beltNeeded = Math.ceil(mmBeltNeeded*0.001);
		totalBeltNeeded.setValue((float)beltNeeded);
		
		double mmServoNeeded = (Math.sqrt(w*w+h*h)+SAFETY_MARGIN) + w/2.0; // 10cm safety margin
		double servoNeeded = Math.ceil(mmServoNeeded*0.001);
		totalServoNeeded.setValue((float)servoNeeded);

		double mmStepperNeeded = w/2.0+SAFETY_MARGIN; // 10cm safety margin
		double stepperNeeded = Math.ceil(mmStepperNeeded*0.001);
		totalStepperNeeded.setValue((float)stepperNeeded);
	}

	public void save() {
		double mwf = machineWidth.getValue();
		double mhf = machineHeight.getValue();
		double accel = acceleration.getValue();

		boolean isDataSane = (mwf > 0 && mhf > 0);
		if (isDataSane) {
			robot.getSettings().setMachineSize(mwf, mhf);
			robot.getSettings().setAcceleration(accel);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();

		// jog motors
		     if(o == buttonApos) robot.jogLeftMotorOut();
		else if(o == buttonAneg) robot.jogLeftMotorIn();
		else if(o == buttonBpos) robot.jogRightMotorOut();
		else if(o == buttonBneg) robot.jogRightMotorIn();
		else {
			//updateLengthNeeded();
		}
	}
}
