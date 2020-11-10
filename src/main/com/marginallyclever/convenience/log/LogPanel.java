package com.marginallyclever.convenience.log;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

@SuppressWarnings("serial")
public class LogPanel extends JPanel implements LogListener, ActionListener, KeyListener {
	public static final int LOG_LENGTH = 5000;
	
	MakelangeloRobot robot;
	
	// logging
	private JList<String> logArea;
	private DefaultListModel<String> listModel;
	private JScrollPane logPane;

	// command line
	private JPanel textInputArea;
	private JTextField commandLineText;
	private JButton commandLineSend;
	private JButton clearLog;
	
	public LogPanel(MakelangeloRobot robot) {
		this.robot = robot;

		// log panel
		Log.addListener(this);

		listModel = new DefaultListModel<String>();
		logArea = new JList<String>(listModel);
		logPane = new JScrollPane(logArea); 

		// Now put all the parts together
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.BOTH;
		con1.anchor=GridBagConstraints.NORTHWEST;
		this.add(logPane,con1);
		con1.gridy++;


		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.weightx=1;
		con1.weighty=0;
		this.add(getTextInputField(),con1);
	}


	public void finalize() throws Throwable  {
		super.finalize();
		Log.removeListener(this);
	}

	// appends a message to the log tab and system out.
	@Override
	public void logEvent(String msg) {
		// remove the 
		//if (msg.indexOf(';') != -1) msg = msg.substring(0, msg.indexOf(';'));
		msg = msg.trim();
		msg = msg.replace("\n", "<br>\n") + "\n";
		msg = msg.replace("\n\n", "\n");
		if(msg.length()==0) return;
		
		listModel.addElement(msg);
		if(listModel.size()>LOG_LENGTH) {
			listModel.remove(0);
		}
		logArea.ensureIndexIsVisible(listModel.getSize()-1);
	}


	private JPanel getTextInputField() {
		textInputArea = new JPanel();
		textInputArea.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		commandLineText = new JTextField(0);
		//commandLineText.setPreferredSize(new Dimension(10, 10));
		commandLineSend = new JButton(Translator.get("Send"));
		clearLog = new JButton(Translator.get("Clear"));
		//commandLineSend.setHorizontalAlignment(SwingConstants.EAST);
		c.gridwidth=4;
		c.weightx=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=c.gridy=0;
		textInputArea.add(commandLineText,c);
		c.gridwidth=1;
		c.gridx=4;
		c.weightx=0;
		textInputArea.add(commandLineSend,c);
		c.gridwidth=1;
		c.gridx=5;
		c.weightx=0;
		textInputArea.add(clearLog,c);
		
		commandLineText.addKeyListener(this);
		commandLineSend.addActionListener(this);
		clearLog.addActionListener(this);

		//textInputArea.setMinimumSize(new Dimension(100,50));
		//textInputArea.setMaximumSize(new Dimension(10000,50));

		return textInputArea;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}

	/**
	 * Handle the key-pressed event from the text field.
	 */
	@Override
	public void keyPressed(KeyEvent e) {}

	/**
	 * Handle the key-released event from the text field.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			sendCommand();
		}
	}
	
	public void clearLog() {
		listModel.removeAllElements();
	}
	
	// The user has done something. respond to it.
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject == commandLineSend) {
			sendCommand();
		}
		if(subject == clearLog) {
			clearLog();
		}
	}

	public void sendCommand() {
		robot.sendLineToRobot(commandLineText.getText());
		commandLineText.setText("");
	}
}
