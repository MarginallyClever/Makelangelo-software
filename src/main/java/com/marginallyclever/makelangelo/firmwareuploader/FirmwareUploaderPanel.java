package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.donatello.select.SelectButton;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.PrintStream;

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
    private final SelectButton startM5 = new SelectButton("startM5",Translator.get("FirmwareUploaderPanel.startM5"));
	private final SelectButton startHuge = new SelectButton("startHuge",Translator.get("FirmwareUploaderPanel.startHuge"));
	private final JTextArea console = new JTextArea();


    public FirmwareUploaderPanel() {
		super(new GridBagLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		updateCOMPortList();
		console.setEditable(false);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.gridwidth=1;
		c.weighty=0;

        SelectButton refreshButton = new SelectButton("refresh", "⟳");
		refreshButton.addActionListener(e -> updateCOMPortList());

        SelectReadOnlyText help = new SelectReadOnlyText("help", Translator.get("FirmwareUploader.help"));
        help.attach(this,c);  c.gridy++;
		c.gridwidth=1;

		JPanel panel = new JPanel(new BorderLayout(2,0));
		panel.add(port.getLabel(),BorderLayout.LINE_START);
		panel.add(port.getField(),BorderLayout.CENTER);
		this.add(panel,c);
		c.gridx++;
		c.weightx=0;
		this.add(refreshButton.getButton(),c);
		c.gridy++;

		c.gridx=0;
		startM5.attach(this,c);
		c.gridy++;

		startHuge.attach(this,c);
		c.gridy++;

		c.fill = GridBagConstraints.BOTH;
		c.weighty=1;
		c.weightx=1;
		c.gridx=0;
		c.gridwidth=3;

		var scroll = new JScrollPane(console);
		scroll.setPreferredSize(new Dimension(300, 100));
		scroll.setMinimumSize(new Dimension(300, 100));
		this.add(scroll,c);

		startM5.addActionListener(e -> run(e,"firmware-m5.hex"));
		startHuge.addActionListener(e -> run(e,"firmware-huge.hex"));

	}

	private void updateCOMPortList() {
		String [] list = getListOfCOMPorts();
		port.setNewList(list);
		if(list.length==1) port.setSelectedIndex(0);
	}
	
	private String[] getListOfCOMPorts() {
		return new SerialTransportLayer().listConnections().toArray(new String[0]);
	}

	private void run(ActionEvent evt, String name) {
		String title = Translator.get("FirmwareUploaderPanel.status");

		logger.debug("setup...");
		if(port.getSelectedIndex()==-1) {
			JOptionPane.showMessageDialog(this, Translator.get("FirmwareUploaderPanel.noPortSelected"), title, JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("maybe downloading avrdude...");
		try {
			String installPath = AVRDudeDownloader.downloadAVRDude();
			firmwareUploader.setInstallPath(installPath);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.avrdudeNotDownloaded"),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("maybe downloading firmware...");
		if(!firmwareDownloader.getFirmware(name)) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.downloadFailed"),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("finding avrdude file...");
		if(!firmwareUploader.findAVRDude()) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.notFound",new String[]{"avrdude"}),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		logger.debug("finding conf file...");
		if(!firmwareUploader.findConf()) {
			JOptionPane.showMessageDialog(this,Translator.get("FirmwareUploaderPanel.notFound",new String []{"avrdude.conf"}),title,JOptionPane.ERROR_MESSAGE);
			return;
		}

		firmwareUploader.setHexPath(firmwareDownloader.getDownloadPath(name));
		startM5.setEnabled(false);
		startHuge.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		var doc = console.getDocument();
		// clear text area
		try {
			doc.remove(0, doc.getLength());
		} catch (Exception e) {
			logger.error("failed to clear console: ",e);
		}

		// save the current output stream
		var out = System.out;
		// print results to the text area
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				try {
					doc.insertString(doc.getLength(), String.valueOf((char) b), null);
				} catch (Exception e) {
					logger.error("failed to write to console: ",e);
				}
			}
		}));

		SwingWorker<Void, Void> worker = new SwingWorker<>() {
			@Override
			protected Void doInBackground() throws Exception {
				String status = Translator.get("FirmwareUploaderPanel.finished");
				int messageType = JOptionPane.PLAIN_MESSAGE;
				int result = 1;
				try {
					result = firmwareUploader.performUpdate(port.getSelectedItem());
				} catch (Exception e1) {
					logger.error("failed to run avrdude: ", e1);
					status = e1.getMessage();
					messageType = JOptionPane.ERROR_MESSAGE;
				}
				if (result != 0) {
					logger.error("upload failed.");
					status = Translator.get("FirmwareUploaderPanel.failed");
					messageType = JOptionPane.ERROR_MESSAGE;
				}
				JOptionPane.showMessageDialog(FirmwareUploaderPanel.this, status, title, messageType);
				return null;
			}

			@Override
			protected void done() {
				setCursor(Cursor.getDefaultCursor());
				startM5.setEnabled(true);
				startHuge.setEnabled(true);
				System.setOut(out);
			}
		};

		worker.execute();
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
