package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class FirmwareUploaderWindows extends FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderWindows.class);

	public FirmwareUploaderWindows() {
		super();
		if(!OSHelper.isWindows()) throw new RuntimeException("This class is for Windows only.");
		AVRDUDE_APP = "avrdude.exe";
		if((new File("C:\\Program Files\\Makelangelo\\app\\firmware-m5.hex").exists())) {
			logger.info("using hex file from install directory.");
			setHexPath("C:\\Program Files\\Makelangelo\\app\\firmware-m5.hex");
		} else if((new File("src\\main\\package\\firmware-m5.hex").exists())) {
			logger.info("using hex file from source code.");
			setHexPath("src\\main\\package\\firmware-m5.hex");
		} else {
			logger.error("cannot find hex file.");
		}
	}

	public boolean findAVRDude() {
		if(attemptFindAVRDude(AVRDUDE_APP)) return true;
		if(attemptFindAVRDude("C:\\Program Files\\Makelangelo\\app\\"+AVRDUDE_APP)) return true;
		if(attemptFindAVRDude("C:\\Program Files (x86)\\Arduino\\hardware\\tools\\avr\\bin\\"+AVRDUDE_APP)) return true;
		if(attemptFindAVRDude(FileAccess.getWorkingDirectory() + File.separator+AVRDUDE_APP)) return true;
		return attemptFindAVRDude(FileAccess.getWorkingDirectory() + File.separator + "app" + File.separator+AVRDUDE_APP);
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
		String path = avrdudePath;
		if(!path.endsWith(File.separator)) path+=File.separator;
		path += AVRDUDE_APP;
		return path;
	}

	// TEST
	
	public void main(String[] args) throws Exception {
		FirmwareUploaderWindows uploader = new FirmwareUploaderWindows();
		setHexPath("./firmware-m5.hex");
		uploader.run("COM3");
	}
}
