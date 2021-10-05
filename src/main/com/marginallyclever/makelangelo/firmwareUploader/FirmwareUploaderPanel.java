package com.marginallyclever.makelangelo.firmwareUploader;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;

public class FirmwareUploaderPanel extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7101530052729740681L;
	private FirmwareUploader firmwareUploader = new FirmwareUploader();
	private SelectFile sourceAVRDude = new SelectFile(Translator.get("avrDude path"),firmwareUploader.arduinoPath);
	private SelectFile sourceHex = new SelectFile(Translator.get("*.hex file"),"");
	private SelectOneOfMany port = new SelectOneOfMany(Translator.get("Port"));
	private SelectButton refreshButton = new SelectButton(Translator.get("Refresh"));
	private SelectButton goButton = new SelectButton(Translator.get("Start")); 
	
	public FirmwareUploaderPanel() {
		super();
		
		updateCOMPortList();
		refreshLayout();
		
		refreshButton.addPropertyChangeListener((e)->{
			updateCOMPortList();
		});
		goButton.addPropertyChangeListener((e)->{
			if(AVRDudeExists()) uploadNow();
		});
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
		SerialTransportLayer serial = new SerialTransportLayer();
		String [] list = serial.listConnections();
		return list;
	}

	private void uploadNow() {
		goButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		try {
			firmwareUploader.arduinoPath = sourceAVRDude.getText();
			firmwareUploader.run(sourceHex.getText(),port.getSelectedItem());
		} catch (Exception e1) {
			setCursor(Cursor.getDefaultCursor());
			JOptionPane.showMessageDialog(this,e1.getMessage(),"Firmware upload error",JOptionPane.ERROR_MESSAGE);
		}

		setCursor(Cursor.getDefaultCursor());
		goButton.setEnabled(true);
		JOptionPane.showMessageDialog(this,"Finished!","Firmware upload status",JOptionPane.PLAIN_MESSAGE);
	}
	
	private boolean AVRDudeExists() {
		File f = new File(sourceAVRDude.getText());
		boolean state = f.exists(); 
		if(!state) {
			JOptionPane.showMessageDialog(this,"AVRDude not found.","Firmware upload error",JOptionPane.ERROR_MESSAGE);
		}
		return state;
	}
	
	public static void main(String[] args) {
		Log.start();
		Translator.start();
		try {
			JFrame frame = new JFrame("FirmwareUploaderPanel");
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
