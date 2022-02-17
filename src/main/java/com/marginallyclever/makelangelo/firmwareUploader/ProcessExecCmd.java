/*
 */
package com.marginallyclever.makelangelo.firmwareUploader;

import com.marginallyclever.makelangelo.select.SelectTextArea;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ProcessExecCmd {

	private static final Logger logger = LoggerFactory.getLogger(ProcessExecCmd.class);

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

	/**
	 * To run on the system a command.
	 * <p>
	 * Warning: As this is a command to be executed on the user's system, always
	 * beware of user input, you should not initiate a formatting of the disk or
	 * delete files because the user has inadvertently copy paste something like
	 * "C: / Y format" in an edit control.
	 * <p>
	 * NOT IMPLEMENTED :
	 * <ul>
	 * <li>a utility class/method.</li>
	 * <li>a ProcessExecStreamGobbler interface to ease the implementation.</li>
	 * <li>a specific class for the exec results to be more generic (usable in
	 * most usage cases)</li>
	 * <li>a Timer / Kill switch (frozen command case.)</li>
	 * <li>usable for interactive commands.( not implemented to open a Stream to
	 * give chars input to the process ... )</li>
	 * </ul>
	 * <p>
	 * TO REVEIW ( as this is a way to exec commands on the user system ... )
	 * should be a least protected for security (and all methodes that using it
	 * ) ?
	 *
	 * @param cmdArray (command and arguments) used to create the process.
	 * Somethign like  <code>new String[]{"ls", "-l"}</code>.
	 * @param streamGobblerProcessIn can be null, a streamGobbler to take care
	 * of the Process outputs ( get the erros stream only if normal output and
	 * error output merged). Only to be usable in other usage than the one
	 * actualy implimented)
	 * @param execResult a GUI Element ( basicaly a JTextArea .append(..) ) to
	 * show the output of the command executed.
	 * @param modeLineByLine to take care of the command output char by char or
	 * line by line. (may have to take care of non printable char in char by
	 * char mode for some command (not implemented) )
	 * @param preAppendCommandAndAtTheEndAppendTheExitCodeValue to pre append to
	 * the possible not null execResult JTextArea like the command executed end
	 * post append posible exec exception or exitValue of the exec)
	 * @return true if the Process.exitValue() == 0 (normaly this means the
	 * command have been executed and terminated succeffuly with no errors/no
	 * exec exceptions (but the programme executed by the command have to use
	 * exitValues diffrent from 0 in the case of termination on an error ... ))
	 */
	protected static boolean execBashCommand(String[] cmdArray, ProcessExecStreamGobbler streamGobblerProcessIn, SelectTextArea execResult, boolean modeLineByLine, boolean preAppendCommandAndAtTheEndAppendTheExitCodeValue) {
		logger.debug("Running : " + processStringArrayCommandToPlainString(cmdArray));
		if (execResult != null && preAppendCommandAndAtTheEndAppendTheExitCodeValue) {
			execResult.append("Running : " + processStringArrayCommandToPlainString(cmdArray) + "\n");
		}
		try {
			Date dStar = new Date();
			Process process;
			ProcessBuilder pb = new ProcessBuilder(cmdArray);
			// If you want to "change" the environment given to the process before running the process
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
				streamGobblerProcessIn = new ProcessExecStreamGobbler(process.getInputStream(), "out") {
					// ! to be thread safe do not move this SimpleDateFormat (so each thread have one) ?
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy hh:mm:ss");

					@Override
					public void readEvent(Date d, int intread) {
						if (execResult != null && !modeLineByLine) {
							execResult.append("" + (char) intread);
							//TODO a better way to scroll the JTextArea. ... like an interface class not to depand on JTextArea.
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
			ProcessExecStreamGobbler streamGobblerProcessErr = new ProcessExecStreamGobbler(process.getErrorStream(), "err") {
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
			return ret == 0; // normally a 0 value for process.exitValue() means a success (no errors only if the commande used non 0 return value if errors).
		} catch (IOException | InterruptedException e) {
			logger.debug("Running : " + processStringArrayCommandToPlainString(cmdArray));
			logger.debug(" Exception : {}", e.getMessage(), e);
			if (execResult != null && preAppendCommandAndAtTheEndAppendTheExitCodeValue) {
				execResult.append(String.format("Exception : %s\n", e.getMessage()));
			}
			//	    e.printStackTrace();
		}
		return false; // Something go wrong. (exception to study in the logs ...) )
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
		try {
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
		} catch (Exception ignored) {// if in an applet to avoid security exception ?
			// maybe in an applet or some securitymanager restriction.
			// therefore certainly not possible Process exec therefore to be ignored. (The result array will then certainly be empty.)
		}
		return res;
	}

	public static boolean isOSWindows() {
		String OS = System.getProperty("os.name").toLowerCase();
		boolean isWindowsOS = OS.contains("win");//is this suffisant maybe one day you will have a non windows OS with "win" in is name ...
		return isWindowsOS;
	}

	public static String[] getWindowsOsCommunlyUsedEnvVarNameProgramFiles() {
		// only if Windows OS.
		// Communly used variable name for path to programFilesDir on Windows OS.
		String[] windowsOSVarNameForProgFileDir = {
			"PROGRAMFILES" //ex : "C:\Program Files"
			,
			 "PROGRAMFILES(x86)" //ex : "C:\Program Files (x86)"
			,
			 "CommunProgramFiles" //
			,
			 "CommunProgramFiles(x86)" //
		};
		return windowsOSVarNameForProgFileDir;
	}

	//
	// TODO some test case TO REVIEW 
	//
	public static void main(String[] args) {
		String cmdNameLs = "ls";
		String cmdNameDir = "dir";

		//
		//
		//
		String cmdName;
		if (isOSWindows()) {
			cmdName = cmdNameDir;
		} else {
			cmdName = cmdNameLs;
		}
		// not a usefull test : assert cmdName != null
		// ?? do not throw a exception
		// ?? have correctly match the OS ??
		//
		ArrayList<String> searchCommandFromEnvPath = searchCommandFromEnvPath(cmdName, true);

		if (searchCommandFromEnvPath.isEmpty()) {
			System.out.println(String.format("searchCommandFromEnvPath(\"%s\", true) not empty = KO\n", cmdName));
			// ko
		} else {

			System.out.println(String.format("searchCommandFromEnvPath(\"%s\", true) not empty = OK", cmdName));
			//ok  
			for (String c : searchCommandFromEnvPath) {
				System.out.println(String.format("  %s", c));
			}
		}

		//
		//
		//
		boolean isExcOk = execBashCommand(new String[]{cmdName}, null, null, true, true);
		assert isExcOk == true;

		try {
			boolean isExcNull = execBashCommand(null, null, null, true, true);
		} catch (Exception e) {
			if (e instanceof NullPointerException) {
				System.out.println("null cmd throw excetion OK");
			} else {
				System.out.println("null cmd throw excetion KO");
			}
		}

		try {
			boolean isExcNull = execBashCommand(new String[]{null}, null, null, true, true);
		} catch (Exception e) {
			if (e instanceof NullPointerException) {
				System.out.println("new String[]{null} cmd throw excetion OK");
			} else {
				System.out.println("new String[]{null} cmd throw excetion KO");
			}
		}

		try {
			boolean isExcNull = execBashCommand(new String[]{""}, null, null, true, true);
			if (isExcNull) {
				System.out.println("new String[]{\"\"} cmd exec faild KO");
			} else {
				System.out.println("new String[]{\"\"} cmd exec faild OK");
			}
		} catch (Exception e) {
			if (e instanceof NullPointerException) {
				System.out.println("new String[]{\"\"} cmd throw excetion KO");
			} else {
				System.out.println("new String[]{\"\"} cmd throw excetion KO");
			}
		}

	}
}
