package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);

	private final String AVRDUDE_EXE;
	private String avrdudePath = "";

	public FirmwareUploader() {
		String OS = System.getProperty("os.name").toLowerCase();
		AVRDUDE_EXE = (OS.indexOf("win") >= 0) ? "avrdude.exe" : "avrdude";

		findAVRDude();
	}

	private void findAVRDude() {
		// if Arduino is not installed in the default windows location, offer the current working directory (fingers crossed)
		if(attemptFindAVRDude(AVRDUDE_EXE)) return;

		String OS = System.getProperty("os.name").toLowerCase();
		if(OS.indexOf("win") >= 0) {
			// arduinoPath
			if(attemptFindAVRDude("C:\\Program Files (x86)\\Arduino\\hardware\\tools\\avr\\bin\\" + AVRDUDE_EXE)) return;
		}

		if(attemptFindAVRDude(FileAccess.getWorkingDirectory() + File.separator+AVRDUDE_EXE)) return;
		attemptFindAVRDude(FileAccess.getWorkingDirectory() + File.separator + "app" + File.separator + AVRDUDE_EXE);
	}

	private boolean attemptFindAVRDude(String path) {
		File f = new File(path);
		logger.debug("searching for avrdude in {}",f.getAbsolutePath());
		if(f.exists()) {
			avrdudePath = f.getAbsolutePath();
			return true;
		}
		return false;
	}

	private File attempt(int i,String filename) {
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {} {}",i, p.resolve(filename));
		return p.resolve(filename).toFile();
	}
	
	public void run(String hexPath,String portName) throws Exception {
		logger.debug("update started");

		int i=0;
		File f = attempt(i++, "avrdude.conf");
		if(!f.exists()) f = attempt(i++, ".."+File.separator+"avrdude.conf");
		if(!f.exists()) f = attempt(i++, ".."+File.separator+".."+File.separator+"etc"+File.separator+"avrdude.conf");
		if(!f.exists()) f = attempt(i++, ".."+File.separator+"etc"+File.separator+"avrdude.conf");
		if(!f.exists()) {
			throw new Exception("Cannot find nearby avrdude.conf");
		}
		
		String confPath = f.getAbsolutePath();
		String path = avrdudePath;
		if(!path.endsWith(File.separator)) path+=File.separator;
		
		String [] options = new String[]{
				path+AVRDUDE_EXE,
	    		"-C"+confPath,
	    		//"-v","-v","-v","-v",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D","-Uflash:w:"+hexPath+":i"
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
		//runStreamReaders(p);
		runBufferedReaders(p);
	}

	@SuppressWarnings("unused")
	private void runStreamReaders(Process p) throws IOException {
		InputStreamReader stdInput = new InputStreamReader(p.getInputStream());
		InputStreamReader stdError = new InputStreamReader(p.getErrorStream());

		System.out.println("errors (if any):\n");
		boolean errorOpen=true;
		boolean inputOpen=true;
		int s;
		do {
			if(stdError.ready()) {
				if((s = stdError.read()) <=0) System.out.print("error: "+(char)s);
			}
			if(stdInput.ready()) {
				if((s = stdInput.read()) <=0) System.out.print("input: "+(char)s);
			}
		} while(p.isAlive());
	}
	
	private void runBufferedReaders(Process p) throws IOException {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		String s = null;

		while(p.isAlive()) {
			while ((s = stdError.readLine()) != null) {
				logger.debug("error: {}", s);
				System.out.println("error: " + s);
			}

			while ((s = stdInput.readLine()) != null) {
				logger.debug("output: {}", s);
				System.out.println("output: " + s);
			}
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
		fu.run("./firmware.hex", "COM3");
	}
}
