package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FirmwareUploaderWindows extends FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderWindows.class);

	public FirmwareUploaderWindows() {
		super();
		if(!OSHelper.isWindows()) throw new RuntimeException("This class is for Windows only.");
		avrdudePath = "avrdude.exe";
	}

	// TEST
	
	public static void main(String[] args) throws Exception {
		FirmwareUploaderWindows uploader = new FirmwareUploaderWindows();
		uploader.setHexPath("./firmware-m5.hex");
		uploader.run("COM3");
	}
}
