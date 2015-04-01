package com.marginallyclever.makelangelo;

public class Makelangelo {
	public static MainGUI gui;
	
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	/*
	        	String OS = System.getProperty("os.name").toLowerCase();
	            String workingDirectory=System.getProperty("user.dir");
	            System.out.println(workingDirectory);
	            
	            System.out.println(OS);
	            // is this Windows?
	            if(OS.indexOf("win") >= 0) {
	            	// is 64 bit?
	            	if(System.getenv("ProgramFiles(x86)") != null) {
	            		// 64 bit
	            		System.load(workingDirectory+"/64/rxtxSerial.dll");
	            	} else {
	            		// 32 bit
	            		System.load(workingDirectory+"/32/rxtxSerial.dll");
	            	}
	            } else {
	            	// is this OSX?
	    	        if(OS.indexOf("mac") >= 0) {
	    	    		System.load(workingDirectory+"/librxtxSerial.jnilib");
	    	        }
	            }
	    		*/
	        	gui = MainGUI.getSingleton();
	        }
	    });
	}
}
