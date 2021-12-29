package com.marginallyclever.makelangelo.firmwareUploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Try to do a firmware upload.
 * <p>
 * There are a large number of prerequisites and possible difficulties to
 * successfully upload firmware. This class should allow an upload in "basic"
 * cases (which has the prerequisites available and a properly configured and
 * usable COM port, no blocked microcontroller, ...) but otherwise, rather than
 * to try to deal with all eventualities (which would considerably increase the
 * time of analysis, development, tests) in the event of errors or problems, we
 * shoud refer to a troubleshooting wiki page. (TODO wiki page URL in the
 * traductions)
 *
 * <p>
 * Maybe needed for troubleshooting guide:
 * <ul>
 * <li>avrdude documentation :
 * http://www.nongnu.org/avrdude/user-manual/avrdude_3.html#Option-Descriptions<br/>
 * <code>
 * -F</code>
 * <p>
 * Normally, AVRDUDE tries to verify that the device signature read from the
 * part is reasonable before continuing. Since it can happen from time to time
 * that a device has a broken (erased or overwritten) device signature but is
 * otherwise operating normally, this options is provided to override the check.
 * Also, for programmers like the Atmel STK500 and STK600 which can adjust
 * parameters local to the programming tool (independent of an actual connection
 * to a target controller), this option can be used together with ‘-t’ to
 * continue in terminal mode.
 *
 * </li>
 * </lu>
 * <p>
 * NOT IMPLEMENTED :
 * <ul>
 * <li>the name of the "avrdude" command may need to be changed, but for
 * security reasons it should not be in a configuration or preferences
 * file.</li>
 * <li>the arguments of the commands can be adapted for certain cases. But that
 * does mean complex use cases possible because for security reasons the
 * arguments should not be in a config or preference file.</li>
 * <li>read and save flash and eeprom content before writing new flash (.hex
 * file)</li>
 * <li></li>
 * </lu>
 */
public class FirmwareUploader {

    private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);

    /**
     * The path to the directory containing the avrdude command. It is not the
     * same as the path to the avrdude file.
     */
    private String avrdudePath = "";

    /**
     * TODO reorder / review : first ".", then env PATH, then posible "arduino
     * programfile dir", then user home.
     */
    public FirmwareUploader() {

	String avrdudeCOmmandName = getAvrdudeCommandNameDependingOfTheOs();

	File f = new File(avrdudeCOmmandName);

	// PWD (".") and PATH dirs.
	if (avrdudePath != null && avrdudePath.isBlank()) {
	    //Search the env system PATH.
	    ArrayList<String> searchCommandFromEnvPath = FirmwareUploader.searchCommandFromEnvPath(avrdudeCOmmandName, true);
	    if (searchCommandFromEnvPath != null && !searchCommandFromEnvPath.isEmpty()) {
		avrdudePath = searchCommandFromEnvPath.get(0);
	    }
	}

	// // Normally already done ( if  getUserDirectory() is the same as PWD , new File(".") ) 
//		// user dir
//		f = new File(FileAccess.getUserDirectory() + File.separator+name);
//		if(f.exists()) {
//			avrdudePath = f.getAbsolutePath();
//		}
	// avrdude ArduinoIDE embebde version.
	try {
	    if (isOSWindows()) { // only if Windows OS.
		// Communly used variable name for path to programFilesDir on Windows OS.
		String[] windowsOSVarNameForProgFileDir = {
		    "PROGRAMFILES"//C:\Program Files				
		    ,
		     "PROGRAMFILES(x86)"//C:\Program Files (x86)
		    ,
		     "CommunProgramFiles"//
		    ,
		     "CommunProgramFiles(x86)"//
		};
		// Not a good practice to have a hard code partial path but for security reason (avoid possible modification by an evil/dumb user) keep it that way.
		String partialSubPathFromProgramFileAduinoIdeDirToAvrdudeDir = "Arduino\\hardware\\tools\\avr\\bin";

		for (String varName : windowsOSVarNameForProgFileDir) {
		    String varValue = System.getenv(varName);
		    //System.out.println(varName + "=" + varValue);
		    if (varValue != null && !varValue.isBlank()) {
			File posibleAvrdudeDir = new File(varValue, partialSubPathFromProgramFileAduinoIdeDirToAvrdudeDir);
			if (posibleAvrdudeDir.exists() && posibleAvrdudeDir.isDirectory()) {
			    File posibleAvrdudePath = new File(posibleAvrdudeDir, avrdudeCOmmandName);
			    if (posibleAvrdudePath.exists() && posibleAvrdudePath.canExecute()) {
				avrdudePath = posibleAvrdudePath.getAbsolutePath();
				logger.debug(avrdudeCOmmandName + " : \"" + posibleAvrdudePath.getAbsolutePath() + "\"");
				System.out.println(avrdudeCOmmandName + " : \"" + posibleAvrdudePath.getAbsolutePath() + "\"");
			    }
			}
		    }
		}
	    }
	} catch (Exception e) {
	}

    }

    public String getAvrdudeCommandNameDependingOfTheOs() {
	String name = isOSWindows() ? "avrdude.exe" : "avrdude";// Keep it hard coded or don't take responsibility for possible change by the user(or eveil one) if in an editable config ou preference file.
	return name;
    }

    public boolean isOSWindows() {
	String OS = System.getProperty("os.name").toLowerCase();
	boolean isWindowsOS = (OS.contains("win"));
	return isWindowsOS;
    }

//	public void run(String hexPath,String portName) throws Exception {
    /**
     *
     * To run avrdude to do the firmware update with the binary firmware file
     * (.hex) on the serial COM port specified.
     * <p>
     * Warning: As this is a command to be executed on the user's system, always
     * beware of user input, you should not initiate a formatting of the disk or
     * delete files because the user has inadvertently copy paste something like
     * "format C: /Y" in an edit field.
     * <p>
     * ? maybe we should try to read and save the flash and eeprom befor writing
     * ? flash:r:flash_save.hex:i
     * http://www.nongnu.org/avrdude/user-manual/avrdude_3.html#Option-Descriptions
     *
     * @param hexPath the path to the binary file to use in avrdude
     * @param portName the serial COM port to use in avrdude
     * @param textAreaThatCanBeNullForPosibleLogs can be null a SelectTextArea
     * to show to the user the output by append if not null of the avrdude
     * run/exec outputs.
     * @throws Exception
     */
    public boolean run(String hexPath, String portName, SelectTextArea textAreaThatCanBeNullForPosibleLogs) throws Exception {
	logger.debug("update started");

	String avrdudeExecutatbleFilePath = getAvrdudeCommandNameDependingOfTheOs();

	// mixed view path or filepath ?
	Path p = Path.of(avrdudePath, avrdudeExecutatbleFilePath);
	if (p.toFile().isFile() && p.toFile().canExecute()) {
	    // OK
	} else {
	    // KO
	}

	logger.debug("Trying {}", (p.resolve("../avrdude.conf").toString()));
	File fAvrdudeConfFile = p.resolve("../avrdude.conf").toFile();
	if (!fAvrdudeConfFile.exists()) {
	    logger.debug("Trying 2 {}", (p.resolve("../../etc/avrdude.conf").toString()));
	    fAvrdudeConfFile = p.resolve("../../etc/avrdude.conf").toFile();
	    if (!fAvrdudeConfFile.exists()) {
		// in some case ( avrdude correctly installed on the environment (in the path) ) ) there is no need to give the .conf file path to the avrdude command.
		//throw new Exception("Cannot find nearby avrdude.conf");
		logger.error("Cannot find nearby avrdude.conf ");// TODO traduction
	    }
	}

	String confPath = fAvrdudeConfFile.getCanonicalPath();

	//In some case there is no need to give the .conf file.
	String[] options = new String[]{
	    p.toString(),
	    //"-C"+confPath, // OK 
	    //"-v","-v","-v","-v",
	    "-patmega2560",
	    "-cwiring",
	    "-P" + portName,
	    "-b115200",
	    "-D",
	    "-Uflash:w:" + hexPath + ":i"
	};

	//In some case you need the .conf file. (not in the same path of avrdude and not in the system PATH ...)
	if (fAvrdudeConfFile.exists() && fAvrdudeConfFile.canRead()) {
	    options = new String[]{
		p.toString(),
		"-C" + confPath,
		//"-v","-v","-v","-v",
		"-patmega2560",
		"-cwiring",
		"-P" + portName,
		"-b115200",
		"-D", "-Uflash:w:" + hexPath + ":i"
	    };
	}

	// Running the command ...
	// Implementation only for non interactive (no user inputs needed) commande (that terminate ...).		
	logger.debug("(During)Command exec result : ");
	boolean resExec = execBashCommand(options, null, textAreaThatCanBeNullForPosibleLogs, false, true);

	logger.debug("update finished");
	return resExec;
    }

    /**
     * Return the path of the directory that (seem to) containe the avrdude
     * executable file to use.
     *
     * @return
     */
    public String getAvrdudePath() {
	return avrdudePath;
    }

    /**
     * TODO should be a valid path. (File exist(), isDirectory() )
     *
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
	    fu.run("./firmware.hex", "/dev/ttyACM0", new SelectTextArea("test", "test", ""));//linux 
	} catch (Exception e) {
	    e.printStackTrace();
	    logger.error("error: {}", e.getMessage(), e);
	}

    }

    //
    //
    //
    // 
    /**
     * To run on the system a command.Warning: As this is a command to be
     * executed on the user's system, always beware of user input, you should
     * not initiate a formatting of the disk or delete files because the user
     * has inadvertently copy paste something like "C: / Y format" in an edit
     * control.
     *
     * NOT IMPLEMENTED :
     * <ul>
     * <li>a utility class/method.</li>
     * <li>a StreamGobblerReadLineBufferedSpecial interface to ease the
     * implementation.</li>
     * <li>a specific class for the exec results to be more generic (usable in
     * most usage cases)</li>
     * <li>a Timer / Kill switch (frozen command case.)</li>
     * <li>usable for interactive commands.( not implemented to open a Stream to
     * give chars input to the process ... )</li>
     * </ul>
     * <p>
     * TO REVEIW ( as this is a way to exec commands on the user system ... )
     * should be a least private for security (and all methodes that using it )
     * ?
     *
     * @param cmdArray the command Array (command and arguments) used to create
     * the process. somethign like  <code>new String[]{"ls", "-l"}</code>.
     * @param streamGobblerProcessIn can be null, the streamGobbler to take care
     * of the Process outputs ( get the erros stream only if normal output and
     * error output merged). Only to be usable in other usage than the one
     * actualy implimented)
     * @param execResult a GUI Element ( basicaly a JTextArea .append(..) ) to
     * show the output of the command executed.
     * @param modeLineByLine to take care of the command output char by char or
     * line by line. (may have to take care of non printable char in char by
     * char mode for some command not implemented )
     * @param preAppendCommandAndAtTheEndAppendTheExitCodeValue to pre append to
     * the posible not null execResult JTextArea like the command executed end
     * post append posible exec exception or exitValue of the exec)
     * @return true if the Process.exitValue() == 0 (normaly this means the
     * command have been executed and terminated succeffuly with no errors/no
     * exec exceptions (but the programme executed by the command have to use
     * exitValues diffrent from 0 in the case of termination on an error ... ))
     */
    protected static boolean execBashCommand(
	    String[] cmdArray,
	    StreamGobblerReadLineBufferedSpecial streamGobblerProcessIn,
	    SelectTextArea execResult,
	    boolean modeLineByLine,
	    boolean preAppendCommandAndAtTheEndAppendTheExitCodeValue
    ) {

	logger.debug("Running : " + processStringArrayCommandToPlainString(cmdArray));

	if (execResult != null && preAppendCommandAndAtTheEndAppendTheExitCodeValue) {
	    execResult.append("Running : " + processStringArrayCommandToPlainString(cmdArray) + "\n");
	}
	try {
	    Date dStar = new Date();
	    Process process;
	    ProcessBuilder pb = new ProcessBuilder(cmdArray);

	    // If you want to "alter" the environment given to the process before running the process
	    //Map<String, String> env = pb.environment();
	    //env.put("VAR1", "myValue");
	    //env.remove("OTHERVAR");
	    //env.put("VAR2", env.get("VAR1") + "suffix");
	    //pb.directory(new File("myDir"));
	    // To merge err on out // TO REVIEW for specials case.
	    pb = pb.redirectErrorStream(true);

	    //Process 
	    process = pb.start();

	    // the process is started we can now get is streams ( standard output, error output of the command ).
	    // N.B. : confusing naming, from our program's point of view the getInputStream() is the "output" of the process ("output" for the process point of view).
	    if (streamGobblerProcessIn == null) {
		// to connect the "output" of the process as an input streamGobbler.
		streamGobblerProcessIn = new StreamGobblerReadLineBufferedSpecial(process.getInputStream(), "out") {

		    // ! to be thread safe do not move this SimpleDateFormat (so each thread have one) ?
		    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");

		    @Override
		    public void readEvent(Date d, int intread) {
			if (execResult != null && !modeLineByLine) {
			    execResult.append("" + (char) intread);
			    //TODO a better way to scroll the JTextArea.
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
			logger.debug(String.format("%s : [%3d][%s]", simpleDateFormat.format(d), s.length(), s));
			if (execResult != null && modeLineByLine) {
			    execResult.append(s + "\n");
			}
		    }
		};

	    }

	    // Normally has the error stream is merged in out stream (see ProcessBuilder redirectErrorStream(true); )
	    // this will not be used but to keep if we don't want the merge.
	    // For the process output error stream.
	    StreamGobblerReadLineBufferedSpecial streamGobblerProcessErr = new StreamGobblerReadLineBufferedSpecial(process.getErrorStream(), "err") {

		// ! to be thread safe do not move this SimpleDateFormat (so each thread have one) ?
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");

		@Override
		public void doFinish() {
		    super.doFinish();
		}

		@Override
		public void readLineEvent(Date d, String s) {
		    logger.debug(String.format("%s : [%3d][%s]", simpleDateFormat.format(d), s.length(), s));
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

	    // TODO a timer to kill the process if it never ends ...
	    //if ( notAnInteractiveCommandWithAnProcessInputStremOpen && delaisFromLastReadEvent > ??? ) process.destroy(); // ??
	    // To be sure to have a well-filled StringBuilder to the end
	    // you have to wait for the threads that read the outputs of the process to end.
	    streamGobblerProcessErr.join();
	    streamGobblerProcessIn.join();

	    // Wait for the process to end.
	    process.waitFor();
	    int ret = process.exitValue();
	    Date dEnd = new Date();

	    logger.debug("Running : " + processStringArrayCommandToPlainString(cmdArray));
	    logger.debug(String.format("exitValue = %d (in %d ms : out %d err %d)", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount));

	    if (execResult != null && preAppendCommandAndAtTheEndAppendTheExitCodeValue) {
		execResult.append(String.format("exitValue = %d (in %d ms)\n", ret, dEnd.getTime() - dStar.getTime(), streamGobblerProcessIn.readCount, streamGobblerProcessErr.readCount));
	    }

	    return (ret == 0);// normally a 0 value for process.exitValue() means a success (no errors only if the commande used non 0 return value if errors). 

	} catch (IOException | InterruptedException e) {
	    logger.debug("Running : " + processStringArrayCommandToPlainString(cmdArray));
	    logger.debug(" Exception : {}", e.getMessage(), e);
	    if (execResult != null && preAppendCommandAndAtTheEndAppendTheExitCodeValue) {
		execResult.append(String.format("Exception : %s\n", e.getMessage()));
	    }
	}

	return false; // Something go wrong. (exception to study in the logs ...) )
    }

    /**
     * Utility function to get a plain String from a String array.
     *
     * <p>
     * No implemntation to be usable in a cut and past to a consol, (for that
     * you may have to do some chars protection (reserved char like '\', '$',
     * '%', ...) or cotting ".. ." or spaces protection if some ' ' spaces.)
     *
     * @param cmdArray
     * @return
     */
    public static String processStringArrayCommandToPlainString(String[] cmdArray) {
	StringBuilder sbCommande = new StringBuilder();
	if (cmdArray != null && cmdArray.length > 0) {
	    for (String arg : cmdArray) {
		// to be usable in a cut and past to a consol
		//arg = arg.replaceAll(" ","\\ "); // ?
		//if ( arg.contains("\"")) // ?
		// ... 

		sbCommande.append(arg).append(" ");
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
     * Utility function to find if an executable file with the name commandName
     * is in the system PATH.
     * <ul>
     * <li>do not check the system OS so under windows you need to specify a
     * "cmdName.exe" or "cmdName.com" or ... </li>
     * <li>do not work for bash specific command (only implemented in the bash)
     * like "export" or "set" ...</li>
     * <li>on some system/JRE configuration, File.canExecute() can always return
     * true.</li>
     * </ul>
     *
     * @param commandName the name of the command to find.
     * @param addCurrentPwdFirst to check in the current directory first. (may
     * not be in the system PATH (security reasons)).
     * @return an arrayList of the parents directory of executable files
     * matching ( in the order of the system PATH ).
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

	    String pathDelimiter = File.pathSeparator; // or : String pathDelimiter = System.getProperty("path.separator");
	    String envPathVariable = System.getenv("PATH");
	    for (String s : envPathVariable.split(pathDelimiter)) {
		//for dev debug : //System.out.println("> "+s);
		File f = new File(s, commandName);
		if (f.exists() && f.canExecute()) {
		    res.add(f.getParent());
		}
	    }
	} catch (Exception ignored) {
	    // maybe in an applet or some securitymanager restriction.
	    // therefore certainly not possible Process exec therefore to be ignored. (The result array will then certainly be empty.)
	}
	return res;
    }

}
