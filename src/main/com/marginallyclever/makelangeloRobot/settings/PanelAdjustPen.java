package com.marginallyclever.makelangeloRobot.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;


public class PanelAdjustPen extends SelectPanel implements ActionListener {
	protected MakelangeloRobot robot;
	
	protected SelectFloat penDiameter;
	protected SelectFloat maxFeedRate;
	protected SelectFloat currentFeedRate;
	protected SelectFloat penUp;
	protected SelectFloat penDown;
	protected SelectFloat penZRate;

	protected SelectButton buttonTestUp;
	protected SelectButton buttonTestDown;
	protected SelectButton buttonSave;
	protected SelectButton buttonCancel;
	
	protected SelectColor selectPenDownColor;
	protected SelectColor selectPenUpColor;

	
	public PanelAdjustPen(MakelangeloRobot robot) {
		super();
		
		this.robot = robot;
	    
	    MakelangeloRobotSettings settings = robot.getSettings();
	    
	    add(penDiameter = new SelectFloat(Translator.get("penToolDiameter"),settings.getPenDiameter()));
	    add(maxFeedRate = new SelectFloat(Translator.get("penToolMaxFeedRate"),settings.getPenUpFeedRate()));
	    add(currentFeedRate = new SelectFloat(Translator.get("Speed"),settings.getPenDownFeedRate()));
	    add(penZRate = new SelectFloat(Translator.get("penToolLiftSpeed"),settings.getZRate()));
	    add(penUp = new SelectFloat(Translator.get("penToolUp"),settings.getPenUpAngle()));
	    add(buttonTestUp = new SelectButton(Translator.get("penToolTest")));
	    add(penDown = new SelectFloat(Translator.get("penToolDown"),settings.getPenDownAngle()));
	    add(buttonTestDown = new SelectButton(Translator.get("penToolTest")));
		add(selectPenDownColor = new SelectColor(panel,Translator.get("pen down color"),robot.getSettings().getPenDownColor()));
		add(selectPenUpColor = new SelectColor(panel,Translator.get("pen up color"),robot.getSettings().getPenUpColor()));
		finish();
	}
	
	
	public void actionPerformed(ActionEvent event) {
		Object subject = event.getSource();

		if (subject == buttonTestUp  ) {
			// must match MakelangeloRobotSettings.getPenUpString()
			robot.sendLineToRobot(
				"G00 F" + penZRate.getValue() + " Z" + penUp.getValue() + ";\n"+
				"G00 F" + maxFeedRate.getValue() + ";\n"
				);
		}
		if (subject == buttonTestDown) {
			// must match MakelangeloRobotSettings.getPenDownString()
			robot.sendLineToRobot(
					"G01 F" + penZRate.getValue() + " Z" + penDown.getValue() + ";\n"+
					"G01 F" + currentFeedRate.getValue() + ";\n"
					);
		}
	}
	
	
	public void save() {
	    MakelangeloRobotSettings settings = robot.getSettings();
		settings.setDiameter(penDiameter.getValue());
		settings.setMaxFeedRate(maxFeedRate.getValue());
		settings.setCurrentFeedRate(currentFeedRate.getValue());
		settings.setZRate(penZRate.getValue());
		settings.setPenUpAngle(penUp.getValue());
		settings.setPenDownAngle(penDown.getValue());
		settings.setPenDownColor(selectPenDownColor.getColor());
		settings.setPenDownColorDefault(selectPenDownColor.getColor());
		settings.setPenUpColor(selectPenUpColor.getColor());
	}
}
