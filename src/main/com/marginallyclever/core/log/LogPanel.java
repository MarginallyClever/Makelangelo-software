package com.marginallyclever.core.log;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.marginallyclever.core.Translator;
import com.marginallyclever.makelangelo.robot.RobotController;


public class LogPanel extends JPanel implements LogListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2753297349917155256L;

	public static final int LOG_LENGTH = 5000;
	
	protected RobotController robot;
	
	// logging
	private JList<String> logArea;
	private DefaultListModel<String> listModel;
	private JScrollPane logPane;

	// command line
	private JPanel textInputArea;
	private JTextField commandLineText;
	private JButton commandLineSend;
	
	public LogPanel() {
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

		c.gridwidth=4;
		c.weightx=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=c.gridy=0;
		
		commandLineText = new JTextField(0);
		commandLineText.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendCommand();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		textInputArea.add(commandLineText,c);
		
		commandLineSend = new JButton(Translator.get("Send"));
		commandLineSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendCommand();
			}
		});
		c.gridwidth=1;
		c.gridx=4;
		c.weightx=0;
		textInputArea.add(commandLineSend,c);

		
		/*
		JButton clearLog = new JButton(Translator.get("ClearLog"));
		clearLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});
		c.gridwidth=1;
		c.gridx=5;
		c.weightx=0;
		textInputArea.add(clearLog,c);*/
		
		//textInputArea.setMinimumSize(new Dimension(100,50));
		//textInputArea.setMaximumSize(new Dimension(10000,50));

		return textInputArea;
	}
	
	
	public void clearLog() {
		listModel.removeAllElements();
	}
	

	public void setRobot(RobotController robot) {
		this.robot = robot;
	}
	
	public void sendCommand() {
		if(robot != null) {
			robot.sendLineToRobot(commandLineText.getText());
		}
		commandLineText.setText("");
	}

	// test separate 
	public static void main(String[] args) {
		Log.start();
		Translator.start();
		JFrame frame = new JFrame("Log");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600,400));
		
		frame.add(new LogPanel());
		
		frame.pack();
		frame.setVisible(true);
	}
}
