package com.marginallyclever.makelangelo.firmwareUploader;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import com.marginallyclever.util.PreferencesHelper;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Panel to do Firmware Update (via a serial port on a USB connection) from a
 * binary firmware file (.hex) with the commmand avrdude.
 * <p>
 * using avrdude path, a .hex file path (binary firmware file), a COM port
 * (serial port (USB)).
 * <p>
 * TODO user input vérification ( files exist (and redable) , valid serial COM
 * port selected )
 * <p>
 * TODO Minimal size ! for window resize.
 *
 * <p>
 * User case :<br>
 * ? if no avrdude found ? url to get it / install it ? or to get ArduinoIDE (
 * that embed avrdude) or XLoader ...
 * <ul>
 * <li><a href="http://savannah.nongnu.org/projects/avrdude">http://savannah.nongnu.org/projects/avrdude/</a></li>
 * <li><a href="https://www.arduino.cc/en/software">https://www.arduino.cc/en/software</a></li>
 * </ul>
 * <p>
 * ? border for fiel with values not ok ... ?<br>
 * jPanel1.setBorder(javax.swing.BorderFactory.createMatteBorder(5, 5, 5, 5, new
 * java.awt.Color(255, 204, 51)));
 */
public class FirmwareUploaderPanel extends	SelectPanel {

    /**
     * a specific logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderPanel.class);

    /**
     * For serialisation need. TODO check if this is usefull as it look to me
     * there is no serialisation used.
     */
    private static final long serialVersionUID = 7101530052729740681L;

    /**
     * The class that provides the functional needed methods implementation. (to
     * disociate the GUI of the core function. an instance of firmwareUploader
     * must be able to be called and do the job without a graphical interface.)
     */
    private FirmwareUploader firmwareUploader = new FirmwareUploader();

    /**
     * User interface to select/change the avrdude used.
     *
     * Should be automaticaly filed with the return of ...
     */
    private SelectFile sourceAVRDude = new SelectFile("path", Translator.get("FirmwareUploaderPanel.avrDude path"), firmwareUploader.getAvrdudePath());

    /**
     * User interface to select/change the binary firmware file (.hex) to use.
     *
     * Sould be a valid readable file.
     *
     * TODO ? file size check. As this is a firmware the .hex is not a bit to bit the flash size ( as for a .bin), but normaly the size of the
     * file have to be ??? less or equal to ?2x 512Ko or ? depending of the
     * maximum flash capacity of the microchip. ( basicaly if we got a 10 Mb file for a atmega2560 (flash size of ?512KB )  there is something wrong ... )
     */
    private SelectFile sourceHex = new SelectFile("file", Translator.get("FirmwareUploaderPanel.HexFile"), "");

    /**
     * User interface to select/change the Serial COM port to use. Can be
     * refresh. Sould be filed with available COM Port.
     */
    private SelectOneOfMany port = new SelectOneOfMany("port", Translator.get("FirmwareUploaderPanel.Port"));

    /**
     * User interface to refresh the port aivalable.
     *
     * Sould be groupe with the SelectOneOfMany port in an unique class.
     */
    //private SelectButton refreshButton = new SelectButton("refresh", Translator.get("FirmwareUploaderPanel.Refresh"));
    private ButtonIcon portRefreshJButtonIcon = new ButtonIcon( "FirmwareUploaderPanel.Refresh", "/images/arrow_refresh.png");

    /**
     * User interface to run the firmware update process.
     *
     * ( that use as input the sourceAVRDude, sourceHex,port from the user)
     *
     * All the input should be check ( files exist ( canExecut for
     * sourceAVRDude, canRead for sourceHex), available for port))
     */
    private SelectButton goButton = new SelectButton("upload", Translator.get("FirmwareUploaderPanel.Upload"));

    /**
     * A JTextArea in a JScroolPane to have the output of the execution of
     * avrdude.
     *
     * Should not be editable.
     *
     * Should be automatiquely scrool to the last line/char added.
     */
    private SelectTextArea selectTextAreaForAvrdudeExecLog = new SelectTextArea("avrdude_logs", Translator.get("FirmwareUploaderPanel.avrdude.logs"), "");

    /**
     * The title for all JDialogueMessage to inform the user. of the
     * state/validity of the inputs (sourceAVRDude, sourceHex, port valid or
     * not) of execution ( avrdude not found, avrdude exit code error, avrdude
     * exit code success )
     *
     * TODO : Warning: using a firmware (binary file) that is not suitable for
     * your machine could make it unusable. Please accept the risks beyond our
     * responsibility and choose your file carefully.
     */
    final String msg_firmware_upload_status = Translator.get("FirmwareUploaderPanel.FirmwareUploadStatus");

    public FirmwareUploaderPanel() {
	super();
	// populating the port select
	updateCOMPortList();
	refreshLayout();
	// preparing and populating the avrdude source path
	sourceAVRDude.setPathOnly();
	sourceHex.setFilter(new FileNameExtensionFilter(Translator.get("FirmwareUploaderPanel.FileExtFilterDesc.HexFile"), "hex"));
	sourceHex.setFileHidingEnabled(false);// if this is in my .pio (hidden dir on linux start with a '.') from a VSCode build ...
	// implementing the port select refresh content
	//	refreshButton.addPropertyChangeListener((e) -> {
	//	    updateCOMPortList();
	//	});
	portRefreshJButtonIcon.addActionListener(e -> {
		    updateCOMPortList();
			});

	// implementing the "start" button to run the avrdude command
	goButton.addPropertyChangeListener((e) -> {
	    if (AVRDudeExists()) {
		uploadNow();
	    }
	});
	// just for logs, no edition :
	selectTextAreaForAvrdudeExecLog.setEditable(false);
	// 
	checkForHexFileInCurrentWorkingDirectory();
    }

    /**
     * To automaticaly populate the hex source file JTextField. ... the first of the .hex
     * files found in the currend PWD/"." dir.
     *
     * TODO user home dir ? download dir ?
     */
    private void checkForHexFileInCurrentWorkingDirectory() {
	String path = FileAccess.getWorkingDirectory();
	File folder = new File(path);
	File[] contents = folder.listFiles();
	for (File c : contents) {
	    String ext = FilenameUtils.getExtension(c.getAbsolutePath());
	    if (ext.equalsIgnoreCase("hex")) {
		sourceHex.setText(c.getAbsolutePath());
		return;
	    }
	}
    }

    /**
     * panel content initialisation. (adding all this sub JComponants for the
     * GUI)
     */
    private void refreshLayout() {

	//setLayout(new FlowLayout()); // If you use that, the Select* will not resize when the window is resize.
	add(sourceAVRDude);

	add(sourceHex);

	JPanel jpanelGroupPort = new JPanel(new BorderLayout());
	
	//TODO put this in one "line" in the GUI ! ( need a new kind of SelectOneOfMany that avec a refresh button ? or simply use a Jpanel to combinat ?)
	//add(port);
	////	add(refreshButton);
	//add(refresh);
	
	jpanelGroupPort.add(port,BorderLayout.CENTER);
	jpanelGroupPort.add(portRefreshJButtonIcon,BorderLayout.EAST);
	add(jpanelGroupPort);
	

	add(goButton);

	add(selectTextAreaForAvrdudeExecLog);

    }

    private void updateCOMPortList() {
	String[] list = getListOfCOMPorts();
	port.setNewList(list);
    }

  private String[] getListOfCOMPorts() {
		return new SerialTransportLayer().listConnections().toArray(new String[0]);
    }

    /**
     * As this is a long action that can block this as to be done in a thread.
     * and sometimes avrdude do not exit ...
     *
     * TODO do it better ... / more resiliant. Not so rude to read.
     */
    private void uploadNow() {
	// disabling the "start" button to avoid concurent run (a Serial com port can only be used by one application and have to be free/liberated)
	goButton.setEnabled(false);
	// GUI interaction to notify the yuser ther is some computings ...
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	// preparing the firmwareUploader instance to give it the inputs to use.
	firmwareUploader.setAvrdudePath(sourceAVRDude.getText());

	try {
	    // Threaded class to do the work
	    class RunExecAvrDudeProcess extends SwingWorker<String, Object> {

		boolean resExecValueIsZero = false;

		@Override
		public String doInBackground() {
		    try {
			// TODO all checks sould be done in the "functionnals needs" implementation class (FirmwareUploader) ! 
			// but it may be easyer to do it in here also, to get a more user friendly GUI interface. not bound to the basic "functionnals needs".
			// 
			// we only need a way to distinc the inputs parameters in defaut from the exception thrown to help the user (? border red the field)
			// TODO traduction TODO simplyfy the message, TODO move that in a location so it can be modifield easyly
			final String msg_please_selecte_a_Port_ = "Please selecte a port" +/*port.getTexte()+*/ "!";
			final String msg_please_selecte_a_hex_file_ = "Please selecte an existing redable firmware (*.hex) file!";
			final String msg_finished = "Finished!";
			final String msg_errors_refer_to_the_avrdudelog_s = "Errors! refer to the avrdude.log s";

			if (port.getSelectedItem() == null || port.getSelectedItem().isBlank()) {
			    //port. // GUI red border ???
			    JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_please_selecte_a_Port_, msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);

			} else if (sourceHex.getText().isBlank() || !new File(sourceHex.getText()).canRead()) {
			    JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_please_selecte_a_hex_file_, msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);

			} else {

			    selectTextAreaForAvrdudeExecLog.setText("");// To empty
			    // TODO in the case the port was wrong or nothing was connected avrdude may not finish ... waiting for ?
			    // So a timeout have to by added
			    boolean resExec = firmwareUploader.run(sourceHex.getText(), port.getSelectedItem(), selectTextAreaForAvrdudeExecLog);
			    // TODO a better way to get the exec return code (badly done so if there is multiple concurent exec can be anyone result ...)
			    if (resExec) {
				JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_finished, msg_firmware_upload_status, JOptionPane.PLAIN_MESSAGE);
			    } else {
				JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_errors_refer_to_the_avrdudelog_s, msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);
			    }
			}
		    } catch (Exception e1) {
			JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, e1.getMessage(), msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);
		    }
		    return "done ?";// result normaly later availabel via get() in the done() methode ... but not used in this imlementation.
		}

		@Override
		protected void done() {
		    try {
			setCursor(Cursor.getDefaultCursor());
			goButton.setEnabled(true);

			//label.setText(get());
		    } catch (Exception ignore) {
			logger.error("Failed to upload firmware {}", sourceHex.getText(), ignore);//?				
			//ignore.printStackTrace();
		    }
		}
	    }

	    // instanciation and running the SwingWorker ( the threaded class to do the job )
	    (new RunExecAvrDudeProcess()).execute();

	} catch (Exception e1) {
	    JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, e1.getMessage(), msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);
	}

    }

    /**
     * avrdude can be in the env path so maybe no need to find it ...
     *
     * @return
     */
    private boolean AVRDudeExists() {

	//  Warning: As this is a command to be executed on the user's system, always beware of user input, you should not initiate a formatting of the disk or delete files because the user has inadvertently copy paste something like "C: / Y format" in an edit control.
	boolean resExec = ProcessExecCmd.execBashCommand(new String[]{FirmwareUploader.getAvrdudeCommandNameDependingOfTheOs(), "-?"}, null, null, true, true);
	if (resExec) {
	    return resExec;
	}

	File f = new File(sourceAVRDude.getText());
	boolean state = f.exists() && f.canExecute() && f.isFile();
	if (!state) {
	    // TODO Review ! Yes or NO : 
	    String msg = "<html><body>" + f.getAbsolutePath() + "<br>"
		    + "AVRDude not found.<br>"
		    + " Select a valide avrdude executable file.<br>"
		    + " (you can get/install avrdude on your system from <a href=\"http://savannah.nongnu.org/projects/avrdude/\">http://savannah.nongnu.org/projects/avrdude/</a> "
		    // TODO ? url to github wiki how to get / install avrdude.
		    + "or install ArduinoIDE "
		    + "<a href=\"https://www.arduino.cc/en/software\">https://www.arduino.cc/en/software<a> "
		    + "or XLoader that embed it)</body></html>";//Translator.get("AboutAvrdude");
	    JTextComponent createHyperlinkListenableJEditorPane = /*DialogAbout.*/ createHyperlinkListenableJEditorPane(msg);//
	    JOptionPane.showMessageDialog(this, createHyperlinkListenableJEditorPane, msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);
	    //JOptionPane.showMessageDialog(this,"AVRDude not found.",msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);		    

	}
	return state;
    }

    public static void main(String[] args) {
	try {
	    PreferencesHelper.start();
	    CommandLineOptions.setFromMain(args);
	    Translator.start();

	    JFrame frame = new JFrame(FirmwareUploaderPanel.class.getSimpleName());
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    //frame.setPreferredSize(new Dimension(600, 400));
	    frame.add(new FirmwareUploaderPanel());
	    frame.pack();
	    frame.setVisible(true);
	} catch (Exception e) {
	    logger.error("Failed to upload firmware {}", e.getMessage(), e);// ?	
	    //e.printStackTrace();
	}
    }

    /**
     * A Cut and past (fonctionnaly the same code (except lamda expression to
     * the inner class methode overide) ) from
     * DialogAbout.createHyperlinkListenableJEditorPane(...).
     *
     * TODO do i keep that or do i use the one in DialogAbout (modified as
     * public static), for code factoring ?
     * 
     * Or do i remove this message. (lets the user find it by himself with no leads )
     *
     * @param html
     * @return
     */
    private JTextComponent createHyperlinkListenableJEditorPane(String html) {
	final JEditorPane bottomText = new JEditorPane();
	bottomText.setContentType("text/html");
	bottomText.setEditable(false);
	bottomText.setText(html);
	bottomText.setOpaque(false);
	final HyperlinkListener hyperlinkListener = (HyperlinkEvent hyperlinkEvent) -> {
	    if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		if (Desktop.isDesktopSupported()) {
		    try {
			URI u = hyperlinkEvent.getURL().toURI();
			Desktop.getDesktop().browse(u);
		    } catch (IOException | URISyntaxException e) {
			logger.error("Failed to open the browser to the url", e);
		    }
		}

	    }
	};
	bottomText.addHyperlinkListener(hyperlinkListener);
	return bottomText;
    }
}
