package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;


@Deprecated
public class PenSettingsPanel extends SelectPanel {
	private static final long serialVersionUID = 1L;

	protected Plotter robot;
	
	protected SelectDouble penDiameter;
	protected SelectDouble maxFeedRate;
	protected SelectDouble currentFeedRate;
	protected SelectDouble penUp;
	protected SelectDouble penDown;
	protected SelectDouble penZRate;

	protected SelectButton buttonSave;
	protected SelectButton buttonCancel;
	
	protected SelectColor selectPenDownColor;
	protected SelectColor selectPenUpColor;

	
	public PenSettingsPanel(Plotter robot) {
		super();
		
		this.robot = robot;
	    
	    PlotterSettings settings = robot.getSettings();
	    
	    add(penDiameter = new SelectDouble("diameter",Translator.get("penToolDiameter"),settings.getPenDiameter()));
	    add(maxFeedRate = new SelectDouble("feedrate",Translator.get("penToolMaxFeedRate"),settings.getTravelFeedRate()));
	    add(currentFeedRate = new SelectDouble("speed",Translator.get("Speed"),settings.getDrawFeedRate()));
	    add(penZRate = new SelectDouble("liftSpeed",Translator.get("penToolLiftSpeed"),settings.getPenLiftTime()));
	    add(penUp = new SelectDouble("up",Translator.get("penToolUp"),settings.getPenUpAngle()));
	    //add(buttonTestUp = new SelectButton("testUp",Translator.get("penToolTest")));
	    add(penDown = new SelectDouble("down",Translator.get("penToolDown"),settings.getPenDownAngle()));
	    //add(buttonTestDown = new SelectButton("testDown",Translator.get("penToolTest")));
		add(selectPenDownColor = new SelectColor("colorDown",Translator.get("pen down color"),robot.getSettings().getPenDownColor(),this));
		add(selectPenUpColor = new SelectColor("colorUp",Translator.get("pen up color"),robot.getSettings().getPenUpColor(),this));
	}
	
	/*
	public void actionPerformed(ActionEvent event) {
		Object subject = event.getSource();

		if (subject == buttonTestUp  ) {
			// must match MakelangeloRobotSettings.getPenUpString()
			robot.send(
				"G00 F" + penZRate.getValue() + " Z" + penUp.getValue() + ";\n"+
				"G00 F" + maxFeedRate.getValue() + ";\n"
				);
		}
		if (subject == buttonTestDown) {
			// must match MakelangeloRobotSettings.getPenDownString()
			robot.send(
					"G01 F" + penZRate.getValue() + " Z" + penDown.getValue() + ";\n"+
					"G01 F" + currentFeedRate.getValue() + ";\n"
					);
		}
	}*/
	
	public void save() {
	    PlotterSettings settings = robot.getSettings();
		settings.setPenDiameter(penDiameter.getValue());
		settings.setTravelFeedRate(maxFeedRate.getValue());
		settings.setDrawFeedRate(currentFeedRate.getValue());
		settings.setPenLiftTime(penZRate.getValue());
		settings.setPenUpAngle(penUp.getValue());
		settings.setPenDownAngle(penDown.getValue());
		settings.setPenDownColor(selectPenDownColor.getColor());
		settings.setPenDownColorDefault(selectPenDownColor.getColor());
		settings.setPenUpColor(selectPenUpColor.getColor());
	}
	
	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(PenSettingsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PenSettingsPanel(new Plotter()));
		frame.pack();
		frame.setVisible(true);
	}
}
