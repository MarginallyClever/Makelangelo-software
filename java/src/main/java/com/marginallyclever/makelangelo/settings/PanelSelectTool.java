package com.marginallyclever.makelangelo.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;


public class PanelSelectTool
extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8256498380663422463L;
	protected Makelangelo gui;
	protected Translator translator;
	protected MakelangeloRobot robot;

	protected JComboBox<String> toolCombo;

	
	public PanelSelectTool(Makelangelo _gui, Translator _translator, MakelangeloRobot robot) {
		gui = _gui;
		translator = _translator;
		this.robot = robot;

	    this.setLayout(new GridBagLayout());

	   	toolCombo = new JComboBox<String>(robot.settings.getToolNames());
	    toolCombo.setSelectedIndex(robot.settings.getCurrentToolNumber());

	    GridBagConstraints c = new GridBagConstraints();
	    c.gridx = 0;
	    c.gridy = 1;
	    c.gridwidth = 2;
	    c.gridheight = 1;
	    this.add(new JLabel(translator.get("ToolType")), c);
	    c.anchor = GridBagConstraints.WEST;
	    c.gridx = 2;
	    c.gridy = 1;
	    c.gridwidth = 2;
	    c.gridheight = 1;
	    this.add(toolCombo, c);

	    this.setVisible(true);
	}
	

    void save() {
    	robot.settings.setCurrentToolNumber(toolCombo.getSelectedIndex());
    }
}
