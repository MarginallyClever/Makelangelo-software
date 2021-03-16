package com.marginallyclever.makelangelo;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.marginallyclever.core.Translator;

public class ActionAbout extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6110774220426068567L;

	private Frame myFrame;
	private String myVersion;
	
	public ActionAbout(Frame f,String version) {
		super(Translator.get("MenuAbout"));
		myFrame =f;
		myVersion=version;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DialogAbout a = new DialogAbout();
		a.display(myFrame,myVersion);
	}

}
