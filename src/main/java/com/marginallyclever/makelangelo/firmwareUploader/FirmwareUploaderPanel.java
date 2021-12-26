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
import javax.swing.JLabel;
import javax.swing.SwingWorker;

public class FirmwareUploaderPanel extends SelectPanel {
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
	
	public FirmwareUploaderPanel() {
		super();
		
		updateCOMPortList();
		refreshLayout();
		
		sourceAVRDude.setPathOnly();
		sourceHex.setFilter(new FileNameExtensionFilter(Translator.get("*.hex file"),"hex"));
		sourceHex.setFileHidingEnabled(false);// if this is in my .pio dir from a VSCode build ...
		refreshButton.addPropertyChangeListener((e)->{
			updateCOMPortList();
		});
		goButton.addPropertyChangeListener((e)->{
			if(AVRDudeExists()) uploadNow();
		});
		// just for logs no edition :
		selectTextAreaForAvrdudeExecLog.setEditable(false);
		checkForHexFileInCurrentWorkingDirectory();
	}
	
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
		setLayout(new FlowLayout());
		add(sourceAVRDude);
		add(sourceHex);
		add(port);
		add(refreshButton);
		add(goButton);
		
		add(selectTextAreaForAvrdudeExecLog);
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

//		String status = "Finished!";
//		int messageType = JOptionPane.PLAIN_MESSAGE;
		try {
		    // As this is a long action that can block this as to be done in a thread.
		    // and sometimes avrdude do not exit ... 
		    
		    // TODO clean the mess ... do it better ...
		    
		    class MeaningOfLifeFinder extends SwingWorker<String, Object> {
			// TODO traduction
			final String msg_firmware_upload_status = "Firmware upload status";				

			boolean resExecValueIsZero = false;

			@Override
			public String doInBackground() {
			    try {
				// TODO traduction
				final String msg_please_selecte_a_Port_ = "Please selecte a Port !";
				final String msg_please_selecte_a_hex_file_ = "Please selecte a .hex file !";
				final String msg_finished = "Finished!";
				final String msg_errors_refer_to_the_avrdudelog_s = "Errors! refer to the avrdude.log s";
				
				
				if ( port.getSelectedItem().isBlank()){
				    
				        JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_please_selecte_a_Port_, msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
				
				}else if ( sourceHex.getText().isBlank()){
				        JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog, msg_please_selecte_a_hex_file_, msg_firmware_upload_status,JOptionPane.ERROR_MESSAGE);
				
				}else{
				    
					selectTextAreaForAvrdudeExecLog.setText("");// To clearn if précédent logs
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
				//			status = e1.getMessage();
				//			messageType = JOptionPane.ERROR_MESSAGE;
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
				ignore.printStackTrace();
			    }
			}
		    }
		    
		    // running the SwingWorker ( a thread )
		    (new MeaningOfLifeFinder()).execute();
		    //			firmwareUploader.setAvrdudePath( sourceAVRDude.getText() );
		    //			 firmwareUploader.run(sourceHex.getText(),port.getSelectedItem());
		    //			
		    //status = e1.getMessage();
		    //messageType = JOptionPane.ERROR_MESSAGE;

		    //firmwareUploader.run(sourceHex.getText(),port.getSelectedItem());
		} catch (Exception e1) {
		     JOptionPane.showMessageDialog(selectTextAreaForAvrdudeExecLog,e1.getMessage(),"Firmware upload status",JOptionPane.ERROR_MESSAGE);
//		    status = e1.getMessage();
//		    messageType = JOptionPane.ERROR_MESSAGE;
		}

		//setCursor(Cursor.getDefaultCursor());
		//goButton.setEnabled(true);
		//JOptionPane.showMessageDialog(this,status,"Firmware upload status",messageType);
	}
	
	/**
	 * TO REVIEW.
	 * avrdude can be in the env path so maybe no need to find it ...
	 * @return 
	 */
	private boolean AVRDudeExists() {
	    
		FirmwareUploader.execBashCommand(new String[]{"avrdude", "--version"}, null,null);
		if ( FirmwareUploader.lastExecSucces ) {
		    return true;
		}		
	
		File f = new File(sourceAVRDude.getText());
		boolean state = f.exists(); 
		if(!state) {
			JOptionPane.showMessageDialog(this,"AVRDude not found.","Firmware upload status",JOptionPane.ERROR_MESSAGE);
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
		e.printStackTrace();
	    }
	}
}
