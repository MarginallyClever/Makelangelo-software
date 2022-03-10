package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;

/**
 * {@link PlotterSettingsPanel} is the user interface to adjust {@link PlotterSettings}.
 *
 * @author Dan Rmaybe oyer
 * @since 7.1.4
 */
public class PlotterSettingsPanel extends JPanel {

	private final Plotter myPlotter;

	private final SelectDouble machineWidth, machineHeight;
	private final SelectDouble totalBeltNeeded;
	private final SelectDouble totalServoNeeded;
	private final SelectDouble totalStepperNeeded;
	private final SelectDouble acceleration;
	
	private final SelectDouble penDiameter;
	private final SelectDouble travelFeedRate;
	private final SelectDouble drawFeedRate;
	private final SelectDouble penUpAngle;
	private final SelectDouble penDownAngle;
	private final SelectDouble penZRate;
	
	private final SelectColor selectPenDownColor;
	private final SelectColor selectPenUpColor;

	private final SelectInteger blockBufferSize;
	private final SelectInteger segmentsPerSecond;
	private final SelectDouble minSegmentLength;
	private final SelectInteger minSegTime;
	private final SelectBoolean handleSmallSegments;
	private final SelectDouble minAcceleration;
	private final SelectDouble minPlannerSpeed;
	
	public PlotterSettingsPanel(Plotter robot) {
		super();
		this.myPlotter = robot;

		JButton buttonSave = new JButton(Translator.get("Save"));
		buttonSave.addActionListener((e)->save());

		JButton buttonReset = new JButton(Translator.get("Reset"));
		buttonReset.addActionListener((e)->reset());

		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(buttonSave);
		bottom.add(buttonReset);

		SelectPanel interior = new SelectPanel();

		PlotterSettings settings = myPlotter.getSettings();
		
		double w = settings.getLimitRight() - settings.getLimitLeft();
		double h = settings.getLimitTop() - settings.getLimitBottom();
		interior.add(machineWidth 		= new SelectDouble("width",		 Translator.get("MachineWidth"		),w));
		interior.add(machineHeight 		= new SelectDouble("height",		 Translator.get("MachineHeight"		),h));
		interior.add(totalStepperNeeded = new SelectDouble("stepperLength", Translator.get("StepperLengthNeeded"),0));
		interior.add(totalBeltNeeded 	= new SelectDouble("beltLength",	 Translator.get("BeltLengthNeeded"	),0));
		interior.add(totalServoNeeded 	= new SelectDouble("servoLength",	 Translator.get("ServoLengthNeeded"	),0));
		interior.add(penDiameter 		= new SelectDouble("diameter",		 Translator.get("penToolDiameter"	),settings.getPenDiameter()));
	    interior.add(travelFeedRate 	= new SelectDouble("feedrate",		 Translator.get("penToolMaxFeedRate"),settings.getTravelFeedRate()));
	    interior.add(drawFeedRate 		= new SelectDouble("speed",		 Translator.get("Speed"				),settings.getDrawFeedRate()));
	    interior.add(acceleration 		= new SelectDouble("acceleration",	 Translator.get("AdjustAcceleration"),settings.getMaxAcceleration()));
		interior.add(penZRate 			= new SelectDouble("liftSpeed",	 Translator.get("penToolLiftSpeed"	),settings.getPenLiftTime()));
	    interior.add(penUpAngle 		= new SelectDouble("up",			 Translator.get("penToolUp"			),settings.getPenUpAngle()));
	    interior.add(penDownAngle 		= new SelectDouble("down",			 Translator.get("penToolDown"		),settings.getPenDownAngle()));
	    interior.add(selectPenDownColor = new SelectColor("colorDown",		 Translator.get("pen down color"	),settings.getPenDownColor(),this));
		interior.add(selectPenUpColor 	= new SelectColor("colorUp",		 Translator.get("pen up color"		),settings.getPenUpColor(),this));

		interior.add(blockBufferSize     = new SelectInteger("blockBufferSize",     Translator.get("PlotterSettings.blockBufferSize"     ),settings.getBlockBufferSize()));
		interior.add(segmentsPerSecond   = new SelectInteger("segmentsPerSecond",   Translator.get("PlotterSettings.segmentsPerSecond"   ),settings.getSegmentsPerSecond()));
		interior.add(minSegmentLength    = new SelectDouble ("minSegmentLength",    Translator.get("PlotterSettings.minSegmentLength"    ),settings.getMinSegmentLength()));
		interior.add(minSegTime          = new SelectInteger("minSegTime",          Translator.get("PlotterSettings.minSegTime"          ),(int)settings.getMinSegmentTime()));
		interior.add(handleSmallSegments = new SelectBoolean("handleSmallSegments", Translator.get("PlotterSettings.handleSmallSegments" ),settings.isHandleSmallSegments()));
		interior.add(minAcceleration     = new SelectDouble ("minAcceleration",     Translator.get("PlotterSettings.minAcceleration"     ),settings.getMinAcceleration()));
		interior.add(minPlannerSpeed     = new SelectDouble ("minPlannerSpeed",     Translator.get("PlotterSettings.minimumPlannerSpeed" ),settings.getMinPlannerSpeed()));
		

		machineWidth.addPropertyChangeListener((e)->updateLengthNeeded());
		machineHeight.addPropertyChangeListener((e)->updateLengthNeeded());
		totalStepperNeeded.setReadOnly();
		totalBeltNeeded.setReadOnly();
		totalServoNeeded.setReadOnly();
		updateLengthNeeded();

		// now assemble the dialog
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty=0;
		gbc.weightx=1;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weighty=1;
		this.add(interior,gbc);
		gbc.gridy++;
		gbc.weighty=0;
		this.add(bottom,gbc);
	}

	private void save() {
		PlotterSettings settings = myPlotter.getSettings();

		double mwf = machineWidth.getValue();
		double mhf = machineHeight.getValue();
		double accel = acceleration.getValue();

		boolean isDataSane = (mwf > 0 && mhf > 0);
		if(!isDataSane) {
			// TODO display a notice to the user?
			return;
		}
		
		settings.setMachineSize(mwf, mhf);
		settings.setAcceleration(accel);
	
		settings.setPenDiameter(penDiameter.getValue());
		settings.setTravelFeedRate(travelFeedRate.getValue());
		settings.setDrawFeedRate(drawFeedRate.getValue());
		settings.setPenLiftTime(penZRate.getValue());
		settings.setPenUpAngle(penUpAngle.getValue());
		settings.setPenDownAngle(penDownAngle.getValue());
		settings.setPenDownColor(selectPenDownColor.getColor());
		settings.setPenDownColorDefault(selectPenDownColor.getColor());
		settings.setPenUpColor(selectPenUpColor.getColor());
		
		settings.setBlockBufferSize(blockBufferSize.getValue());
		settings.setSegmentsPerSecond(segmentsPerSecond.getValue());
		settings.setMinSegmentLength(minSegmentLength.getValue());
		settings.setMinSegmentTime(minSegTime.getValue());
		settings.setHandleSmallSegments(handleSmallSegments.isSelected());
		settings.setMinAcceleration(minAcceleration.getValue());
		settings.setMinPlannerSpeed(minPlannerSpeed.getValue());

		settings.saveConfig();
	}

	private void reset() {
		PlotterSettings settings = myPlotter.getSettings();
		settings.reset();
		double w = settings.getLimitRight() - settings.getLimitLeft();
		double h = settings.getLimitTop() - settings.getLimitBottom();

		machineWidth.setValue(w);
		machineHeight.setValue(h);
		penDiameter.setValue(settings.getPenDiameter());
		travelFeedRate.setValue(settings.getTravelFeedRate());
		drawFeedRate.setValue(settings.getDrawFeedRate());
		acceleration.setValue(settings.getMaxAcceleration());
		penZRate.setValue(settings.getPenLiftTime());
		penUpAngle.setValue(settings.getPenUpAngle());
		penDownAngle.setValue(settings.getPenDownAngle());

		blockBufferSize.setValue(settings.getBlockBufferSize());
		segmentsPerSecond.setValue(settings.getSegmentsPerSecond());
		minSegmentLength.setValue(settings.getMinSegmentLength());
		minSegTime.setValue((int) settings.getMinSegmentTime());
		handleSmallSegments.setSelected(settings.isHandleSmallSegments());
		minAcceleration.setValue(settings.getMinAcceleration());
		minPlannerSpeed.setValue(settings.getMinPlannerSpeed());
	}

	/**
	 * Calculate length of belt and cables needed based on machine dimensions.
	 */
	private void updateLengthNeeded() {
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

	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		Plotter p = new Plotter();
		JFrame frame = new JFrame(PlotterSettingsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterSettingsPanel(p));
		frame.pack();
		frame.setVisible(true);	
	}
}
