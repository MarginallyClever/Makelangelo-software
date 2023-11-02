package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectButton;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for uploading firmware to the robot.
 *
 * @since 7.32.0?
 * @author Dan Royer
 */
public class FirmwareUploaderPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(FirmwareUploaderPanel.class);
	private final FirmwareDownloader firmwareDownloader = new FirmwareDownloader();
	private final FirmwareUploader firmwareUploader = new FirmwareUploader();
	private final SelectOneOfMany port = new SelectOneOfMany("port",Translator.get("Port"));
	private final SelectButton refreshButton = new SelectButton("refresh","⟳");
	private final SelectButton startM5 = new SelectButton("startM5",Translator.get("FirmwareUploaderPanel.startM5"));
	private final SelectButton startHuge = new SelectButton("startHuge",Translator.get("FirmwareUploaderPanel.startHuge"));
	private final SelectReadOnlyText help = new SelectReadOnlyText("help",Translator.get("FirmwareUploader.help"));


	public FirmwareUploaderPanel() {
		super(new GridBagLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		updateCOMPortList();

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
		c.gridwidth=2;
		add(connectTo,c);
		c.gridy++;
		c.gridwidth=1;
		c.weightx=1;
		c.weighty=1;
		c.anchor = GridBagConstraints.PAGE_END;
		add(startM5,c);
		c.gridx++;
		add(startHuge,c);

		refreshButton.addPropertyChangeListener(e -> updateCOMPortList());
		startM5.addPropertyChangeListener(e -> run("firmware-m5.hex"));
		startHuge.addPropertyChangeListener(e -> run("firmware-huge.hex"));
	}

	private void updateCOMPortList() {
		String [] list = getListOfCOMPorts();
		port.setNewList(list);
		if(list.length==1) port.setSelectedIndex(0);
	}
	
	private String[] getListOfCOMPorts() {
		return new SerialTransportLayer().listConnections().toArray(new String[0]);
	}

	private void run(String name) {
		String title = Translator.get("FirmwareUploaderPanel.status");
		String AVRDudePath="";


		logger.debug("maybe downloading avrdude...");
		try {
			AVRDudePath = AVRDudeDownloader.downloadAVRDude();
			firmwareUploader.setAVRDude(AVRDudePath);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.avrdudeNotDownloaded"),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("maybe downloading firmware...");
		if(!firmwareDownloader.getFirmware(name)) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.downloadFailed"),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("finding conf file...");
		if(!firmwareUploader.findConf()) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.confNotFound"),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("setup...");
		if(port.getSelectedIndex()==-1) {
			JOptionPane.showMessageDialog(this, Translator.get("FirmwareUploaderPanel.noPortSelected"), title, JOptionPane.ERROR_MESSAGE);
			return;
		}
		firmwareUploader.setHexPath(firmwareDownloader.getDownloadPath(name));
		startM5.setEnabled(false);
		startHuge.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		String status = Translator.get("FirmwareUploaderPanel.finished");
		int messageType = JOptionPane.PLAIN_MESSAGE;
		int result = 1;
		try {
			logger.debug("uploading firmware...");
			result = firmwareUploader.run(port.getSelectedItem());
		} catch (Exception e1) {
			logger.error("failed to run avrdude: ",e1);
			status = e1.getMessage();
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		setCursor(Cursor.getDefaultCursor());
		startM5.setEnabled(true);
		startHuge.setEnabled(true);

		if(result!=0) {
			logger.error("upload failed.");
			status = Translator.get("FirmwareUploaderPanel.failed");
			messageType = JOptionPane.ERROR_MESSAGE;
		}

		JOptionPane.showMessageDialog(this,status,title,messageType);
	}
	
	public static void main(String[] args) throws Exception {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(FirmwareUploaderPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new FirmwareUploaderPanel());
		frame.setPreferredSize(new Dimension(250,150));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
