package com.marginallyclever.makelangelo;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanelRegister
extends JPanel
implements ActionListener {

	protected Makelangelo gui;
	protected MultilingualSupport translator;
	protected MakelangeloRobot machineConfiguration;


	public PanelRegister(Makelangelo _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

	    setLayout(new FlowLayout());
	
	    this.add(new JLabel(_translator.get("PleaseRegister")));
	}

    public void actionPerformed(ActionEvent e) {
      //Object subject = e.getSource();
    }
}
