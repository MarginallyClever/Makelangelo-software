package com.marginallyclever.makelangelo.firmwareUploader;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.util.PreferencesHelper;

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
	
	public FirmwareUploaderPanel() {
		super();
		
		updateCOMPortList();
		refreshLayout();
		
		sourceAVRDude.setPathOnly();
		sourceHex.setFilter(new FileNameExtensionFilter(Translator.get("*.hex file"),"hex"));
		refreshButton.addPropertyChangeListener((e)->{
			updateCOMPortList();
		});
		goButton.addPropertyChangeListener((e)->{
			if(AVRDudeExists()) uploadNow();
		});
		
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

		String status = "Finished!";
		int messageType = JOptionPane.PLAIN_MESSAGE;
		try {
			firmwareUploader.setAvrdudePath( sourceAVRDude.getText() );
			firmwareUploader.run(sourceHex.getText(),port.getSelectedItem());
		} catch (Exception e1) {
			status = e1.getMessage();
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		setCursor(Cursor.getDefaultCursor());
		goButton.setEnabled(true);
		JOptionPane.showMessageDialog(this,status,"Firmware upload status",messageType);
	}
	
	private boolean AVRDudeExists() {
		File f = new File(sourceAVRDude.getText());
		boolean state = f.exists(); 
		if(!state) {
			JOptionPane.showMessageDialog(this,"AVRDude not found.","Firmware upload status",JOptionPane.ERROR_MESSAGE);
		}
		return state;
	}
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		try {
			JFrame frame = new JFrame(FirmwareUploaderPanel.class.getSimpleName());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//frame.setPreferredSize(new Dimension(600, 400));
			frame.add(new FirmwareUploaderPanel());
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage(),"Firmware upload error",JOptionPane.ERROR_MESSAGE);
		}
	}
}
