package com.marginallyclever.makelangelo.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;import com.marginallyclever.makelangelo.Translator;

public class PanelAdjustTools
extends JPanel
implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061412413141577960L;

	protected Makelangelo gui;
	protected Translator translator;
	protected MakelangeloRobot robot;
	protected ArrayList<JButton> buttons = new ArrayList<JButton>();

	public PanelAdjustTools(Makelangelo _gui, Translator _translator, MakelangeloRobot robot) {
		gui = _gui;
		translator = _translator;
		this.robot = robot;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;
		
		buttons.clear();
		
    	String [] names = robot.settings.getToolNames();
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
    			robot.settings.getTool(i).adjust();
    			return;
    		}
    	}
    }
    
    void save() {}
}
