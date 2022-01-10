package com.marginallyclever.makelangelo.firmwareUploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import com.marginallyclever.makelangelo.select.SelectTextArea;
import java.util.ArrayList;

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
 * </ul>
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
 * </ul>
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

	String avrdudeCommandName = getAvrdudeCommandNameDependingOfTheOs();

	File f = new File(avrdudeCommandName);

	// PWD (".") and PATH dirs.
	if (avrdudePath != null && avrdudePath.isBlank()) {
	    //Search the env system PATH.
	    ArrayList<String> searchCommandFromEnvPath = ProcessExecCmd.searchCommandFromEnvPath(avrdudeCommandName, true);
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
	    if (ProcessExecCmd.isOSWindows()) {
		String[] windowsOSVarNameForProgFileDir = ProcessExecCmd.getWindowsOsCommunlyUsedEnvVarNameProgramFiles();
		// Not a good practice to have a hard code partial path but for security reason (avoid possible modification by an evil/dumb user) keep it that way.
		String partialSubPathFromProgramFileAduinoIdeDirToAvrdudeDir = "Arduino\\hardware\\tools\\avr\\bin";

		for (String varName : windowsOSVarNameForProgFileDir) {
		    String varValue = System.getenv(varName);
		    //System.out.println(varName + "=" + varValue);
		    if (varValue != null && !varValue.isBlank()) {
			File posibleAvrdudeDir = new File(varValue, partialSubPathFromProgramFileAduinoIdeDirToAvrdudeDir);
			if (posibleAvrdudeDir.exists() && posibleAvrdudeDir.isDirectory()) {
			    File posibleAvrdudePath = new File(posibleAvrdudeDir, avrdudeCommandName);
			    if (posibleAvrdudePath.exists() && posibleAvrdudePath.canExecute()) {
				avrdudePath = posibleAvrdudePath.getAbsolutePath();
				logger.debug(avrdudeCommandName + " : \"" + posibleAvrdudePath.getAbsolutePath() + "\"");
				System.out.println(avrdudeCommandName + " : \"" + posibleAvrdudePath.getAbsolutePath() + "\"");
			    }
			}
		    }
		}
	    }
	} catch (Exception e) {
	}

    }

    public static String getAvrdudeCommandNameDependingOfTheOs() {
	String name = ProcessExecCmd.isOSWindows() ? "avrdude.exe" : "avrdude";// Keep it hard coded or don't take responsibility for possible change by the user(or eveil one) if in an editable config ou preference file.
	return name;
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
     * TODO ? maybe we should try to read and save the flash and eeprom befor
     * writing TODO ? flash:r:flash_save.hex:i
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

	String avrdudeExecCmdForTheOS = getAvrdudeCommandNameDependingOfTheOs();

	// mixed view path or filepath ?
	Path avrdudeExecCmdFullPathForTheOs = Path.of(avrdudePath, avrdudeExecCmdForTheOS);
	if (avrdudeExecCmdFullPathForTheOs.toFile().isFile() && avrdudeExecCmdFullPathForTheOs.toFile().canExecute()) {
	    // OK
	} else {
	    // KO
	    // TODO throw distinguishable exception for the GUI to highlight the field in default.
	}

	// TODO : review : this is to find the .conf file of avrdude, only needed if ? not in the same path of avrdude cmd or if avrdude in not in the system PATH (corrctely installed in the system)
	logger.debug("Trying {}", (avrdudeExecCmdFullPathForTheOs.resolve("../avrdude.conf").toString()));
	File fAvrdudeConfFile = avrdudeExecCmdFullPathForTheOs.resolve("../avrdude.conf").toFile();
	if (!fAvrdudeConfFile.exists()) {
	    logger.debug("Trying 2 {}", (avrdudeExecCmdFullPathForTheOs.resolve("../../etc/avrdude.conf").toString()));
	    fAvrdudeConfFile = avrdudeExecCmdFullPathForTheOs.resolve("../../etc/avrdude.conf").toFile();
	    if (!fAvrdudeConfFile.exists()) {
		// in some case ( avrdude correctly installed on the environment (in the path) ) ) there is no need to give the .conf file path to the avrdude command.
		//throw new Exception("Cannot find nearby avrdude.conf");
		logger.error("Cannot find nearby avrdude.conf ");// TODO traduction
	    }
	}

	String confPath = fAvrdudeConfFile.getCanonicalPath();

	String[] options = getFullAvrDudeCmdWithArguments(avrdudeExecCmdFullPathForTheOs, fAvrdudeConfFile, confPath, portName, hexPath);

	// Running the command ...
	// Implementation only for non interactive (no user inputs needed) commande (that terminate ...).		
	logger.debug("(During)Command exec result : ");
	boolean resExec = ProcessExecCmd.execBashCommand(options, null, textAreaThatCanBeNullForPosibleLogs, false, true);

	logger.debug("update finished");
	return resExec;
    }

    /** Building the avrdude command and aguments.
     * Refer to
     * <a href="http://www.nongnu.org/avrdude/user-manual/avrdude_3.html#Option-Descriptions">http://www.nongnu.org/avrdude/user-manual/avrdude_3.html#Option-Descriptions</a>
     *
     * @param avrdudeExecCmdFullPathForTheOs the avrdude commande in the system, "avrdude" or "avrdude.exe" or the full path ...
     * @param fAvrdudeConfFile the path to the avrdude.conf file to use
     * @param confPath TODO get ride of this arg ... doublon juste not to take care of posible exception getAbsolutPath
     * @param portName 
     * @param hexPath the path of the file .hex to read from the flash to be upload to the microcontroleur.
     * @return array to create the Process, the avrdude command and aguments.
     */
    public String[] getFullAvrDudeCmdWithArguments(Path avrdudeExecCmdFullPathForTheOs, File fAvrdudeConfFile, String confPath, String portName, String hexPath) {
	// TODO factorizing to review maybe a better way to do build a commande line arguments array with conditionnal elements ...

	int verboseLevel = 0;
	ArrayList<String> commandArryBuilding = new ArrayList<>();
	
	// the commande (mandatory)
	commandArryBuilding.add(avrdudeExecCmdFullPathForTheOs.toString());
	
	// some argument or not to specify the avrdude.conf file.
	if (fAvrdudeConfFile.exists() && fAvrdudeConfFile.canRead()) {
	    commandArryBuilding.add("-C" + confPath);
	} else {
	    // maybe no need to give the config file. (finger cross ...)
	}
	
	// somes agument to set or not avrdude verbose level
	for (int level = 0; level < verboseLevel; level++) {
	    // specific to avrdude the more you add "-v" the more verbose it get.
	    commandArryBuilding.add("-v");
	}
	
	// the chip type
	commandArryBuilding.add("-patmega2560");
	
	// the connection methode
	commandArryBuilding.add("-cwiring");
	commandArryBuilding.add("-P" + portName);
	
	// baudrate
	commandArryBuilding.add("-b115200");
	
	// -D
	/*Disable auto erase for flash. 
	When the -U option with flash memory is specified, avrdude will perform 
	a chip erase before starting any of the programming operations, 
	since it generally is a mistake to program the flash without performing an erase first. 
	This option disables that. Auto erase is not used for ATxmega devices as these devices can use page erase 
	before writing each page so no explicit chip erase is required. Note however that any page not affected by 
	the current operation will retain its previous contents.*/
	commandArryBuilding.add("-D");	

	// the action type ( read/write flash/eeprom/fuse ... ) can be multiple 	
	boolean actionReadToSaveBeforWriting = false;
	if ( actionReadToSaveBeforWriting){
	    // To review is this needed ? 
	    // To review as the .hex file can be in a non writable path this have to be adapte. to be sure we have a writable file to do the save.
	 // read the flash from the microcontroleur to a file ... ( but if the chips is protected you do not get a usable file ...)
	  commandArryBuilding.add("-Uflash:r:" + hexPath + ".save." + System.currentTimeMillis() + ".hex" + ":i");
	  //todo save the eeprom ? ... so we also need to write it later : TODO
	}
	
	boolean actionWrite = true;	  
	if (actionWrite) {
	    //write the flash from the file hexPath to the microcontroleur	
	    commandArryBuilding.add("-Uflash:w:" + hexPath + ":i");
	}
	
	// 
	String[] options = commandArryBuilding.toArray(String[]::new); // casting trick "String[]::new" equivalent to "new String[0]" to get a String[] and not an Object[] ...

//	//In some case there is no need to give the .conf file.
//	String[] options = new String[]{
//	    p.toString(),
//	    //"-C"+confPath, // OK 
//	    //"-v","-v","-v","-v",
//	    "-patmega2560",
//	    "-cwiring",
//	    "-P" + portName,
//	    "-b115200",
//	    "-D",
//	    "-Uflash:w:" + hexPath + ":i"
//	};

//	//In some case you need the .conf file. (not in the same path of avrdude and not in the system PATH ...)
//	if (fAvrdudeConfFile.exists() && fAvrdudeConfFile.canRead()) {
//	    options = new String[]{
//		p.toString(),
//		"-C" + confPath,
//		//"-v","-v","-v","-v",
//		"-patmega2560",
//		"-cwiring",
//		"-P" + portName,
//		"-b115200",
//		"-D", "-Uflash:w:" + hexPath + ":i"
//	    };
//	}

	return options;
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
     * TODO should be a valid path. (File exist(), isDirectory() ) TODO and
     * should containe an executable file avrdude or avrdude.exe depanding on
     * the OS.
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

}
