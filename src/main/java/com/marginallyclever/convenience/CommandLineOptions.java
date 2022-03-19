package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store command line options for use in the app
 * @author Dan Royer
 *
 */
public class CommandLineOptions {

	private static final Logger logger = LoggerFactory.getLogger(CommandLineOptions.class);
	protected static String [] args;
	
	static public void setFromMain(String [] args) {
		CommandLineOptions.args = args;

		if (logger.isDebugEnabled()) {
			for (String arg : args) {
				logger.debug("START OPTION {}", arg);
			}
		}
	}
	
	static public boolean hasOption(String option) {
		for (String arg : args) {
			if (arg.equals(option)) {
				return true;
			}
		}
		return false;
	}
}
