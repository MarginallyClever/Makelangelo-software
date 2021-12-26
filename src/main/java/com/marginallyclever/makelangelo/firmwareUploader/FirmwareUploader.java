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
import java.nio.file.FileSystem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class FirmwareUploader {

	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);

	private String avrdudePath = "";

	/**
	 * TODO to reorder : first ".", then env PATH, then posible "arduino programfile dir", then user home.
	 */
	public FirmwareUploader() {
		String OS = System.getProperty("os.name").toLowerCase();
		boolean isWindowsOS = (OS.indexOf("win") >= 0);
		String name =  isWindowsOS ? "avrdude.exe": "avrdude";
		
//		// if Arduino is not installed in the default windows location, offer the current working directory (fingers crossed)
		File f = new File(name);
//		if(f.exists()) {
//			avrdudePath = f.getAbsolutePath();
//			return;
//		}
						
		try {
		    if (isWindowsOS) { // only if Windows OS.
			// Communly used variable name for path to programFilesDir on Windows OS.
			String[] windowsOSVarNameForProgFileDir = {
			    "PROGRAMFILES"//C:\Program Files				
			    ,"PROGRAMFILES(x86)"//C:\Program Files (x86)
			    ,"CommunProgramFiles"//
			    ,"CommunProgramFiles(x86)"//
			};
			String FromProgramFileAduinoIdeDirToAvrdudeDir = "Arduino\\hardware\\tools\\avr\\bin";
				
			for (String varName : windowsOSVarNameForProgFileDir) {
			    String varValue = System.getenv(varName);
			    System.out.println(varName+"="+varValue);
			    if ( varValue != null && !varValue.isBlank() ){
				File posibleAvrdudeDir = new File(varValue, FromProgramFileAduinoIdeDirToAvrdudeDir);
				if (posibleAvrdudeDir.exists() && posibleAvrdudeDir.isDirectory()) {
				    File posibleAvrdudePath = new File(posibleAvrdudeDir, name);
				    if (posibleAvrdudePath.exists() && posibleAvrdudePath.canExecute()) {
					avrdudePath = posibleAvrdudePath.getAbsolutePath();
					logger.debug(name+" : \""+posibleAvrdudePath.getAbsolutePath()+"\"");
					System.out.println(name+" : \""+posibleAvrdudePath.getAbsolutePath()+"\"");
				    }
				}
			    } else{
					    
			    }
			}
		    }
		} catch (Exception e) {

		}
//		f = new File("C:\\Program Files (x86)\\Arduino\\hardware\\tools\\avr\\bin\\"+name);
//		if(f.exists()) {
//			avrdudePath = f.getAbsolutePath();
//			return;
//		} 
		
		// user home 
		f = new File(FileAccess.getUserDirectory() + File.separator+name);
		if(f.exists()) {
			avrdudePath = f.getAbsolutePath();
		}
		
		// PWD (".") and PATH dirs.
		if ( avrdudePath != null && avrdudePath.isBlank()){
		    //Search the env system PATH.
		    ArrayList<String> searchCommandFromEnvPath = FirmwareUploader.searchCommandFromEnvPath(name, true);
		    if (searchCommandFromEnvPath != null && searchCommandFromEnvPath.size() > 0) {
			avrdudePath = searchCommandFromEnvPath.get(0);
		    }	    
		}
	}
	
//	public void run(String hexPath,String portName) throws Exception {
	/**
	 *  TODO to review return normaly not used.
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
			    // in some case ( avrdude correctly installed on the environment (in the path) ) ) there is no need to give the .conf file path to the avrdude command.
			    //throw new Exception("Cannot find nearby avrdude.conf");
			    logger.error("Cannot find nearby avrdude.conf ");// TODO traduction
			}
		}
		
		String confPath = f.getAbsolutePath();
		
		//In some case there is no need to give the .conf file.
		String [] options = new String[]{
				avrdudePath,
	    		//"-C"+confPath, // OK 
	    		//"-v","-v","-v","-v",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D","-Uflash:w:"+hexPath+":i"
		    }; 
		
		//In some case you need the .conf file. (not in the same path of avrdude ...)
		if ( f.exists() && f.canRead()){
		    options = new String[]{
				avrdudePath,
	    		"-C"+confPath, 
	    		//"-v","-v","-v","-v",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D","-Uflash:w:"+hexPath+":i"
		    }; 
		}
		
		// depreated : 
		//runCommand(options);
		
		// Running the command ...
		// Only for non interactive (no inputs) commande (that terminate ... TODO timer for non terminating commandes).
		//
		logger.debug("(During)Commande exec result : ");
		String fullExecCmdOutputsAsTexte = execBashCommand(options, null,textAreaThatCanBeNullForPosibleLogs);
		// For simple test :  String fullExecCmdOutputsAsTexte =  execBashCommand(new String[]{"ls"}, null);
		resRunLog = fullExecCmdOutputsAsTexte;
		logger.debug("(After)Commande exec result : is a succes = " + lastExecSucces);
		logger.debug(fullExecCmdOutputsAsTexte);

		logger.debug("update finished");
		return resRunLog;
	}

	/**
	 * 
	 * @param cmd
	 * @throws Exception
	 * @deprecated cause it don't use process exit code value.
	 */
	@Deprecated
	private void runCommand(String[] cmd) throws Exception {
		Process p = Runtime.getRuntime().exec(cmd);
		//runStreamReaders(p);
		runBufferedReaders(p);
	}

	/**
	 * 
	 * @param p
	 * @throws IOException
	 * @deprecated as some error and output stream can be interlaced.
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private void runStreamReaders(Process p) throws IOException {
		InputStreamReader stdInput = new InputStreamReader(p.getInputStream());
		InputStreamReader stdError = new InputStreamReader(p.getErrorStream());

		logger.debug("errors (if any):\n");
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
	
	@Deprecated
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
	
	/**
	 * Return the path of the arvdude executable file to use.
	 * @return 
	 */
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
			//fu.run("./firmware.hex", "COM3", new SelectTextArea("test","test","") );//Windows
			fu.run("./firmware.hex", "/dev/ttyACM0", new SelectTextArea("test","test","") );//linux 
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("error: {}", e.getMessage());
		}
		
	}
	
	//
	//
	//
	
	//public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");//Not Thread safe (have to create a new at eatch need)

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
		logger.debug("Running : "+processStringArrayCommandToPlainString(cmdArray));
	    }
	    //
	    res.append("Running : "+processStringArrayCommandToPlainString(cmdArray));
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
			
//			@Override
//			public void readEvent(Date d, int intread) {
//			}

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
				logger.debug(String.format("%s : [%3d][%s]", simpleDateFormat.format(d), s.length(), s));
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
			    logger.debug(String.format("%s : [%3d][%s]", simpleDateFormat.format(d), s.length(), s));
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
//			    logger.debug(String.format("%s %s", "out on", p_out.getClass().getCanonicalName()));
//			}
//			if (p_out instanceof BufferedOutputStream) {
//			    BufferedOutputStream p_out_buf = (BufferedOutputStream) p_out;
//			    p_out_buf.close();
//			}
//			p_out.flush();
//			p_out.close();
//		    } else {
//			 if (debugRuntimeExecPre) {
//			     logger.debug(String.format("%s", "out off"));
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
		    logger.debug("Running : "+processStringArrayCommandToPlainString(cmdArray));   		
		    logger.debug(String.format("exitValue = %d (in %d ms : out %d err %d)", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount));
		}
		
		res.append(String.format("exitValue = %d (in %d ms : out %d err %d)\n", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount));

	    } catch (IOException | InterruptedException e) {
		
		// 
		if (debugRuntimeExecPre) {		    
		    logger.debug("Running : "+processStringArrayCommandToPlainString(cmdArray));
		    logger.debug(" exit on error : {}", e.getMessage());
		}
		
		res.append(e.getMessage());
		
		logger.error("Failed to uploade firmware : {}", e.getMessage());
	    }
	    
	    return res.toString();
	}
	
	
	/**
	 * Utility function to get a plaine String from a String array.
	 * 
	 * TODO : char protection " " ...
	 * 
	 * @param cmdArray
	 * @return 
	 */
	public static String processStringArrayCommandToPlainString(String[] cmdArray) {
	    StringBuilder sbCommande = new StringBuilder();
	    if (cmdArray != null && cmdArray.length > 0) {
		for (String arg : cmdArray) {
		    // TODO to be usable in a cut and past to a consol, may have to do some char protection or ...
		    //arg = arg.replaceAll(" ","\\ ");
		    //if ( arg.contains("\""))
		    // ...
			
		    sbCommande.append(arg + " ");
		}
		if (sbCommande.length() > 0) {
		    //To remove a posible space add from the for 
		    sbCommande.setLength(sbCommande.length() - 1);
		}
	    }
	    return sbCommande.toString();
	}
	
	//
	//
	//
	
	/**
	 * Utility function to find if an executable file with the name commandName is in the systeme PATH.
	 * 
	 * ( do not check the systeme OS so under windows you need to specifiy a "cmdName.exe" or "cmdName.com" or ... )
	 * 
	 * ( do not work for bash specific command like "export" or "set" ... )
	 * 
	 * ( on some system/JRE configuration, File.canExecute() can alwayse return true.)
	 * 
	 * @param commandName the name of the commande to find.
	 * @param addCurrentPwdFirst to check in the currend directory. (may not be in the system PATH (security reasons))
	 * @return an arrayList of the executable files matching ( in the order of the system PATH ).
	 */
	public static ArrayList<String> searchCommandFromEnvPath(String commandName, boolean addCurrentPwdFirst) {
	    ArrayList<String> res = new ArrayList<>();
	    try { // if in an applet to avoid security exception ?

		if (addCurrentPwdFirst) {
		    File f = new File(commandName);
		    if (f.exists() && f.canExecute()) {
			res.add(f.toString());
		    }
		}
		
		String pathDelimiter = File.pathSeparator;
		//String pathDelimiter = System.getProperty("path.separator");
		String envPathVariable = System.getenv("PATH");
		for (String s : envPathVariable.split(pathDelimiter)) {
		    //System.out.println("> "+s);
		    File f = new File(s, commandName);
		    if (f.exists() && f.canExecute()) {
			res.add(f.toString());
		    }
		}
	    } catch (Exception e) {
	    }
	    return res;
	}

}
