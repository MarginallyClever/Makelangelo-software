package com.marginallyclever.makelangelo.firmwareUploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.log.Log;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class FirmwareUploader {
	private String avrdudePath = "avrdude";// if this is in the path juste the cmd (linux) (for windows avrdude.exe )

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
		Log.message("update started");
		
		Path p = Path.of(avrdudePath);
		Log.message("Trying "+(p.resolve("../avrdude.conf").toString()));
		File f = p.resolve("../avrdude.conf").toFile();
		if(!f.exists()) {
			Log.message("Trying 2 "+(p.resolve("../../etc/avrdude.conf").toString()));
			f = p.resolve("../../etc/avrdude.conf").toFile();
			if(!f.exists()) {
			    // TODO to reactive the throw juste for demo purpose of process exec.
				//throw new Exception("Cannot find nearby avrdude.conf");
				   System.err.println("Cannot find nearby avrdude.conf ");
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
	    
		//runCommand(options);
		
		//
		// Only for non interactive (no inputs) commande (that terminate ... TODO timer for non terminating commandes).
		//
		System.out.println("(During)Commande exec result : ");
		String fullExecCmdOutputsAsTexte = execBashCommand(options, null);
		// For simple test :  String ouptups =  execBashCommand(new String[]{"ls"}, null);

		System.out.println("");
		System.out.println("(After)Commande exec result : is a succes = " + lastExecSucces);
		System.out.println(fullExecCmdOutputsAsTexte);

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

		Log.message("command out:\n");
		while ((s = stdInput.readLine()) != null)
			Log.message("update: "+s);		
	}
	
	public String getAvrdudePath() {
		return avrdudePath;
	}

	public void setAvrdudePath(String avrdudePath) {
		this.avrdudePath = avrdudePath;
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		FirmwareUploader fu = new FirmwareUploader();
		try {
			fu.run("./firmware.hex", "COM3");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//
	//
	//
	
	public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");

	static boolean debugRuntimeExecPre = true;
	static boolean lastExecSucces = false;

	public static String execBashCommand(String[] cmdArray, StreamGobblerReadLineBufferedSpecial streamGobblerProcessIn) {
	    lastExecSucces = false;
	    final StringBuffer res = new StringBuffer();
	    res.setLength(0);
	    if (debugRuntimeExecPre) {
		System.out.printf("Running : ");
		for (String arg : cmdArray) {
		    System.out.printf("%s ", arg);
		}
		System.out.printf("\n");
	    }
	    res.append("Running : ");
	    for (String arg : cmdArray) {
		res.append(String.format("%s ", arg));
	    }
	    res.append("\n");

	    try {
		Date dStar = new Date();
		Process process;
		ProcessBuilder pb = new ProcessBuilder(cmdArray);
		Map<String, String> env = pb.environment();
		//env.put("VAR1", "myValue");
		//env.remove("OTHERVAR");
		//env.put("VAR2", env.get("VAR1") + "suffix");
		//pb.directory(new File("myDir"));

		pb = pb.redirectErrorStream(true);
		//Process 
		process = pb.start();

		if (streamGobblerProcessIn == null) {
		    streamGobblerProcessIn = new StreamGobblerReadLineBufferedSpecial(process.getInputStream(), "out") {

			@Override
			public void readEvent(Date d, int intread) {
    //                    System.out.printf("%s : '%c' = %d\n",d.toLocaleString(),(char)intread, intread );
    //                    System.out.flush();
			}

			@Override
			public void readLineEventWithCounter(Date d, int lineNum, String s) {
			}

			@Override
			public void doFinish() {
			    super.doFinish();
			}

			@Override
			public void readLineEvent(Date d, String s) {
			    System.out.printf("%s : [%3d][%s]\n", simpleDateFormat.format(d), s.length(), s);
			    System.out.flush();
			    res.append(s);
			    res.append("\n");
			}
		    };

		} else {
		    //streamGobblerProcessIn.setInputStream(process.getInputStream());
		}

		StreamGobblerReadLineBufferedSpecial streamGobblerProcessErr = new StreamGobblerReadLineBufferedSpecial(process.getErrorStream(), "err") {

		    @Override
		    public void doFinish() {
			super.doFinish();
		    }

		    @Override
		    public void readLineEvent(Date d, String s) {
			System.out.printf("%s : [%3d][%s]\n", simpleDateFormat.format(d), s.length(), s);
			System.out.flush();
			res.append(s);
			res.append("\n");
		    }

		    @Override
		    public void readEvent(Date d, int intread) {
    //                    System.out.printf("%s :: '%c' = %d\n",d.toLocaleString(),(char)intread, intread );
    //                    System.out.flush();
		    }

		    @Override
		    public void readLineEventWithCounter(Date d, int lineNum, String s) {
		    }
		};
		
		streamGobblerProcessErr.start();

		streamGobblerProcessIn.start();

		OutputStream p_out = process.getOutputStream();
		
		if (p_out != null) {
		    // p_out.write(10);
		    if (debugRuntimeExecPre) {
			System.out.printf("%s %s\n", "out on", p_out.getClass().getCanonicalName());
		    }
		    if (p_out instanceof BufferedOutputStream) {
			BufferedOutputStream p_out_buf = (BufferedOutputStream) p_out;
			p_out_buf.close();
		    }
		    p_out.flush();
		    p_out.close();
		} else {
		     if (debugRuntimeExecPre) {
			 System.out.printf("%s\n", "out off");
		     }
		}
		// Pour etre certain d'avoir un StringBuilder bien remplie jusqu'au bout
		// il faut attendre que les thread qui lisent les sorties du process se termine
		streamGobblerProcessErr.join();
		streamGobblerProcessIn.join();
		
		// TO REVIEW / TODO le cas d'un process interactif ou planté ou sans fin ...
		
		// Wait for the process to end.
		process.waitFor();
		int ret = process.exitValue();
		Date dEnd = new Date();

		if (ret == 0) {
		    lastExecSucces = true;
		}

		if (debugRuntimeExecPre) {
		    System.out.printf("Running : ", ret);
		    for (String arg : cmdArray) {
			System.out.printf("%s ", arg);
		    }
   		}
   
		if (debugRuntimeExecPre) {
		    System.out.printf("\nexitValue = %d (in %d ms : out %d err %d)\n", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount);
		}
		res.append(String.format("exitValue = %d (in %d ms : out %d err %d)\n", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount));

	    } catch (IOException | InterruptedException e) {
		if (debugRuntimeExecPre) {
		    System.out.printf("Running : ");
		    for (String arg : cmdArray) {
			System.out.printf("%s ", arg);
		    }
		    System.out.printf("\nexit on error = %s\n", e.getMessage());
		}
		res.append(e.getMessage());

	    }
	    return res.toString();
	}

}
