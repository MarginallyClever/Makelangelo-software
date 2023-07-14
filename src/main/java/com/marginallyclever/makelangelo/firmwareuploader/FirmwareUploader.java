package com.marginallyclever.makelangelo.firmwareuploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Common methods for uploading firmware to an AVR microcontroller.
 */
public abstract class FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);
	protected String AVRDUDE_APP = "";
	protected String avrdudePath = "";
	protected String hexPath = "";
	protected String confPath = "";

	protected FirmwareUploader() {
		super();
	}

	abstract public boolean findAVRDude();

	public boolean hasFoundAVRdude() {
		boolean found = findAVRDude();
		if(!found) {
			logger.error("Cannot find avrdude");
			return false;
		}
		if(!findConf()) {
			logger.error("Cannot find avrdude.conf");
			return false;
		}
		return true;
	}

	// find avrdude.conf
	abstract boolean findConf();

	protected File attemptToFindConf(int i, String filename) {
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {} {}",i, p.resolve(filename));
		return p.resolve(filename).toFile();
	}

	protected boolean attemptFindAVRDude(String path) {
		File f = new File(path);

		logger.debug("Searching for avrdude in {}",f.getAbsolutePath());
		if(f.exists()) {
			avrdudePath = f.getParent() + File.separator;
			return true;
		}
		return false;
	}

	abstract public String getCommand();

	public void run(String portName) throws Exception {
		logger.debug("update started");

		// setup avrdude command
		String path = getCommand();

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
		logger.debug("running command: {}",String.join(" ",options));

		List<String> command = new ArrayList<>();
		for (String option : options) {
			command.add("\"" + option.replace("\\", "\\\\") + "\"");
		}

		ProcessBuilder builder = new ProcessBuilder(command);
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
	
	String getAVRDudePath() {
		return avrdudePath;
	}

	void setAvrdudePath(String avrdudePath) {
		this.avrdudePath = avrdudePath;
	}

	public void setHexPath(String s) {
		hexPath = s;
	}

	public String getHexPath() {
		return hexPath;
	}
}
