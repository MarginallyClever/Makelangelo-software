package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.helpers.OSHelper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.util.PreferencesHelper;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * A panel for uploading firmware to the robot.
 *
 * @since 7.32.0?
 * @author Dan Royer
 */
public class FirmwareUploaderPanel extends JPanel {
	private final FirmwareUploader firmwareUploader;
	private final SelectFile sourceAVRDude;
	private final SelectFile sourceHex;
	private final SelectOneOfMany port = new SelectOneOfMany("port",Translator.get("Port"));
	private final SelectButton refreshButton = new SelectButton("refresh","⟳");
	private final SelectButton goButton = new SelectButton("start",Translator.get("Start"));
	private final SelectReadOnlyText help = new SelectReadOnlyText("help",Translator.get("FirmwareUploader.help"));

	private static String lastAVRDudePath = "";
	private static String lastHexFile = "";
	
	public FirmwareUploaderPanel() {
		super(new GridBagLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if(OSHelper.isWindows()) firmwareUploader = new FirmwareUploaderWindows();
		else if(OSHelper.isOSX()) firmwareUploader = new FirmwareUploaderOSX();
		else firmwareUploader = new FirmwareUploaderUbuntu();

		sourceAVRDude = new SelectFile("path",Translator.get("avrDude path"),firmwareUploader.getAVRDudePath());
		sourceHex = new SelectFile("file",Translator.get("*.hex file"),firmwareUploader.getAVRDudePath());

		updateCOMPortList();
		refreshLayout();

		if(lastAVRDudePath ==null || lastAVRDudePath.trim().isEmpty()) {
			lastAVRDudePath = firmwareUploader.getAVRDudePath();
		}
		if(lastHexFile==null || lastHexFile.trim().isEmpty()) {
			lastHexFile = firmwareUploader.getHexPath();
		}

		sourceAVRDude.setPathOnly();
		sourceAVRDude.setText(lastAVRDudePath);
		sourceAVRDude.addPropertyChangeListener((e)->{
			lastAVRDudePath = sourceAVRDude.getText();
		});

		sourceHex.setFilter(new FileNameExtensionFilter(Translator.get("*.hex file"),"hex"));
		sourceHex.setText(lastHexFile);
		sourceHex.addPropertyChangeListener((e)->{
			lastHexFile = sourceHex.getText();
		});

		refreshButton.addPropertyChangeListener((e)->{
			updateCOMPortList();
		});
		goButton.addPropertyChangeListener((e)->{
			if(AVRDudeExists()) uploadNow();
		});
		
		checkForHexFileInCurrentWorkingDirectory();
	}
	
	private void checkForHexFileInCurrentWorkingDirectory() {
		String path = lastHexFile;
		File folder = new File(path);
		File [] list = folder.listFiles();
		if( list == null ) return;

		for( File c : list) {
			String ext = FilenameUtils.getExtension(c.getAbsolutePath());
			if(ext.equalsIgnoreCase("hex")) {
				sourceHex.setText(c.getAbsolutePath());
				return;
			}
		}
	}

	private void refreshLayout() {
		JPanel connectTo = new JPanel(new BorderLayout());
		connectTo.add(port,BorderLayout.CENTER);
		connectTo.add(refreshButton,BorderLayout.EAST);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=0;

		add(help,c);
		c.gridy++;
		add(sourceAVRDude,c);
		c.gridy++;
		add(sourceHex,c);
		c.gridy++;
		add(connectTo,c);
		c.gridy++;
		c.weightx=1;
		c.weighty=1;
		c.anchor = GridBagConstraints.PAGE_END;
		add(goButton,c);
	}

	private void updateCOMPortList() {
		String [] list = getListOfCOMPorts();
		port.setNewList(list);
		if(list.length==1) port.setSelectedIndex(0);
	}
	
	private String[] getListOfCOMPorts() {
		return new SerialTransportLayer().listConnections().toArray(new String[0]);
	}

	private void uploadNow() {
		goButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		String status = "Finished!";
		int messageType = JOptionPane.PLAIN_MESSAGE;
		try {
			firmwareUploader.setHexPath(sourceHex.getText());
			firmwareUploader.setAvrdudePath( sourceAVRDude.getText() );
			firmwareUploader.run(port.getSelectedItem());
		} catch (Exception e1) {
			status = e1.getMessage();
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		setCursor(Cursor.getDefaultCursor());
		goButton.setEnabled(true);
		JOptionPane.showMessageDialog(this,status,"Firmware upload status",messageType);
	}
	
	private boolean AVRDudeExists() {
		boolean state = firmwareUploader.hasFoundAVRdude();
		if(!state) {
			JOptionPane.showMessageDialog(this,"AVRDude not found.","Firmware upload status",JOptionPane.ERROR_MESSAGE);
		}
		return state;
	}
	
	public static void main(String[] args) throws Exception {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(FirmwareUploaderPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new FirmwareUploaderPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
