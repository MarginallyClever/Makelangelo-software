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
	public boolean run(String hexPath,String portName, SelectTextArea textAreaThatCanBeNullForPosibleLogs) throws Exception {
		logger.debug("update started");
	    
		
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
		boolean resExec =  execBashCommand(options, null,textAreaThatCanBeNullForPosibleLogs,false);
		// For simple test :  String fullExecCmdOutputsAsTexte =  execBashCommand(new String[]{"ls"}, null);
		
		
		logger.debug("update finished");
		return resExec;
	}

	/**
	 * Return the path of the arvdude executable file to use.
	 * @return 
	 */
	public String getAvrdudePath() {
	    return avrdudePath;
	}

	/**
	 * TODO should be a valid path. (File exist(), canExecute() )
	 * @param avrdudePath 
	 */
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
	
	/**
	 * Only for dev prurpose TODO to remove as we have a logger with debug level.
	 */
	static boolean debugRuntimeExecPre = true;

	
	// TO REVIEW / TODO le cas d'un process interactif ou sans fin ...
	// TO REVEIW ( as this is a way to exec commands on the user system ... )
	// should be a least private for security (and all methodes that using it ) ?
	/**
	 * 
	 * @param cmdArray the command Array (command and arguments) used to create the process. somethign like  <code>new String[]{"ls", "-l"}</code>
	 * @param streamGobblerProcessIn can be null, the streamGobbler to take care of the Process outputs ( normal output and error output merged). Only to be usable in other usage than the one actualy implimented)
	 * @param execResult a GUI Element ( basicaly a JTextArea) to show the output of the command executed.
	 * @param onlyOnNewLine to take care of the command output char by char or line by line. (may have to take care of non printable char in char by char mode for some command not implemented ) 
	 * @return true if the Process.exitValue() == 0 (normaly this means the command have been executed and terminated succeffuly with no errors (but the programme executed by the command have to use exitValues diffrent from 0 in the case of termination on an error ... ))
	 */
	protected static boolean execBashCommand(String[] cmdArray, StreamGobblerReadLineBufferedSpecial streamGobblerProcessIn,SelectTextArea  execResult,boolean onlyOnNewLine) {
	    
	    
	    // Only for dev debug (can be remove)
	    if (debugRuntimeExecPre) {
		logger.debug("Running : "+processStringArrayCommandToPlainString(cmdArray));
	    }

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
			     if ( execResult != null && !onlyOnNewLine){
				execResult.append(""+(char)intread);
				//TODO a better way to scrool the JTextArea.
				execResult.getFeild().setCaretPosition(execResult.getFeild().getText().length());
			    }
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
				logger.debug(String.format("%s : [%3d][%s]", simpleDateFormat.format(d), s.length(), s));
			    }
			    
			    if ( execResult != null && onlyOnNewLine){
				execResult.append(s+"\n");
			    }
			}
		    };

		} 

		// Normaly has the error stream is merged in out stream (see ProcessBuilder redirectErrorStream(true); )
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
		

		if (debugRuntimeExecPre) {
		    logger.debug("Running : "+processStringArrayCommandToPlainString(cmdArray));   		
		    logger.debug(String.format("exitValue = %d (in %d ms : out %d err %d)", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount));
		}	
		
		
		return (ret == 0);// normaly a 0 value as process.exitValue() means a success (no errors). 
		
	    } catch (IOException | InterruptedException e) {
		
		// 
		if (debugRuntimeExecPre) {		    
		    logger.debug("Running : "+processStringArrayCommandToPlainString(cmdArray));
		    logger.debug(" exit on error : {}", e.getMessage());
		}
				
		logger.error("Failed to uploade firmware : {}", e.getMessage());
	    }
	    
	    return false; // Something go wrong.
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
