package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);
	private final String AVRDUDE_APP;
	private String avrdudePath = "";
	private String confPath;

	public FirmwareUploader() {
		if(isWindows()) {
			AVRDUDE_APP = "avrdude.exe";
		} else {
			AVRDUDE_APP = "avrdude";
		}
	}

	public boolean hasFoundAVRdude() {
		boolean found = isWindows() ? findAVRDudeWindows() : findAVRDudeOther();
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

	private boolean isWindows() {
		String OS = System.getProperty("os.name").toLowerCase();
		return OS.contains("win");
	}

	private boolean findAVRDudeWindows() {
		// if Arduino is not installed in the default windows location, offer the current working directory (fingers crossed)
		if(attemptFindAVRDude(AVRDUDE_APP)) return true;
		if(attemptFindAVRDude("C:\\Program Files\\Makelangelo\\app\\"+AVRDUDE_APP)) return true;
		if(attemptFindAVRDude("C:\\Program Files (x86)\\Arduino\\hardware\\tools\\avr\\bin\\"+AVRDUDE_APP)) return true;
		if(attemptFindAVRDude(FileAccess.getWorkingDirectory() + File.separator+AVRDUDE_APP)) return true;
		return attemptFindAVRDude(FileAccess.getWorkingDirectory() + File.separator + "app" + File.separator+AVRDUDE_APP);
	}

	private boolean findAVRDudeOther() {
		try {
			Process process = new ProcessBuilder("which", "avrdude").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String path;
			while((path = reader.readLine()) != null) {
				logger.debug("which: {}", path);
				if(attemptFindAVRDude(path)) return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean attemptFindAVRDude(String path) {
		File f = new File(path);

		logger.debug("Searching for avrdude in {}",f.getAbsolutePath());
		if(f.exists()) {
			avrdudePath = f.getParent() + File.separator;
			return true;
		}
		return false;
	}

	// find avrdude.conf
	private boolean findConf() {
		int i=0;
		File f = attemptToFindConf(i++, "avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+"avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+".."+File.separator+"etc"+File.separator+"avrdude.conf");
		if(!f.exists()) f = attemptToFindConf(i++, ".."+File.separator+"etc"+File.separator+"avrdude.conf");

		if(!f.exists()) return false;
		confPath = f.getAbsolutePath();
		return true;
	}

	private File attemptToFindConf(int i, String filename) {
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {} {}",i, p.resolve(filename));
		return p.resolve(filename).toFile();
	}
	
	public void run(String hexPath,String portName) throws Exception {
		logger.debug("update started");

		// setup avrdude command
		String path;
		if(isWindows()) {
			path = avrdudePath;
			if(!path.endsWith(File.separator)) path+=File.separator;
			path += AVRDUDE_APP;
		} else {
			path = "/bin/bash -c "+ AVRDUDE_APP;
		}
		
		String [] options = new String[]{
				path,
	    		"-C"+confPath,
	    		//"-v","-v","-v","-v",
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

	private void runCommand(String[] options) throws Exception {
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

	private void runBufferedReaders(Process p) throws IOException {
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
	
	public String getAvrdudePath() {
		return avrdudePath;
	}

	public void setAvrdudePath(String avrdudePath) {
		this.avrdudePath = avrdudePath;
	}
	
	// TEST
	
	public void main(String[] args) throws Exception {
		FirmwareUploader fu = new FirmwareUploader();
		fu.run("./firmware-m5.hex", "COM3");
	}
}
