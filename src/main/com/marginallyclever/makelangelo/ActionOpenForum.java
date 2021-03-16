package com.marginallyclever.makelangelo;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.AbstractAction;

import com.marginallyclever.core.Translator;

/**
 * opens a link to Discord
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class ActionOpenForum extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String FORUM_URL = "https://discord.gg/Q5TZFmB";
	
	
	public ActionOpenForum() {
		super(Translator.get("MenuForums"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			java.awt.Desktop.getDesktop().browse(URI.create(FORUM_URL));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
