package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Common methods for uploading firmware to an AVR microcontroller.
 */
public class FirmwareUploader {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploader.class);
	private static final String CONF = "avrdude.conf";
	protected String installPath = "";
	protected String avrdudePath = "";
	protected String hexPath = "";
	protected String confPath = "";


	protected FirmwareUploader() {
		super();
	}

	/**
	 * Search the tree starting at the install path for the given filename.
	 * @param target the name of the file to find.
	 * @return the file if found, null otherwise.
	 */
	public File findFileRecursively(String target) {
		logger.info("Searching for "+target+" starting in "+installPath);
		Path startPath = Paths.get(installPath);
		try {
			Optional<Path> filePath = Files.walk(startPath)
					.filter(path -> path.getFileName().toString().equals(target))
					.findFirst();
			return filePath.map(Path::toFile).orElse(null);
		} catch (IOException e) {
			logger.error("An error occurred while searching for the file: ", e);
			return null;
		}
	}

	public boolean findAVRDude() {
		String path = "avrdude";
		if( OSHelper.isWindows()) path+=".exe";
		File f = findFileRecursively(path);
		if(f==null || !f.exists()) return false;
		avrdudePath = f.getAbsolutePath();
		return true;
	}

	// find avrdude.conf
	public boolean findConf() {
		File f = findFileRecursively(CONF);
		if(f==null || !f.exists()) return false;
		confPath = f.getAbsolutePath();
		return true;
	}

	protected File attemptToFindConf(int i, String filename) {
		Path p = Path.of(avrdudePath);
		logger.debug("Trying {} {}",i, p.resolve(filename));
		return p.resolve(filename).toFile();
	}

	/**
	 * @param portName
	 * @return 0 if successful.
	 * @throws Exception if the process fails.
	 */
	public int performUpdate(String portName) throws Exception {
		logger.debug("uploading firmware...");

		// setup avrdude command

		String [] options = new String[] {
				avrdudePath,
	    		"-C"+confPath,
				//"-v",
	    		"-V",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D",
				"-Uflash:w:"+hexPath+":i"
		    };
		int result = runCommand(options);
		logger.debug("update finished");

		return result;
	}

	/**
	 * @param options command line options to pass to the process.
	 * @return 0 if successful.
	 * @throws Exception if the process fails.
	 */
	protected int runCommand(String[] options) throws Exception {
		System.out.println("running command: "+String.join(" ",options));
		logger.debug("running command: {}",String.join(" ",options));
/*
		List<String> command = new ArrayList<>();
		for (String option : options) {
			command.add("\"" + option.replace("\\", "\\\\") + "\"");
		}*/

		ProcessBuilder builder = new ProcessBuilder(options);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		runBufferedReaders(p);
		return p.exitValue();
	}

	protected void runBufferedReaders(Process p) throws IOException {
		InputStreamReader stdInput = new InputStreamReader(p.getInputStream());
		InputStreamReader stdError = new InputStreamReader(p.getErrorStream());

		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		while (p.isAlive()) {
			readByte(stdInput,sb1,"output");
			readByte(stdError,sb2,"error");
		}
	}

	private void readByte(InputStreamReader isr,StringBuilder sb,String label) throws IOException {
		int c = isr.read();
		if(c == -1) return;
		System.out.print((char) c);

		if (c != '\n') {
			sb.append((char) c);
		} else {
			String s = sb.toString();
			sb.delete(0, sb.length());
			logger.debug("{}: {}", label, s);
		}
	}

	public void setHexPath(String s) {
		hexPath = s;
	}

	/**
	 * Set the path to the avrdude executable.
	 * @param path the path to the avrdude executable.
	 */
    public void setAVRDude(String path) {
		logger.debug("setting avrdude to {}",path);
		avrdudePath = path;
    }

	/**
	 * Set the path to the install directory, which should be something like ~/.makelangelo/avrdude
	 * @param path the path to the install directory.
	 */
	public void setInstallPath(String path) {
		logger.debug("setting install path to {}",path);
		this.installPath = path;
	}
}
