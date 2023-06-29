package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FirmwareUploaderUbuntu extends FirmwareUploader {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderUbuntu.class);

    public FirmwareUploaderUbuntu() {
        super();
        if(OSHelper.isOSX() || OSHelper.isWindows()) {
            throw new RuntimeException("This class is for Ubuntu only.");
        }
        AVRDUDE_APP = "avrdude";
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

    @Override
    boolean findConf() {
        return false;
    }

    @Override
    public String getCommand() {
        return "/bin/bash -c "+ AVRDUDE_APP;
    }
}
