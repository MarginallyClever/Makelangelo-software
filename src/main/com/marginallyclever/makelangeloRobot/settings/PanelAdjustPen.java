package com.marginallyclever.makelangeloRobot.settings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangeloRobot.Plotter;


@Deprecated
public class PanelAdjustPen extends SelectPanel/* implements ActionListener*/ {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Plotter robot;
	
	protected SelectDouble penDiameter;
	protected SelectDouble maxFeedRate;
	protected SelectDouble currentFeedRate;
	protected SelectDouble penUp;
	protected SelectDouble penDown;
	protected SelectDouble penZRate;

	//protected SelectButton buttonTestUp;
	//protected SelectButton buttonTestDown;
	protected SelectButton buttonSave;
	protected SelectButton buttonCancel;
	
	protected SelectColor selectPenDownColor;
	protected SelectColor selectPenUpColor;

	
	public PanelAdjustPen(Plotter robot) {
		super();
		
		this.robot = robot;
	    
	    PlotterSettings settings = robot.getSettings();
	    
	    add(penDiameter = new SelectDouble(Translator.get("penToolDiameter"),settings.getPenDiameter()));
	    add(maxFeedRate = new SelectDouble(Translator.get("penToolMaxFeedRate"),settings.getTravelFeedRate()));
	    add(currentFeedRate = new SelectDouble(Translator.get("Speed"),settings.getDrawFeedRate()));
	    add(penZRate = new SelectDouble(Translator.get("penToolLiftSpeed"),settings.getPenLiftTime()));
	    add(penUp = new SelectDouble(Translator.get("penToolUp"),settings.getPenUpAngle()));
	    //add(buttonTestUp = new SelectButton(Translator.get("penToolTest")));
	    add(penDown = new SelectDouble(Translator.get("penToolDown"),settings.getPenDownAngle()));
	    //add(buttonTestDown = new SelectButton(Translator.get("penToolTest")));
		add(selectPenDownColor = new SelectColor(interiorPanel,Translator.get("pen down color"),robot.getSettings().getPenDownColor()));
		add(selectPenUpColor = new SelectColor(interiorPanel,Translator.get("pen up color"),robot.getSettings().getPenUpColor()));
		finish();
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
}
