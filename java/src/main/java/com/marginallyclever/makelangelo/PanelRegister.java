package com.marginallyclever.makelangelo;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

public class PanelRegister
extends JPanel
implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -785395804156632991L;
	
	protected Makelangelo gui;
	protected MultilingualSupport translator;
	protected MakelangeloRobot machineConfiguration;


	public PanelRegister(Makelangelo _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

	    setLayout(new FlowLayout());
	    String html = _translator.get("PleaseRegister");
	    JTextComponent decoratedText = _gui.createHyperlinkListenableJEditorPane(html);
	    this.add(decoratedText);
	}

    public void actionPerformed(ActionEvent e) {
      //Object subject = e.getSource();
    }
}
