package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmwareUploaderUbuntu extends FirmwareUploader {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderUbuntu.class);

    public FirmwareUploaderUbuntu() {
        super();
        if(OSHelper.isOSX() || OSHelper.isWindows()) {
            throw new RuntimeException("This class is for Ubuntu only.");
        }
        avrdudePath = "avrdude";
    }
}
