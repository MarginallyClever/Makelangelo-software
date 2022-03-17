/*
 */
package com.marginallyclever.makelangelo;

import com.marginallyClever.util.PropertiesFileHelper;

/**
 * Utility functions to retrieve Makelangelo Version and detail version. 
 * Should be used only after Translator be initialised.
 */
public class MakelangeloVersion {

	/**
	 * Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update the
	 * VERSION based upon pom.xml.  In this way we only define the VERSION once and prevent violating DRY.
	 */
	public static final String DETAILED_VERSION = PropertiesFileHelper.getMakelangeloGitVersion();
	
	public static final String VERSION = PropertiesFileHelper.getMakelangeloVersion();

	public static String getFullVersionString() {
		return getLiteVersionString() + " (" + DETAILED_VERSION + ")";
	}

	public static String getLiteVersionString() {
		return Translator.get("TitlePrefix") + " " + VERSION;
	}
	
	/**
	 * 
	 * @return the FullVersion if System Env variable DEV is set to true, else the LiteVersion.
	 */
	public static String getFullOrLiteVersionStringRelativeToSysEnvDevValue(){
		if ( "true".equalsIgnoreCase(System.getenv("DEV"))){
			return MakelangeloVersion.getFullVersionString();
		}else{
			return MakelangeloVersion.getLiteVersionString();
		}
	}
}
