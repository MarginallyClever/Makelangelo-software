package com.marginallyclever.makelangelo.firmwareUploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FirmwareUploader {

	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);

	//private String avrdudePath = "";
	private String avrdudePath = "avrdude";// if this is in the path juste the cmd (linux) (for windows avrdude.exe )

	/**
	 * TODO TO REVIEW : if avrdude is in the path .
	 * 
	 * symply a Process exec "avrdude --version" that return 0 ?
	 * 
	 * or on mac os : ?
	 * 
	 * or on linux : using the whereis commande : 
	 * 
	 * OK : 
	 * <code>$ whereis -b avrdude
	 * avrdude: /usr/bin/avrdude /etc/avrdude.conf
	 * </code>
	 * 
	 * Not found : 
	 * <code>$ whereis -b avrdude
	 * avrdude:
	 * </code>
	 * 
	 * or on windows : ??? parse the path env variable and check eatch dirs in the path to maybe find it ?
	 * 
	 */
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
	
//	public void run(String hexPath,String portName) throws Exception {
	/**
	 * TO REVIEW : in some case ( avrdude correctly installed on the environment (in the path) ) ) there is no need to give the .conf file path to the avrdude command.
	 * @param hexPath
	 * @param portName
	 * @throws Exception 
	 */
	public String run(String hexPath,String portName, SelectTextArea textAreaThatCanBeNullForPosibleLogs) throws Exception {
		logger.debug("update started");
	    
		String resRunLog = "";
		
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {}", (p.resolve("../avrdude.conf").toString()));
		File f = p.resolve("../avrdude.conf").toFile();
		if(!f.exists()) {
			logger.debug("Trying 2 {}", (p.resolve("../../etc/avrdude.conf").toString()));
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
	    		//"-C"+confPath, //TODO In some case there is no need to give the .conf file.
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
		String fullExecCmdOutputsAsTexte = execBashCommand(options, null,textAreaThatCanBeNullForPosibleLogs);
		// For simple test :  String ouptups =  execBashCommand(new String[]{"ls"}, null);
		resRunLog = fullExecCmdOutputsAsTexte;
		System.out.println("");
		System.out.println("(After)Commande exec result : is a succes = " + lastExecSucces);
		System.out.println(fullExecCmdOutputsAsTexte);

		logger.debug("update finished");
		//Log.message("update finished");
		return resRunLog;
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
	
	public static void main(String[] args) throws Exception {
		FirmwareUploader fu = new FirmwareUploader();
		
		//fu.run("./firmware.hex", "COM3");
		
		try {
			fu.run("./firmware.hex", "COM3", new SelectTextArea("test","test","") );
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//
	//
	//
	
	//Not Thread safe (have to create a new at eatch need)
	//public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");

	static boolean debugRuntimeExecPre = true;
	static boolean lastExecSucces = false;

	// TODO ! for a clean multiple info return.
	class ExecBashCommandResult{
	    boolean haveStart = true;
	    boolean isFinis = false;
	    Integer returnCode = null;
	    String logs = "";
	    
	    //TODO 
	    
	    
	}
	// TO REVIEW / TODO le cas d'un process interactif ou sans fin ...
	// TO REVEIW ( as this is a way to exec commands on the user system ... )
	// should be a least private for security (and all methodes that using it ) ?
	protected static String execBashCommand(String[] cmdArray, StreamGobblerReadLineBufferedSpecial streamGobblerProcessIn,SelectTextArea  execResult) {
	    
	    
	    lastExecSucces = false;
	    final StringBuffer res = new StringBuffer();
	    res.setLength(0);
	    
	    // Only for dev debug (can be remove)
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
		
		// If you want to "alter" the environement given to the process befor running the process
		//Map<String, String> env = pb.environment();
		//env.put("VAR1", "myValue");
		//env.remove("OTHERVAR");
		//env.put("VAR2", env.get("VAR1") + "suffix");
		//pb.directory(new File("myDir"));

		// To merge err on out // TO REVIEW for specials case.
		pb = pb.redirectErrorStream(true);
		
		//Process 
		process = pb.start();

		// As the process is started we can now get is streams ( standard output, error output of the command ).
		
		// N.B. : confusing naming, from our program's point of view the getInputStream() is the "output" of the process ("output" for the process point of view).
		if (streamGobblerProcessIn == null) {
		    // to connecte the "output" of the process as an input streamGobbler.
		    streamGobblerProcessIn = new StreamGobblerReadLineBufferedSpecial(process.getInputStream(), "out") {
			
			// ! to be thread safe do not move this SimpleDateFormat (so eatch thread have one) ?
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");
			
			@Override
			public void readEvent(Date d, int intread) {
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
			    if ( debugRuntimeExecPre){				
			    System.out.printf("%s : [%3d][%s]\n", simpleDateFormat.format(d), s.length(), s);
			    System.out.flush();
			    }
			    res.append(s);
			    res.append("\n");
			    if ( execResult != null){
				execResult.append(s+"\n");
			    }
			}
		    };

		} else {
		    //TODO a revoir ... 
		    // This was a try for interactive command (that ask input from user ...) but not implemented here.
		    //streamGobblerProcessIn.setInputStream(process.getInputStream());
		}

		// Normaly has the error stream is merged in out strem (see ProcessBuilder redirectErrorStream(true); )
		// this will not be use but to keep if we dont want the merge.
		// for the process output error stream.
		StreamGobblerReadLineBufferedSpecial streamGobblerProcessErr = new StreamGobblerReadLineBufferedSpecial(process.getErrorStream(), "err") {
		    
		    // ! to be thread safe do not move this SimpleDateFormat (so eatch thread have one) ?
		    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");
		    
		    @Override
		    public void doFinish() {
			super.doFinish();
		    }

		    @Override
		    public void readLineEvent(Date d, String s) {
			if ( debugRuntimeExecPre){
			    
			System.out.printf("%s : [%3d][%s]\n", simpleDateFormat.format(d), s.length(), s);
			System.out.flush();
			}
			res.append(s);
			res.append("\n");
		    }

		    @Override
		    public void readEvent(Date d, int intread) {
		    }

		    @Override
		    public void readLineEventWithCounter(Date d, int lineNum, String s) {
		    }
		};
		
		// Starting the Threads that take care of the process outputs...
		streamGobblerProcessErr.start();

		streamGobblerProcessIn.start();

		
//		{
//		    // only for interactive commande ( a commande that require input from the user ... )
//		    // if we need to send input to the Process (not implemented ...)
//		    OutputStream p_out = process.getOutputStream();
//		    if (p_out != null) {
//			// p_out.write(10);
//			if (debugRuntimeExecPre) {
//			    System.out.printf("%s %s\n", "out on", p_out.getClass().getCanonicalName());
//			}
//			if (p_out instanceof BufferedOutputStream) {
//			    BufferedOutputStream p_out_buf = (BufferedOutputStream) p_out;
//			    p_out_buf.close();
//			}
//			p_out.flush();
//			p_out.close();
//		    } else {
//			 if (debugRuntimeExecPre) {
//			     System.out.printf("%s\n", "out off");
//			 }
//		    }
//		}
		
		// TODO a timer to kill the process if it never terminate ...
		//if ( notAnInteractiveCommandWithAnProcessInputStremOpen && delaisFromLastReadEvent > ??? ) process.destroy(); // ??
		
		
		// Pour etre certain d'avoir un StringBuilder bien remplie jusqu'au bout
		// il faut attendre que les thread qui lisent les sorties du process se termine
		streamGobblerProcessErr.join();
		streamGobblerProcessIn.join();
		
		
		// Wait for the process to end.
		process.waitFor();
		int ret = process.exitValue();
		Date dEnd = new Date();
		
		lastExecSucces = (ret == 0);

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
