package com.marginallyclever.makelangelo.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;

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
	protected JPasswordField passwordField;
	protected JTextField nameField;
	protected boolean isRegistered=false;
	protected JButton createNow;
	

	public PanelRegister(Makelangelo _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

		// @TODO check is registered here
		if(isRegistered==true) {
			displayExistingRegistration();
		} else {
			displayRegisterNow();
		}
	}

	
	protected void displayExistingRegistration() {
		
	}
	
	
	protected void displayRegisterNow() {
	    setLayout(new GridBagLayout());
	    GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;
		
	    String html = translator.get("PleaseRegister");
	    JTextComponent decoratedText = gui.createHyperlinkListenableJEditorPane(html);
	    con1.gridwidth=2;
	    this.add(decoratedText,con1);
	    con1.gridwidth=1;
	    con1.gridy++;

	    createNow = new JButton(translator.get("PleaseRegisterCreateAccountButton")); 
	    createNow.addActionListener(this);
	    con1.gridwidth=2;
	    this.add(createNow,con1);
	    con1.gridwidth=1;
	    con1.gridy++;
	    con1.gridy++;

	    this.add(new JLabel(translator.get("PleaseRegisterUsername")),con1);
	    con1.gridx++;
	    nameField = new JTextField();
	    nameField.addActionListener(this);
	    this.add(nameField,con1);
	    con1.gridy++;
	    con1.gridx=0;

	    this.add(new JLabel(translator.get("PleaseRegisterPassword")),con1);
	    con1.gridx++;
	    passwordField = new JPasswordField();
	    passwordField.addActionListener(this);
	    this.add(passwordField,con1);
	    con1.gridy++;
	    con1.gridy++;
	    con1.gridx=0;
	    
	    con1.weighty=1;
	    this.add(new JLabel(),con1);
	}

    public void actionPerformed(ActionEvent e) {
    	Object subject = e.getSource();
    	
    	if(subject == createNow) {
    		URI uri;
			try {
				uri = new URI("https://www.marginallyclever.com/shop/index.php?main_page=login");
	    		java.awt.Desktop.getDesktop().browse(uri);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    }
}
