package com.marginallyclever.makelangelo.uploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.marginallyclever.convenience.log.Log;


public class FirmwareUploader {
	public String arduinoPath = "C:/Program Files (x86)/Arduino/hardware/tools/avr/";
	public String avrdudePath = "bin/avrdude";
	public String confPath = "etc/avrdude.conf";
	
	public FirmwareUploader() {
		// if Arduino is not installed in the default windows location, offer the current working directory (fingers crossed)
		File f = new File(arduinoPath);
		if(!f.exists()) arduinoPath = System.getProperty("user.dir");
	}
	
	public void run(String hexPath,String portName) throws Exception {
		Log.message("update started");
		
		String [] options = new String[]{
	    		arduinoPath+avrdudePath,
	    		"-C"+arduinoPath+confPath,
	    		//"-v","-v","-v","-v",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D","-Uflash:w:"+hexPath+":i"
		    }; 
	    runCommand(options);

		Log.message("update finished");
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

		Log.message("update: errors (if any)\n");
		while ((s = stdError.readLine()) != null)
			Log.message("update: "+s);

		System.out.println("command out:\n");
		while ((s = stdInput.readLine()) != null)
			Log.message("update: "+s);		
	}
	
	// test
	public void main(String[] args) {
		Log.start();
		FirmwareUploader fu = new FirmwareUploader();
		try {
			fu.run("./firmware.hex", "COM3");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
