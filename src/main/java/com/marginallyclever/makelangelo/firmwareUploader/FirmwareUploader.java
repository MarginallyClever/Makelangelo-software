package com.marginallyclever.makelangelo.firmwareUploader;

import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;


public class FirmwareUploader {

	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);

	private String avrdudePath = "";

	public FirmwareUploader() {
		String OS = System.getProperty("os.name").toLowerCase();
		String name = (OS.indexOf("win") >= 0) ? "avrdude.exe": "avrdude";
		
		// if Arduino is not installed in the default windows location, offer the current working directory (fingers crossed)
		File f = new File(name);
		if(f.exists()) {
			avrdudePath = f.getAbsolutePath();
			return;
		}
		
		// arduinoPath
		f = new File("C:\\Program Files (x86)\\Arduino\\hardware\\tools\\avr\\bin\\"+name);
		if(f.exists()) {
			avrdudePath = f.getAbsolutePath();
			return;
		} 
		
		f = new File(FileAccess.getUserDirectory() + File.separator+name);
		if(f.exists()) {
			avrdudePath = f.getAbsolutePath();
		}
	}
	
	public void run(String hexPath,String portName) throws Exception {
		logger.debug("update started");
		
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {}", (p.resolve("../avrdude.conf").toString()));
		File f = p.resolve("../avrdude.conf").toFile();
		if(!f.exists()) {
			logger.debug("Trying 2 {}", (p.resolve("../../etc/avrdude.conf").toString()));
			f = p.resolve("../../etc/avrdude.conf").toFile();
			if(!f.exists()) {
				throw new Exception("Cannot find nearby avrdude.conf");
			}
		}
		
		String confPath = f.getAbsolutePath();
		
		String [] options = new String[]{
				avrdudePath,
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

	private void runCommand(String[] cmd) throws Exception {
		Process p = Runtime.getRuntime().exec(cmd);
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
				if((s = stdError.read()) != -1) System.out.print((char)s);
				else errorOpen=false;
			}
			if(stdInput.ready()) {
				if((s = stdInput.read()) != -1) System.out.print((char)s);
				else inputOpen=false;
			}
		} while(errorOpen && inputOpen);
	}
	
	private void runBufferedReaders(Process p) throws IOException {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		String s = null;

		logger.debug("update: errors (if any)\n");
		while ((s = stdError.readLine()) != null)
			logger.debug("update: {}", s);

		logger.debug("command out:\n");
		while ((s = stdInput.readLine()) != null)
			logger.debug("update: {}", s);
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
