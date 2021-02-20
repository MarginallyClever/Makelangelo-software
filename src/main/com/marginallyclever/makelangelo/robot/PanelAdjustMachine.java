package com.marginallyclever.makelangelo.robot;

import com.marginallyclever.core.select.SelectDouble;
import com.marginallyclever.core.select.SelectPanel;
import com.marginallyclever.makelangelo.Translator;

public class PanelAdjustMachine extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Robot myRobot;

	private SelectDouble machineWidth, machineHeight;
	private SelectDouble totalBeltNeeded;
	private SelectDouble totalServoNeeded;
	private SelectDouble totalStepperNeeded;
	private SelectDouble acceleration;


	public PanelAdjustMachine(Robot robot) {
		super();
		
		this.myRobot = robot;

		// adjust machine size
		{
			float w = (float)(robot.getSettings().getLimitRight() - robot.getSettings().getLimitLeft());
			float h = (float)(robot.getSettings().getLimitTop() - robot.getSettings().getLimitBottom());
			
			add(machineWidth = new SelectDouble(Translator.get("MachineWidth"),w));
			add(machineHeight = new SelectDouble(Translator.get("MachineHeight"),h));
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
			acceleration = new SelectDouble(Translator.get("AdjustAcceleration"),(float)robot.getSettings().getAcceleration());

			if(robot.getSettings().getHardwareProperties().canAccelerate()) {
				add(acceleration);
			}
		}

		
		if(!robot.getSettings().getHardwareProperties().canInvertMotors()) {
			interiorPanel.setVisible(false);
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
			myRobot.getSettings().setMachineSize(mwf, mhf);
			myRobot.getSettings().setAcceleration(accel);
		}
	}
}
