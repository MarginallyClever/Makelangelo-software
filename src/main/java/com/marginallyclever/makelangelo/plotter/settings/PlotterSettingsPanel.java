package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.Machines;
import com.marginallyclever.makelangelo.select.*;
import com.marginallyclever.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;

/**
 * {@link PlotterSettingsPanel} is the user interface to adjust {@link PlotterSettings}.
 *
 * @author Dan Rmaybe oyer
 * @since 7.1.4
 */
public class PlotterSettingsPanel extends JPanel {
	private final PlotterSettings settings;

	private final PlotterSettingsUserGcodePanel userGcodePanel;

	private final SelectOneOfMany style;
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
	private final SelectDouble penRaiseRate;
	private final SelectDouble penLowerRate;
	
	private final SelectColor selectPenDownColor;
	private final SelectColor selectPenUpColor;

	private final SelectInteger blockBufferSize;
	private final SelectInteger segmentsPerSecond;
	private final SelectDouble minSegmentLength;
	private final SelectInteger minSegTime;
	private final SelectBoolean handleSmallSegments;
	private final SelectDouble minAcceleration;
	private final SelectDouble minPlannerSpeed;
	private final SelectOneOfMany zMotorType;

	public PlotterSettingsPanel(PlotterSettings settings) {
		super();
		this.settings = settings;

		userGcodePanel = new PlotterSettingsUserGcodePanel(settings);

		JButton buttonSave = new JButton(Translator.get("Save"));
		buttonSave.addActionListener((e)->save());

		JButton buttonReset = new JButton(Translator.get("Reset"));
		buttonReset.addActionListener((e)->reset());

		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(buttonSave);
		bottom.add(buttonReset);

		SelectPanel interior0 = new SelectPanel();
		SelectPanel interior1 = new SelectPanel();
		SelectPanel interior2 = new SelectPanel();

		List<String> machineStyles = getMachineStyleNames();
		int index = machineStyles.indexOf(settings.getStyle());

		interior0.add(style              = new SelectOneOfMany("style",       Translator.get("PlotterSettingsPanel.Style"				), machineStyles.toArray(new String[0]), index));
		interior0.add(machineWidth 		 = new SelectDouble("width",		 	 Translator.get("PlotterSettingsPanel.MachineWidth"			),settings.getLimitRight() - settings.getLimitLeft()));
		interior0.add(machineHeight 	 = new SelectDouble("height",		 Translator.get("PlotterSettingsPanel.MachineHeight"		),settings.getLimitTop() - settings.getLimitBottom()));
		interior0.add(totalStepperNeeded = new SelectDouble("stepperLength",  Translator.get("PlotterSettingsPanel.StepperLengthNeeded"	),0));
		interior0.add(totalBeltNeeded 	 = new SelectDouble("beltLength",	 Translator.get("PlotterSettingsPanel.BeltLengthNeeded"		),0));
		interior0.add(totalServoNeeded 	 = new SelectDouble("servoLength",	 Translator.get("PlotterSettingsPanel.ServoLengthNeeded"	),0));

		interior1.add(penDiameter 		 = new SelectDouble("diameter",		 Translator.get("PlotterSettingsPanel.penToolDiameter"		),settings.getPenDiameter()));
	    interior1.add(travelFeedRate 	 = new SelectDouble("feedrate",		 Translator.get("PlotterSettingsPanel.penToolMaxFeedRate"	),settings.getTravelFeedRate()));
	    interior1.add(drawFeedRate 		 = new SelectDouble("speed",		 	 Translator.get("PlotterSettingsPanel.Speed"				),settings.getDrawFeedRate()));
	    interior1.add(acceleration 		 = new SelectDouble("acceleration",	 Translator.get("PlotterSettingsPanel.AdjustAcceleration"	),settings.getMaxAcceleration()));
		interior1.add(penRaiseRate       = new SelectDouble("liftSpeed",	 	 Translator.get("PlotterSettingsPanel.penToolLiftSpeed"		),settings.getPenLiftTime()));
		interior1.add(penLowerRate       = new SelectDouble("lowerSpeed",	 Translator.get("PlotterSettingsPanel.penToolLowerSpeed"	),settings.getPenLowerTime()));
	    interior1.add(penUpAngle 		 = new SelectDouble("up",			 Translator.get("PlotterSettingsPanel.penToolUp"			),settings.getPenUpAngle()));
	    interior1.add(penDownAngle 		 = new SelectDouble("down",			 Translator.get("PlotterSettingsPanel.penToolDown"			),settings.getPenDownAngle()));
		interior1.add(selectPenUpColor 	 = new SelectColor("colorUp",		 Translator.get("PlotterSettingsPanel.pen up color"			),settings.getPenUpColor(),this));
		interior1.add(selectPenDownColor = new SelectColor("colorDown",		 Translator.get("PlotterSettingsPanel.pen down color"		),settings.getPenDownColor(),this));

		interior1.add(zMotorType          = new SelectOneOfMany("zMotorType",Translator.get("PlotterSettings.zMotorType"),new String[]{
				Translator.get("PlotterSettings.zMotorType.servo"),  // PlotterSettings.Z_MOTOR_TYPE_SERVO = 1
				Translator.get("PlotterSettings.zMotorType.stepper"),  // PlotterSettings.Z_MOTOR_TYPE_STEPPER = 2
		},settings.getZMotorType()-1));

		interior2.add(blockBufferSize     = new SelectInteger("blockBufferSize",     Translator.get("PlotterSettings.blockBufferSize"     ),settings.getBlockBufferSize()));
		interior2.add(segmentsPerSecond   = new SelectInteger("segmentsPerSecond",   Translator.get("PlotterSettings.segmentsPerSecond"   ),settings.getSegmentsPerSecond()));
		interior2.add(minSegmentLength    = new SelectDouble ("minSegmentLength",    Translator.get("PlotterSettings.minSegmentLength"    ),settings.getMinSegmentLength()));
		interior2.add(minSegTime          = new SelectInteger("minSegTime",          Translator.get("PlotterSettings.minSegTime"          ),(int)settings.getMinSegmentTime()));
		interior2.add(handleSmallSegments = new SelectBoolean("handleSmallSegments", Translator.get("PlotterSettings.handleSmallSegments" ),settings.isHandleSmallSegments()));
		interior2.add(minAcceleration     = new SelectDouble ("minAcceleration",     Translator.get("PlotterSettings.minAcceleration"     ),settings.getMinAcceleration()));
		interior2.add(minPlannerSpeed     = new SelectDouble ("minPlannerSpeed",     Translator.get("PlotterSettings.minimumPlannerSpeed" ),settings.getMinPlannerSpeed()));

		machineWidth.addPropertyChangeListener((e)->updateLengthNeeded());
		machineHeight.addPropertyChangeListener((e)->updateLengthNeeded());
		totalStepperNeeded.setReadOnly();
		totalBeltNeeded.setReadOnly();
		totalServoNeeded.setReadOnly();
		updateLengthNeeded();

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabEssential"),interior0);
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabPen"),interior1);
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabSimulation"),interior2);
		tabbedPane.addTab(Translator.get("PlotterSettingsUserGcodePanel.Title"),userGcodePanel);

		// now assemble the dialog
		this.setLayout(new BorderLayout());
		this.add(tabbedPane,BorderLayout.CENTER);
		this.add(bottom,BorderLayout.SOUTH);
	}

	private void save() {
		double mwf = machineWidth.getValue();
		double mhf = machineHeight.getValue();
		double accel = acceleration.getValue();

		boolean isDataSane = (mwf > 0 && mhf > 0);
		if(!isDataSane) {
			// TODO display a notice to the user?
			return;
		}

		userGcodePanel.save();
		
		settings.setMachineSize(mwf, mhf);
		settings.setAcceleration(accel);
	
		settings.setPenDiameter(penDiameter.getValue());
		settings.setTravelFeedRate(travelFeedRate.getValue());
		settings.setDrawFeedRate(drawFeedRate.getValue());
		settings.setPenLiftTime(penRaiseRate.getValue());
		settings.setPenLowerTime(penLowerRate.getValue());
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
		settings.setZMotorType(zMotorType.getSelectedIndex()+1);

		List<String> machineStyles = getMachineStyleNames();
		settings.setStyle(machineStyles.get(style.getSelectedIndex()));

		settings.saveConfig();
	}

	private List<String> getMachineStyleNames() {
		List<String> machineStyleNames = new ArrayList<>();
		for(Machines m : Machines.values()) {
			machineStyleNames.add(m.name());
		}
		return machineStyleNames;
	}

	private void reset() {
		settings.reset();
		double w = settings.getLimitRight() - settings.getLimitLeft();
		double h = settings.getLimitTop() - settings.getLimitBottom();

		userGcodePanel.reset();

		machineWidth.setValue(w);
		machineHeight.setValue(h);
		penDiameter.setValue(settings.getPenDiameter());
		travelFeedRate.setValue(settings.getTravelFeedRate());
		drawFeedRate.setValue(settings.getDrawFeedRate());
		acceleration.setValue(settings.getMaxAcceleration());
		penRaiseRate.setValue(settings.getPenLiftTime());
		penLowerRate.setValue(settings.getPenLowerTime());
		penUpAngle.setValue(settings.getPenUpAngle());
		penDownAngle.setValue(settings.getPenDownAngle());

		blockBufferSize.setValue(settings.getBlockBufferSize());
		segmentsPerSecond.setValue(settings.getSegmentsPerSecond());
		minSegmentLength.setValue(settings.getMinSegmentLength());
		minSegTime.setValue((int) settings.getMinSegmentTime());
		handleSmallSegments.setSelected(settings.isHandleSmallSegments());
		minAcceleration.setValue(settings.getMinAcceleration());
		minPlannerSpeed.setValue(settings.getMinPlannerSpeed());
		zMotorType.setSelectedIndex(settings.getZMotorType()-1);
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

		PlotterSettings plotterSettings = new PlotterSettings();
		JFrame frame = new JFrame(PlotterSettingsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterSettingsPanel(plotterSettings));
		frame.pack();
		frame.setVisible(true);	
	}
}
