package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


public class FirmwareUploaderOSX extends FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderOSX.class);

	public FirmwareUploaderOSX() {
		super();
		if(!OSHelper.isOSX()) throw new RuntimeException("This class is for OSX only.");
		AVRDUDE_APP = "avrdude";

		File f = new File(System.getProperty("jpackage.app-path")+File.pathSeparator+".."+File.pathSeparator+".."+File.pathSeparator+"app");
		if(f.exists()) {
			logger.debug("found hex file.");
			setHexPath(f.getAbsolutePath());
		} else {
			logger.error("cannot find hex file.");
		}
	}

	public boolean findAVRDude() {
		try {
			Process process = new ProcessBuilder("which", "avrdude").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String path;
			int hitCount=0;
			while((path = reader.readLine()) != null) {
				hitCount++;
				logger.debug("which: {}", path);
				if(attemptFindAVRDude(path)) return true;
			}
			logger.debug("which hit {} times.",hitCount);
		} catch (Exception e) {
			logger.error("failed to run `which`: ",e);
		}
		return false;
	}

	// find avrdude.conf
	public boolean findConf() {
		int i=0;
		File f = attemptToFindConf(i++, "avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+"avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+".."+File.separator+"etc"+File.separator+"avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+"etc"+File.separator+"avrdude.conf");

		if(!f.exists()) return false;
		confPath = f.getAbsolutePath();
		return true;
	}

	public String getCommand() {
		return "/bin/bash -c "+ AVRDUDE_APP;
	}

	// TEST
	
	public void main(String[] args) throws Exception {
		FirmwareUploaderOSX uploader = new FirmwareUploaderOSX();
		uploader.run( "/dev/ttyACM0");
	}
}
