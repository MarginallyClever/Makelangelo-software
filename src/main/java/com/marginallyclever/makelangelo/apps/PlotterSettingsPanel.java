package com.marginallyclever.makelangelo.apps;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer.PlotterRendererFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsListener;
import com.marginallyclever.makelangelo.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
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
	private PlotterSettingsUserGcodePanel userGcodePanel;
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

	private final EventListenerList listeners = new EventListenerList();

	public PlotterSettingsPanel() {
		this(new PlotterSettings());
	}

	public PlotterSettingsPanel(PlotterSettings settings) {
		super(new BorderLayout());
		this.settings = settings;
		rebuildPanel();
	}

	private void rebuildPanel() {
		this.removeAll();
		userGcodePanel = new PlotterSettingsUserGcodePanel(settings);

		JButton buttonSave = new JButton(Translator.get("Save"));
		buttonSave.addActionListener((e)->save());

		JButton buttonReset = new JButton(Translator.get("Reset"));
		buttonReset.addActionListener((e)->reset());

		JPanel bottom = new JPanel(new FlowLayout());

		SelectPanel interior0 = new SelectPanel();
		SelectPanel interior1 = new SelectPanel();
		SelectPanel interior2 = new SelectPanel();

		List<String> machineStyles = getMachineStyleNames();
		String myStyle = settings.getString(PlotterSettings.STYLE);
		int index = Math.max(0,machineStyles.indexOf(myStyle));

		addToPanel(interior0,visualStyle         = new SelectOneOfMany("style",		 Translator.get("RobotMenu.RobotStyle"						), machineStyles.toArray(new String[0]), index));
		addToPanel(interior0,machineWidth 		 = new SelectDouble("width",		 Translator.get("PlotterSettingsPanel.MachineWidth"			),settings.getDouble(PlotterSettings.LIMIT_RIGHT) - settings.getDouble(PlotterSettings.LIMIT_LEFT)));
		addToPanel(interior0,machineHeight 	     = new SelectDouble("height",		 Translator.get("PlotterSettingsPanel.MachineHeight"		),settings.getDouble(PlotterSettings.LIMIT_TOP) - settings.getDouble(PlotterSettings.LIMIT_BOTTOM)));
		addToPanel(interior0,totalStepperNeeded  = new SelectDouble("stepperLength", Translator.get("PlotterSettingsPanel.StepperLengthNeeded"	),0));
		addToPanel(interior0,totalBeltNeeded 	 = new SelectDouble("beltLength",	 Translator.get("PlotterSettingsPanel.BeltLengthNeeded"		),0));
		addToPanel(interior0,totalServoNeeded 	 = new SelectDouble("servoLength",	 Translator.get("PlotterSettingsPanel.ServoLengthNeeded"	),0));

		addToPanel(interior1,penDiameter 		 = new SelectDouble("diameter",		 Translator.get("PlotterSettingsPanel.penToolDiameter"		),settings.getDouble(PlotterSettings.DIAMETER)));
	    addToPanel(interior1,travelFeedRate 	 = new SelectDouble("feedrate",		 Translator.get("PlotterSettingsPanel.penToolMaxFeedRate"	),settings.getDouble(PlotterSettings.FEED_RATE_TRAVEL)));
	    addToPanel(interior1,drawFeedRate 		 = new SelectDouble("speed",		 Translator.get("PlotterSettingsPanel.Speed"				),settings.getDouble(PlotterSettings.FEED_RATE_DRAW)));
	    addToPanel(interior1,acceleration 		 = new SelectDouble("acceleration",	 Translator.get("PlotterSettingsPanel.AdjustAcceleration"	),settings.getDouble(PlotterSettings.MAX_ACCELERATION)));
		addToPanel(interior1,penRaiseRate        = new SelectDouble("liftSpeed",	 Translator.get("PlotterSettingsPanel.penToolLiftSpeed"		),settings.getDouble(PlotterSettings.PEN_ANGLE_UP_TIME)));
		addToPanel(interior1,penLowerRate        = new SelectDouble("lowerSpeed",	 Translator.get("PlotterSettingsPanel.penToolLowerSpeed"	),settings.getDouble(PlotterSettings.PEN_ANGLE_DOWN_TIME)));
	    addToPanel(interior1,penUpAngle 		 = new SelectDouble("up",			 Translator.get("PlotterSettingsPanel.penToolUp"			),settings.getDouble(PlotterSettings.PEN_ANGLE_UP)));
	    addToPanel(interior1,penDownAngle 		 = new SelectDouble("down",			 Translator.get("PlotterSettingsPanel.penToolDown"			),settings.getDouble(PlotterSettings.PEN_ANGLE_DOWN)));
		addToPanel(interior1,selectPenUpColor 	 = new SelectColor("colorUp",		 Translator.get("PlotterSettingsPanel.pen up color"			),settings.getColor(PlotterSettings.PEN_UP_COLOR),this));
		addToPanel(interior1,selectPenDownColor  = new SelectColor("colorDown",		 Translator.get("PlotterSettingsPanel.pen down color"		),settings.getColor(PlotterSettings.PEN_DOWN_COLOR_DEFAULT),this));

		addToPanel(interior1,zMotorType          = new SelectOneOfMany("zMotorType",Translator.get("PlotterSettings.zMotorType"),new String[]{
				Translator.get("PlotterSettings.zMotorType.servo"),  // PlotterSettings.Z_MOTOR_TYPE_SERVO = 1
				Translator.get("PlotterSettings.zMotorType.stepper"),  // PlotterSettings.Z_MOTOR_TYPE_STEPPER = 2
		},settings.getInteger(PlotterSettings.Z_MOTOR_TYPE)-1));

		addToPanel(interior2,blockBufferSize     = new SelectInteger("blockBufferSize",     Translator.get("PlotterSettings.blockBufferSize"     ),settings.getInteger(PlotterSettings.BLOCK_BUFFER_SIZE)));
		addToPanel(interior2,segmentsPerSecond   = new SelectInteger("segmentsPerSecond",   Translator.get("PlotterSettings.segmentsPerSecond"   ),settings.getInteger(PlotterSettings.SEGMENTS_PER_SECOND)));
		addToPanel(interior2,minSegmentLength    = new SelectDouble ("minSegmentLength",    Translator.get("PlotterSettings.minSegmentLength"    ),settings.getDouble(PlotterSettings.MIN_SEGMENT_LENGTH)));
		addToPanel(interior2,minSegTime          = new SelectInteger("minSegTime",          Translator.get("PlotterSettings.minSegTime"          ),settings.getInteger(PlotterSettings.MIN_SEG_TIME)));
		addToPanel(interior2,handleSmallSegments = new SelectBoolean("handleSmallSegments", Translator.get("PlotterSettings.handleSmallSegments" ),settings.getBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS)));
		addToPanel(interior2,minAcceleration     = new SelectDouble ("minAcceleration",     Translator.get("PlotterSettings.minAcceleration"     ),settings.getDouble(PlotterSettings.MIN_ACCELERATION)));
		addToPanel(interior2,minPlannerSpeed     = new SelectDouble ("minPlannerSpeed",     Translator.get("PlotterSettings.minimumPlannerSpeed" ),settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED)));

		if(!settings.isMostAncestral()) {
			bottom.add(buttonSave);
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

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabEssential"),interior0);
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabPen"),interior1);
		tabbedPane.addTab(Translator.get("PlotterSettingsPanel.TabSimulation"),interior2);
		tabbedPane.addTab(Translator.get("PlotterSettingsUserGcodePanel.Title"),userGcodePanel);

		// now assemble the dialog
		this.add(tabbedPane,BorderLayout.CENTER);
		this.add(bottom,BorderLayout.SOUTH);
		this.repaint();

		visualStyle.addSelectListener(e->updateSizeEditable());
		updateSizeEditable();
	}

	private void updateSizeEditable() {
		var isCustom = !visualStyle.getSelectedItem().equals(PlotterRendererFactory.MAKELANGELO_CUSTOM.name());
		var isAncestral = !settings.isMostAncestral();
		var matches = isCustom | isAncestral;
		System.out.println("updateSizeEditable matches="+matches+" isCustom="+isCustom+" isAncestral="+isAncestral);
		machineWidth.setReadOnly(matches);
		machineHeight.setReadOnly(matches);
	}

	private void addToPanel(SelectPanel panel, Select element) {
		panel.add(element);
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

		userGcodePanel.save();
		
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
		settings.setInteger(PlotterSettings.Z_MOTOR_TYPE,zMotorType.getSelectedIndex()+1);

		List<String> machineStyles = getMachineStyleNames();
		settings.setString(PlotterSettings.STYLE,machineStyles.get(visualStyle.getSelectedIndex()));

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
		userGcodePanel.reset();
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
		listeners.add(PlotterSettingsListener.class, listener);
	}

	public void removeListener(PlotterSettingsListener listener) {
		listeners.remove(PlotterSettingsListener.class, listener);
	}

	private void fireSettingsChangedEvent() {
		for(PlotterSettingsListener listener : listeners.getListeners(PlotterSettingsListener.class)) {
			listener.settingsChangedEvent(settings);
		}
	}
}
