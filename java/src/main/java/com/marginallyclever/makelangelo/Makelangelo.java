package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Makelangelo  {

	private static final Logger LOGGER = LoggerFactory.getLogger(Makelangelo.class);

	public static MainGUI gui;
	
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	gui = new MainGUI();
	        }
	    });
		LOGGER.info("Called javax.swing.SwingUtilities#invokeLater using com.marginallyclever.makelangelo.MainGUI#getSingleton. Unless something went terribly wrong, GUI is created and shown.");
	}
}
