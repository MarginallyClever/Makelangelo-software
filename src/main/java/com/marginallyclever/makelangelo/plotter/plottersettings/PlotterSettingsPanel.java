package com.marginallyclever.makelangelo.plotter.plottersettings;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.donatello.select.*;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.plotterrenderer.PlotterRendererFactory;

import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link PlotterSettingsPanel} is the user interface to adjust {@link PlotterSettings}.
 *
 * @author Dan Rmaybe oyer
 * @since 7.1.4
 */
public class PlotterSettingsPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsPanel.class);
	private final PlotterSettings settings;
	private SelectOneOfMany visualStyle;
	private SelectDouble machineWidth, machineHeight;
	private SelectDouble totalBeltNeeded;
	private SelectDouble totalServoNeeded;
	private SelectDouble totalStepperNeeded;
	private SelectDouble acceleration;
	private SelectDouble penDiameter;
	private SelectDouble travelFeedRate;
	private SelectDouble drawFeedRate;
	private SelectDouble penUpAngle;
	private SelectDouble penDownAngle;
	private SelectDouble penRaiseRate;
	private SelectDouble penLowerRate;
	
	private SelectColor selectPenDownColor;
	private SelectColor selectPenUpColor;

	private SelectInteger blockBufferSize;
	private SelectInteger segmentsPerSecond;
	private SelectDouble minSegmentLength;
	private SelectInteger minSegTime;
	private SelectBoolean handleSmallSegments;
	private SelectDouble minAcceleration;
	private SelectDouble minPlannerSpeed;
	private SelectOneOfMany zMotorType;

	private SelectTextArea startGcode;
	private SelectTextArea endGcode;
	private SelectTextField findHomeGcode;
	private SelectTextField penUpGcode;
	private SelectTextField penDownGcode;

	private PlotterSettingsListener listener;

	public PlotterSettingsPanel(PlotterSettings settings) {
		super(new BorderLayout());
		setName("PlotterSettingsPanel");
		this.settings = settings;
		rebuildPanel();
	}

	private void rebuildPanel() {
		this.removeAll();

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabEssential"),rebuildTabMachine());
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabPen"),rebuildTabPen());
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabSimulation"),rebuildTabSimulate());
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabGCode"),rebuildTabGCode());

		JPanel bottom = new JPanel(new FlowLayout());
		if(!settings.isMostAncestral()) {
			JButton buttonSave = new JButton(Translator.get("Save"));
			buttonSave.addActionListener((e)->save());
			bottom.add(buttonSave);

			JButton buttonReset = new JButton(Translator.get("Reset"));
			buttonReset.addActionListener((e)->reset());
			bottom.add(buttonReset);
		} else {
			machineWidth.setReadOnly(true);
			machineHeight.setReadOnly(true);
		}

		machineWidth.addSelectListener((e)->updateLengthNeeded());
		machineHeight.addSelectListener((e)->updateLengthNeeded());

		totalStepperNeeded.setReadOnly(true);
		totalBeltNeeded.setReadOnly(true);
		totalServoNeeded.setReadOnly(true);
		updateLengthNeeded();

		// now assemble the dialog
		this.add(tabbedPane,BorderLayout.CENTER);
		this.add(bottom,BorderLayout.SOUTH);
		this.repaint();

		visualStyle.addSelectListener(e->updateSizeEditable());
	}

	private SelectPanel rebuildTabGCode() {
		var panel = new SelectPanel();

		addToPanel(panel,startGcode = new SelectTextArea("StartGcode", Translator.get("PlotterSettings.StartGcode"), settings.getString(PlotterSettings.START_GCODE)));
		addToPanel(panel,endGcode = new SelectTextArea("EndGcode", Translator.get("PlotterSettings.EndGcode"), settings.getString(PlotterSettings.END_GCODE)));
		addToPanel(panel,findHomeGcode = new SelectTextField("FindHomeGcode", Translator.get("PlotterSettings.FindHomeGcode"), settings.getFindHomeString()));
		addToPanel(panel,penUpGcode = new SelectTextField("PenUpGcode", Translator.get("PlotterSettings.PenUpGcode"), settings.getString(PlotterSettings.PEN_UP_GCODE)));
		addToPanel(panel,penDownGcode = new SelectTextField("PenDownGcode", Translator.get("PlotterSettings.PenDownGcode"), settings.getString(PlotterSettings.PEN_DOWN_GCODE)));

		startGcode.setLineWrap(false);
		endGcode.setLineWrap(false);

		return panel;
	}

	private SelectPanel rebuildTabSimulate() {
		var panel = new SelectPanel();
		addToPanel(panel,blockBufferSize     = new SelectInteger("blockBufferSize",     Translator.get("PlotterSettings.blockBufferSize"     ),settings.getInteger(PlotterSettings.BLOCK_BUFFER_SIZE)));
		addToPanel(panel,segmentsPerSecond   = new SelectInteger("segmentsPerSecond",   Translator.get("PlotterSettings.segmentsPerSecond"   ),settings.getInteger(PlotterSettings.SEGMENTS_PER_SECOND)));
		addToPanel(panel,minSegmentLength    = new SelectDouble ("minSegmentLength",    Translator.get("PlotterSettings.minSegmentLength"    ),settings.getDouble(PlotterSettings.MIN_SEGMENT_LENGTH)));
		addToPanel(panel,minSegTime          = new SelectInteger("minSegTime",          Translator.get("PlotterSettings.minSegTime"          ),settings.getInteger(PlotterSettings.MIN_SEG_TIME)));
		addToPanel(panel,handleSmallSegments = new SelectBoolean("handleSmallSegments", Translator.get("PlotterSettings.handleSmallSegments" ),settings.getBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS)));
		addToPanel(panel,minAcceleration     = new SelectDouble ("minAcceleration",     Translator.get("PlotterSettings.minAcceleration"     ),settings.getDouble(PlotterSettings.MIN_ACCELERATION)));
		addToPanel(panel,minPlannerSpeed     = new SelectDouble ("minPlannerSpeed",     Translator.get("PlotterSettings.minimumPlannerSpeed" ),settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED)));
		return panel;
	}

	private SelectPanel rebuildTabPen() {
		var panel = new SelectPanel();
		addToPanel(panel,penDiameter 		 = new SelectDouble("diameter",		 Translator.get("PlotterSettingsPanel.penToolDiameter"		),settings.getDouble(PlotterSettings.DIAMETER)));
		addToPanel(panel,travelFeedRate 	 = new SelectDouble("feedrate",		 Translator.get("PlotterSettingsPanel.penToolMaxFeedRate"	),settings.getDouble(PlotterSettings.FEED_RATE_TRAVEL)));
		addToPanel(panel,drawFeedRate 		 = new SelectDouble("speed",		 Translator.get("PlotterSettingsPanel.Speed"				),settings.getDouble(PlotterSettings.FEED_RATE_DRAW)));
		addToPanel(panel,acceleration 		 = new SelectDouble("acceleration",	 Translator.get("PlotterSettingsPanel.AdjustAcceleration"	),settings.getDouble(PlotterSettings.MAX_ACCELERATION)));
		addToPanel(panel,penRaiseRate        = new SelectDouble("liftSpeed",	 Translator.get("PlotterSettingsPanel.penToolLiftSpeed"		),settings.getDouble(PlotterSettings.PEN_ANGLE_UP_TIME)));
		addToPanel(panel,penLowerRate        = new SelectDouble("lowerSpeed",	 Translator.get("PlotterSettingsPanel.penToolLowerSpeed"	),settings.getDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME)));
		addToPanel(panel,penUpAngle 		 = new SelectDouble("up",			 Translator.get("PlotterSettingsPanel.penToolUp"			),settings.getDouble(PlotterSettings.PEN_ANGLE_UP)));
		addToPanel(panel,penDownAngle 		 = new SelectDouble("down",			 Translator.get("PlotterSettingsPanel.penToolDown"			),settings.getDouble(PlotterSettings.PEN_ANGLE_DOWN)));
		addToPanel(panel,selectPenUpColor 	 = new SelectColor("colorUp",		 Translator.get("PlotterSettingsPanel.penUpColor"			),settings.getColor(PlotterSettings.PEN_UP_COLOR),this));
		addToPanel(panel,selectPenDownColor  = new SelectColor("colorDown",		 Translator.get("PlotterSettingsPanel.penDownColor"		),settings.getColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT),this));

		return panel;
	}

	private SelectPanel rebuildTabMachine() {
		var panel = new SelectPanel();

		List<String> machineStyles = getMachineStyleNames();
		String myStyle = settings.getString(PlotterSettings.STYLE);
		int index = Math.max(0,machineStyles.indexOf(myStyle));
		addToPanel(panel,visualStyle         = new SelectOneOfMany("style",		 Translator.get("RobotMenu.RobotStyle"						), machineStyles.toArray(new String[0]), index));
		addToPanel(panel,machineWidth 		 = new SelectDouble("width",		 Translator.get("PlotterSettingsPanel.MachineWidth"			),settings.getDouble(PlotterSettings.LIMIT_RIGHT) - settings.getDouble(PlotterSettings.LIMIT_LEFT)));
		addToPanel(panel,machineHeight 	     = new SelectDouble("height",		 Translator.get("PlotterSettingsPanel.MachineHeight"		),settings.getDouble(PlotterSettings.LIMIT_TOP) - settings.getDouble(PlotterSettings.LIMIT_BOTTOM)));
		addToPanel(panel,totalStepperNeeded  = new SelectDouble("stepperLength", Translator.get("PlotterSettingsPanel.StepperLengthNeeded"	),0));
		addToPanel(panel,totalBeltNeeded 	 = new SelectDouble("beltLength",	 Translator.get("PlotterSettingsPanel.BeltLengthNeeded"		),0));
		addToPanel(panel,totalServoNeeded 	 = new SelectDouble("servoLength",	 Translator.get("PlotterSettingsPanel.ServoLengthNeeded"	),0));
		return panel;
	}

	private void updateSizeEditable() {
		var matches = !visualStyle.getSelectedItem().equals(PlotterRendererFactory.MAKELANGELO_CUSTOM.name());
		matches |= !settings.isMostAncestral();
		machineWidth.setReadOnly(matches);
		machineHeight.setReadOnly(matches);
	}

	private void addToPanel(SelectPanel container, Select element) {
		container.add(element);
		element.addSelectListener((e)->{
			save();
			fireSettingsChangedEvent();
		});
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

		List<String> machineStyles = getMachineStyleNames();
		settings.setString(PlotterSettings.STYLE,machineStyles.get(visualStyle.getSelectedIndex()));

		settings.setMachineSize(mwf, mhf);
		settings.setDouble(PlotterSettings.MAX_ACCELERATION,accel);
	
		settings.setDouble(PlotterSettings.DIAMETER,penDiameter.getValue());
		settings.setDouble(PlotterSettings.FEED_RATE_TRAVEL,travelFeedRate.getValue());
		settings.setDouble(PlotterSettings.FEED_RATE_DRAW,drawFeedRate.getValue());
		settings.setDouble(PlotterSettings.MAX_ACCELERATION,acceleration.getValue());
		settings.setDouble(PlotterSettings.PEN_ANGLE_UP_TIME,penRaiseRate.getValue());
		settings.setDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME,penLowerRate.getValue());
		settings.setDouble(PlotterSettings.PEN_ANGLE_UP,penUpAngle.getValue());
		settings.setDouble(PlotterSettings.PEN_ANGLE_DOWN,penDownAngle.getValue());
		settings.setColor(PlotterSettings.PAPER_COLOR,Color.WHITE);
		settings.setColor(PlotterSettings.PEN_DOWN_COLOR,selectPenDownColor.getColor());
		settings.setColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT,selectPenDownColor.getColor());
		settings.setColor(PlotterSettings.PEN_UP_COLOR,selectPenUpColor.getColor());
		
		settings.setInteger(PlotterSettings.BLOCK_BUFFER_SIZE,blockBufferSize.getValue());
		settings.setInteger(PlotterSettings.SEGMENTS_PER_SECOND,segmentsPerSecond.getValue());
		settings.setDouble(PlotterSettings.MIN_SEGMENT_LENGTH,minSegmentLength.getValue());
		settings.setInteger(PlotterSettings.MIN_SEG_TIME,minSegTime.getValue());
		settings.setBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS,handleSmallSegments.isSelected());
		settings.setDouble(PlotterSettings.MIN_ACCELERATION,minAcceleration.getValue());
		settings.setDouble(PlotterSettings.MINIMUM_PLANNER_SPEED,minPlannerSpeed.getValue());

		settings.setString(PlotterSettings.START_GCODE,startGcode.getText());
		settings.setString(PlotterSettings.END_GCODE,endGcode.getText());
		settings.setString(PlotterSettings.FIND_HOME_GCODE,findHomeGcode.getText());
		settings.setString(PlotterSettings.PEN_UP_GCODE,penUpGcode.getText());
		settings.setString(PlotterSettings.PEN_DOWN_GCODE,penDownGcode.getText());

		settings.save();
	}

	private List<String> getMachineStyleNames() {
		List<String> machineStyleNames = new ArrayList<>();
		for(PlotterRendererFactory m : PlotterRendererFactory.values()) {
			machineStyleNames.add(m.name());
		}
		return machineStyleNames;
	}

	private void reset() {
		settings.reset();
		rebuildPanel();
		fireSettingsChangedEvent();
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

	public void addListener(PlotterSettingsListener listener) {
		this.listener = listener;
	}

	private void fireSettingsChangedEvent() {
		if(listener!=null) {
			listener.settingsChangedEvent(settings);
		}
	}

	/**
	 * Start the PlotterSettingsPanel.
 	 * @param args not used
	 */
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			logger.warn("failed to set native look and feel.", ex);
		}

		PlotterSettings plotterSettings = new PlotterSettings();
		JFrame frame = new JFrame(PlotterSettingsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterSettingsPanel(plotterSettings));
		frame.pack();
		frame.setVisible(true);	
	}
}
