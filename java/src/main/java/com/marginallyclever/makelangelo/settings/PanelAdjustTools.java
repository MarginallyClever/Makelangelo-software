package com.marginallyclever.makelangelo.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;

public class PanelAdjustTools
extends JPanel
implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061412413141577960L;

	protected Makelangelo gui;
	protected MultilingualSupport translator;
	protected MakelangeloRobot machineConfiguration;
	protected ArrayList<JButton> buttons = new ArrayList<JButton>();

	public PanelAdjustTools(Makelangelo _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;
		
		buttons.clear();
		
    	String [] names = machineConfiguration.getToolNames();
    	for(int i=0;i<names.length;++i) {
    		JButton b = new JButton(names[i]);
    		b.addActionListener(this);
    		buttons.add(b);
    		this.add(b,con1);
    		con1.gridy++;
    	}
	}
	

    public void actionPerformed(ActionEvent e) {
    	Object subject = e.getSource();
    	for(int i=0;i<buttons.size();++i) {
    		if(subject == buttons.get(i)) {
    			machineConfiguration.getTool(i).adjust();
    			return;
    		}
    	}
    }
    
    void save() {}
}
