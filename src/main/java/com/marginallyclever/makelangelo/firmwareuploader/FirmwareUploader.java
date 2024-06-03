package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import com.marginallyclever.makelangelo.Translator;
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
	private final FirmwareDownloader firmwareDownloader = new FirmwareDownloader();
	protected String installPath = "";
	protected String avrdudePath = "";
	protected String hexPath = "";
	protected String confPath = "";


	protected FirmwareUploader() {
		super();
	}

	/**
	 * Search the tree starting at avrdudePath for the given filename.
	 * @param target the name of the file to find.
	 * @return the file if found, null otherwise.
	 */
	public File findFile(String target) {
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
		File f = findFile(path);
		if(!f.exists()) return false;
		avrdudePath = f.getAbsolutePath();
		return true;
	}

	// find avrdude.conf
	public boolean findConf() {
		File f = findFile("avrdude.conf");
		if(!f.exists()) return false;
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
	 * @throws Exception if the process fails.
	 */
	public void performUpdate(String firmwareName,String portName) throws Exception {
		logger.debug("setup...");
		if(portName==null || portName.isEmpty()) {
			throw new Exception(Translator.get("FirmwareUploaderPanel.noPortSelected"));
		}

		logger.debug("maybe downloading avrdude...");
		try {
			String AVRDudePath = AVRDudeDownloader.downloadAVRDude();
			setInstallPath(AVRDudePath);
		} catch(Exception e) {
			throw new Exception(Translator.get("FirmwareUploaderPanel.avrdudeNotDownloaded"));
		}

		logger.debug("maybe downloading firmware...");
		if(!firmwareDownloader.getFirmware(firmwareName)) {
			throw new Exception(Translator.get("FirmwareUploaderPanel.downloadFailed"));
		}

		logger.debug("finding avrdude file...");
		if(!findAVRDude()) {
			throw new Exception(Translator.get("FirmwareUploaderPanel.notFound",new String[]{"avrdude"}));
		}

		logger.debug("finding conf file...");
		if(!findConf()) {
			throw new Exception(Translator.get("FirmwareUploaderPanel.notFound",new String []{"avrdude.conf"}));
		}
		setHexPath(firmwareDownloader.getDownloadPath(firmwareName));

		logger.debug("uploading firmware...");

		// setup avrdude command
		String [] options = new String[] {
				avrdudePath,
	    		"-C"+confPath,
	    		"-v","-V",
	    		"-patmega2560",
	    		"-cwiring",
	    		"-P"+portName,
	    		"-b115200",
	    		"-D",
				"-Uflash:w:"+hexPath+":i"
		    };
		// run avrdude
	    int result = runCommand(options);
		if(result!=0) {
			throw new Exception(Translator.get("FirmwareUploaderPanel.failed"));
		}
		// cleanup
		logger.debug("update finished");
	}

	/**
	 * @param options command line options to pass to the process.
	 * @return 0 if successful.
	 * @throws Exception if the process fails.
	 */
	protected int runCommand(String[] options) throws Exception {
		System.out.println("running command: "+String.join(" ",options));
		logger.debug("running command: {}",String.join(" ",options));

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

    public void setAVRDude(String avrDudePath) {
		logger.debug("setting avrdude to {}",avrDudePath);
		avrdudePath = avrDudePath;
    }

	public void setInstallPath(String avrDudePath) {
		logger.debug("setting install path to {}",avrDudePath);
		installPath = avrDudePath;
	}
}
