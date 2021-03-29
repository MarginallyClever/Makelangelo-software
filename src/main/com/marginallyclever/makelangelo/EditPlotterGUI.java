package com.marginallyclever.makelangelo;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.select.SelectDouble;
import com.marginallyclever.core.select.SelectInteger;
import com.marginallyclever.core.select.SelectPanel;
import com.marginallyclever.core.select.SelectString;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class EditPlotterGUI extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Plotter myPlotter;

	private SelectString nickname;
	
	private SelectDouble machineWidth, machineHeight;
	private SelectDouble totalBeltNeeded;
	private SelectDouble totalServoNeeded;
	private SelectDouble totalStepperNeeded;
	private SelectDouble acceleration;
	private SelectInteger minimumSegmentTime;
	
	protected SelectDouble travelFeedRate;
	protected SelectDouble drawFeedRate;
	protected SelectDouble penUp;
	protected SelectDouble penDown;
	protected SelectDouble penZRate;


	public EditPlotterGUI(Plotter robot) {
		super();
		
		myPlotter = robot;

		// adjust machine size
		float w = (float)(myPlotter.getLimitRight() - myPlotter.getLimitLeft());
		float h = (float)(myPlotter.getLimitTop() - myPlotter.getLimitBottom());
		
		add(nickname = new SelectString(Translator.get("MachineNickname"),myPlotter.getNickname()));
		add(machineWidth = new SelectDouble(Translator.get("MachineWidth"),w));
		add(machineHeight = new SelectDouble(Translator.get("MachineHeight"),h));
		//machineWidth.setPreferredSize(s);
		//machineHeight.setPreferredSize(s);

		add(totalStepperNeeded = new SelectDouble(Translator.get("StepperLengthNeeded"),0));
		add(totalBeltNeeded = new SelectDouble(Translator.get("BeltLengthNeeded"),0));
		add(totalServoNeeded = new SelectDouble(Translator.get("ServoLengthNeeded"),0));

	    add(travelFeedRate = new SelectDouble(Translator.get("penToolMaxFeedRate"),myPlotter.getTravelFeedRate()));
	    add(drawFeedRate = new SelectDouble(Translator.get("Speed"),myPlotter.getDrawingFeedRate()));

		add(acceleration = new SelectDouble(Translator.get("AdjustAcceleration"),myPlotter.getAcceleration()));
		add(minimumSegmentTime = new SelectInteger(Translator.get("minimumSegmentTime"),myPlotter.getMinimumSegmentTime()));
		
	    add(penZRate = new SelectDouble(Translator.get("penToolLiftSpeed"),myPlotter.getZFeedrate()));
	    add(penUp = new SelectDouble(Translator.get("penToolUp"),myPlotter.getPenUpAngle()));
	    add(penDown = new SelectDouble(Translator.get("penToolDown"),myPlotter.getPenDownAngle()));
	    
		totalStepperNeeded.setReadOnly();
		totalBeltNeeded.setReadOnly();
		totalServoNeeded.setReadOnly();

		if(!myPlotter.canChangeMachineSize()) {
			machineWidth.setReadOnly();
			machineHeight.setReadOnly();
		}
		//this.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		
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
			myPlotter.setNickname(nickname.getText());
			myPlotter.setMachineSize(mwf, mhf);
			myPlotter.setAcceleration(accel);

			myPlotter.setTravelFeedRate(travelFeedRate.getValue());
			myPlotter.setDrawingFeedRate(drawFeedRate.getValue());
			myPlotter.setMinimumSegmentTime(minimumSegmentTime.getValue());
			myPlotter.setZFeedrate(penZRate.getValue());
			myPlotter.setPenUpAngle(penUp.getValue());
			myPlotter.setPenDownAngle(penDown.getValue());
		}
	}
}
