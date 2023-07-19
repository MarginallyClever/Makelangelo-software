package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * Common methods for uploading firmware to an AVR microcontroller.
 */
public class FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);
	protected String avrdudePath = "";
	protected String hexPath = "";
	protected String confPath = "";

	protected FirmwareUploader() {
		super();
	}

	// find avrdude.conf
	public boolean findConf() {
		logger.info("Searching for conf starting in "+avrdudePath);
		int i=0;
		File f = attemptToFindConf(i++, ".."+File.separator+"etc"+File.separator+"avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, "avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+"avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+".."+File.separator+"etc"+File.separator+"avrdude.conf");
		if(!f.exists()) return false;
		confPath = f.getAbsolutePath();
		return true;
	}

	protected File attemptToFindConf(int i, String filename) {
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {} {}",i, p.resolve(filename));
		return p.resolve(filename).toFile();
	}

	public void run(String portName) throws Exception {
		logger.debug("update started");

		// setup avrdude command
		String path = avrdudePath + "avrdude";
		if( OSHelper.isWindows()) path+=".exe";

		String [] options = new String[] {
				path,
	    		"-C"+confPath,
	    		"-v","-V",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D",
				"-Uflash:w:"+hexPath+":i"
		    };
	    runCommand(options);
		logger.debug("update finished");
	}

	protected void runCommand(String[] options) throws Exception {
		System.out.println("running command: "+String.join(" ",options));
		logger.debug("running command: {}",String.join(" ",options));
/*
		List<String> command = new ArrayList<>();
		for (String option : options) {
			command.add("\"" + option.replace("\\", "\\\\") + "\"");
		}*/

		ProcessBuilder builder = new ProcessBuilder(options);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		runBufferedReaders(p);
	}

	protected void runBufferedReaders(Process p) throws IOException {
		InputStreamReader stdInput = new InputStreamReader(p.getInputStream());
		InputStreamReader stdError = new InputStreamReader(p.getErrorStream());

		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		while (p.isAlive()) {
			readByte(stdInput,sb1,"output");
			readByte(stdError,sb2,"error");
		}
	}

	private void readByte(InputStreamReader isr,StringBuilder sb,String label) throws IOException {
		int c = isr.read();
		if(c == -1) return;
		System.out.print((char) c);

		if (c != '\n') {
			sb.append((char) c);
		} else {
			String s = sb.toString();
			sb.delete(0, sb.length());
			logger.debug("{}: {}", label, s);
		}
	}

	public void setHexPath(String s) {
		hexPath = s;
	}

    public void setAVRDude(String avrDudePath) {
		avrdudePath = avrDudePath;
    }
}
