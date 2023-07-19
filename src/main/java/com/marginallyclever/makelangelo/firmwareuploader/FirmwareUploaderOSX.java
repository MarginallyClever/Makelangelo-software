package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FirmwareUploaderOSX extends FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderOSX.class);

	public FirmwareUploaderOSX() {
		super();
		if(!OSHelper.isOSX()) throw new RuntimeException("This class is for OSX only.");
		avrdudePath = "avrdude";
	}

	// TEST
	
	public static void main(String[] args) throws Exception {
		FirmwareUploaderOSX uploader = new FirmwareUploaderOSX();
		uploader.run("/dev/ttyACM0");
	}
}
