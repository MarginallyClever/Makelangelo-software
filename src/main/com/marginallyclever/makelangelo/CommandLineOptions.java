package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.log.Log;

/**
 * Store command line options for use in the app
 * @author Dan Royer
 *
 */
public class CommandLineOptions {
	protected static String [] argv;
	
	static public void setFromMain(String [] argv) {
		CommandLineOptions.argv = argv;

		for(int i=0;i<argv.length;++i) {
			String msg = "START OPTION "+argv[i];
			Log.message(msg);
		}
	}
	
	static public boolean hasOption(String option) {
		for(int i=0;i<argv.length;++i) {
			if(argv[i].equals(option)) {
				return true;
			}
		}
		return false;
	}
}
