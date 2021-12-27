package com.marginallyclever.makelangelo.firmwareUploader;

import com.marginallyclever.communications.serial.SerialTransportLayer;
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
 * A Panel to do Firmware Update (via serial port on a USB connection).
 * 
 * using avrdude path, a .hex file path (binary firmware file), a COM port (serial port (USB)).
 * <p>
 * TODO user input vérification ( files exist (and redable) , valid serial COM port selected )
 * TODO GUI 
 * 
 * Minimal size
 * Run exec command TextArea in a JScroolPane.
 * 
 * Window resize :
 * 
 * <p>
 * User case
 * 
 * ? if no avrdude found ? url to get it / install it ? or to get ArduinoIDE ( that embed avrdude) or XLoader ...
 * 
 * 
 * 
 */
public class FirmwareUploaderPanel extends 
	//	SelectPanel // PPAC37 : Sorry, I can't yet tame the SelectPanel to have a well-presented SelecteTextArea in it.
	JPanel 
{
	
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderPanel.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 7101530052729740681L;
	
	private FirmwareUploader firmwareUploader = new FirmwareUploader();
	
	private SelectFile sourceAVRDude = new SelectFile("path",Translator.get("avrDude path"),firmwareUploader.getAvrdudePath());
	
	private SelectFile sourceHex = new SelectFile("file",Translator.get("*.hex file"),"");
	
	private SelectOneOfMany port = new SelectOneOfMany("port",Translator.get("Port"));
	private SelectButton refreshButton = new SelectButton("refresh",Translator.get("Refresh"));
	
	private SelectButton goButton = new SelectButton("start",Translator.get("Start")); 
	
	private SelectTextArea selectTextAreaForAvrdudeExecLog = new SelectTextArea("avrdude_logs",Translator.get("avrdude.logs"),""); 
	
	final String msg_firmware_upload_status = "Firmware upload status"; // TODO traduction
	
	public FirmwareUploaderPanel() {
		super();
		
		updateCOMPortList();
		refreshLayout();
		
		sourceAVRDude.setPathOnly();
		sourceHex.setFilter(new FileNameExtensionFilter(Translator.get("*.hex file"),"hex"));
		sourceHex.setFileHidingEnabled(false);// if this is in my .pio (hidden dir) from a VSCode build ...
		refreshButton.addPropertyChangeListener((e)->{
			updateCOMPortList();
		});
		goButton.addPropertyChangeListener((e)->{
			if(AVRDudeExists()) uploadNow();
		});
		// just for logs, no edition :
		selectTextAreaForAvrdudeExecLog.setEditable(false);
		
		checkForHexFileInCurrentWorkingDirectory();
	}
	
	/**
	 * TODO user home dir ?
	 */
	private void checkForHexFileInCurrentWorkingDirectory() {
		String path = FileAccess.getWorkingDirectory();
		File folder = new File(path);
		File [] contents = folder.listFiles();
		for( File c : contents ) {
			String ext = FilenameUtils.getExtension(c.getAbsolutePath());
			if(ext.equalsIgnoreCase("hex")) {
				sourceHex.setText(c.getAbsolutePath());
				return;
			}
		}
	}

	private void refreshLayout() {
		setLayout(new BorderLayout(0, 0));

		SelectPanel top = new SelectPanel();
		top.setLayout(new FlowLayout());
		top.add(sourceAVRDude);
		top.add(sourceHex);
		top.add(port);
		top.add(refreshButton);
		top.add(goButton);		
		add(top, BorderLayout.NORTH);
		add(selectTextAreaForAvrdudeExecLog, BorderLayout.CENTER);

	}

	private void updateCOMPortList() {
		String [] list = getListOfCOMPorts();
		port.setNewList(list);
	}
	
	private String[] getListOfCOMPorts() {
		return SerialTransportLayer.listConnections();
	}

	private void uploadNow() {
		goButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		firmwareUploader.setAvrdudePath( sourceAVRDude.getText() );		    

		try {
		    // As this is a long action that can block this as to be done in a thread.
		    // and sometimes avrdude do not exit ... 
		    // TODO do it better ...
		    
		    class RunExecAvrDudeProcess extends SwingWorker<String, Object> {
			
			boolean resExecValueIsZero = false;

			@Override
			public String doInBackground() {
			    try {
				// TODO traduction
				final String msg_please_selecte_a_Port_ = "Please selecte a port"+/*port.getTexte()+*/"!";
				final String msg_please_selecte_a_hex_file_ = "Please selecte an existing redable firmware binary (.hex) file!";
				final String msg_finished = "Finished!";
				final String msg_errors_refer_to_the_avrdudelog_s = "Errors! refer to the avrdude.log s";
				
				
				if ( port.getSelectedItem() == null || port.getSelectedItem().isBlank()){
				    
				        JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_please_selecte_a_Port_, msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
				
				}else if ( sourceHex.getText().isBlank() || ! new File(sourceHex.getText()).canRead()){
				        JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_please_selecte_a_hex_file_, msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
				
				}else{
				    
					selectTextAreaForAvrdudeExecLog.setText("");// To empty
					// TODO in the case the port was wrong or nothing was connected avrdude may not finish ... waiting for ?
					// So a timeout have to by added
					String resExec = firmwareUploader.run(sourceHex.getText(), port.getSelectedItem(), selectTextAreaForAvrdudeExecLog);
					// TODO a better way to get the exec return code (badly done so if there is multiple concurent exec can be anyone result ...)
					if ( firmwareUploader.lastExecSucces  ) {
					    JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_finished, msg_firmware_upload_status,JOptionPane.PLAIN_MESSAGE);
					}else{
					    JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_errors_refer_to_the_avrdudelog_s, msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
					}
				}
			    } catch (Exception e1) {
				JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, e1.getMessage(), msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
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
		    
		    // running the SwingWorker ( a thread )
		    (new RunExecAvrDudeProcess()).execute();
		    //			firmwareUploader.setAvrdudePath( sourceAVRDude.getText() );
		    //			 firmwareUploader.run(sourceHex.getText(),port.getSelectedItem());
		} catch (Exception e1) {
		     JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog,e1.getMessage(),msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
		}

		//setCursor(Cursor.getDefaultCursor());
		//goButton.setEnabled(true);
		//JOptionPane.showMessageDialog(this,status,msg_firmware_upload_status,messageType);
	}
	
	/**
	 * TO REVIEW.
	 * avrdude can be in the env path so maybe no need to find it ...
	 * @return 
	 */
	private boolean AVRDudeExists() {
	    
	  	FirmwareUploader.execBashCommand(new String[]{sourceAVRDude.getText().trim()/*"avrdude"*/, "-?"}, null,null,true);
		if ( FirmwareUploader.lastExecSucces ) {
		    return true;
		}		
	
		File f = new File(sourceAVRDude.getText());
		boolean state = f.exists(); 
		if(!state) {
		    // TODO Review ! Yes or NO : 
		    String msg = "<html><body>"+f.getAbsolutePath()+"<br>"
			    + "AVRDude not found.<br>"
			    + " Select a valide avrdude executable file.<br>"
			    + " (you can get/install avrdude on your system from ??? "
			     // TODO ? url to github wiki how to get / install avrdude.
			    + "or install ArduinoIDE "
			    + "<a href=\"https://www.arduino.cc/en/software\">https://www.arduino.cc/en/software<a> "
			    + "or XLoader that embed it)</body></html>";//Translator.get("AboutAvrdude");
		    JTextComponent createHyperlinkListenableJEditorPane = /*DialogAbout.*/createHyperlinkListenableJEditorPane(msg);//
		    JOptionPane.showMessageDialog(this, createHyperlinkListenableJEditorPane, msg_firmware_upload_status, JOptionPane.ERROR_MESSAGE);
		    //JOptionPane.showMessageDialog(this,"AVRDude not found.",msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);		    
		}
		return state;
	}
	
	public static void main(String[] args) {
	    try{
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(FirmwareUploaderPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new FirmwareUploaderPanel());
		frame.pack();
		frame.setVisible(true);
	    }catch ( Exception e){
		logger.error("Failed to upload firmware {}", e.getMessage(), e);// ?	
		//e.printStackTrace();
	    }
	}
	
	/**
	 * A Cut and past (fonctionnaly the same code (except lamda expression to the inner class methode overide) ) from DialogAbout.createHyperlinkListenableJEditorPane(...).
	 *
	 * TODO do i keep that or do i use the one in DialogAbout (modified as public static), for code factoring ?
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
