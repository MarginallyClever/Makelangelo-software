package com.marginallyclever.makelangelo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class PanelAdjustTools
extends JPanel
implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061412413141577960L;

	protected MainGUI gui;
	protected MultilingualSupport translator;
	protected MachineConfiguration machineConfiguration;


	public PanelAdjustTools(MainGUI _gui, MultilingualSupport _translator, MachineConfiguration _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

	}
	

    public void actionPerformed(ActionEvent e) {
//      Object subject = e.getSource();
    }
    
    void save() {
    	
    }
}
