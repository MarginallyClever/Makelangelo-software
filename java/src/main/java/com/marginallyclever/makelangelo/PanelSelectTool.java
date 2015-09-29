package com.marginallyclever.makelangelo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class PanelSelectTool
extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8256498380663422463L;
	protected MainGUI gui;
	protected MultilingualSupport translator;
	protected MakelangeloRobot machineConfiguration;

	protected JComboBox<String> toolCombo;

	
	public PanelSelectTool(MainGUI _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

	    this.setLayout(new GridBagLayout());

	   	toolCombo = new JComboBox<String>(machineConfiguration.getToolNames());
	    toolCombo.setSelectedIndex(machineConfiguration.getCurrentToolNumber());

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
        machineConfiguration.setCurrentToolNumber(toolCombo.getSelectedIndex());
    }
}
