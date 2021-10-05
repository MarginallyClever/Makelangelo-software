package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.log.Log;

/**
 * Store command line options for use in the app
 * @author Dan Royer
 *
 */
public class CommandLineOptions {
	protected static String [] args;
	
	static public void setFromMain(String [] args) {
		CommandLineOptions.args = args;

		for(int i=0;i<args.length;++i) {
			String msg = "START OPTION "+args[i];
			Log.message(msg);
		}
	}
	
	static public boolean hasOption(String option) {
		for(int i=0;i<args.length;++i) {
			if(args[i].equals(option)) {
				return true;
			}
		}
		return false;
	}
}
