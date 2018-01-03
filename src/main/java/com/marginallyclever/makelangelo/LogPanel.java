package com.marginallyclever.makelangelo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

@SuppressWarnings("serial")
public class LogPanel extends JPanel implements LogListener, ActionListener, KeyListener {
	public static final int LOG_LENGTH = 5000;
	
	Translator translator;
	MakelangeloRobot robot;
	
	// logging
	private JTextPane logArea;
	private HTMLEditorKit kit;
	private HTMLDocument doc;
	private JScrollPane logPane;

	// command line
	private JPanel textInputArea;
	private JTextField commandLineText;
	private JButton commandLineSend;
	
	public LogPanel(Translator translator,MakelangeloRobot robot) {
		this.translator = translator;
		this.robot = robot;

		// log panel
		Log.addListener(this);

		logArea = new JTextPane();
		logArea.setEditable(false);
		kit = new HTMLEditorKit();
		doc = new HTMLDocument();
		logArea.setEditorKit(kit);
		logArea.setDocument(doc);
		DefaultCaret caret = (DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		logPane = new JScrollPane(logArea); 
				
		Logger logger = Logger.getLogger("");
		Handler logHandler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				// TODO Auto-generated method stub

			    if (!isLoggable(record))
			      return;
			    String message = getFormatter().format(record);
			    try {
					kit.insertHTML(doc, doc.getLength(), message, 0, 0, null);
				} catch (BadLocationException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    logArea.validate();
			}

			@Override
			public void flush() {}

			@Override
			public void close() throws SecurityException {}	
		};
		logHandler.setFormatter(new LogFormatterHTML());
		logger.addHandler(logHandler);
		


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
		
		// lastly, clear the log
		//clearLog();
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

		//System.out.print(msg);
		
		try {
			long docLen = doc.getLength();
			long caretPosition = logArea.getCaretPosition();
			
			kit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
			
			int over_length = 0;
			String startingText ="";
			if(docLen>LOG_LENGTH) {
				startingText = doc.getText(0, 1000);
				over_length = startingText.indexOf("\n")+1;
			}
			// don't let the log grow forever
			doc.remove(0, over_length);
			
			if(docLen==caretPosition) {
				logArea.setCaretPosition(doc.getLength());
			}
		} catch (BadLocationException | IOException e) {
			// FIXME failure here logs new error, causes infinite loop?
			Log.error("Logging error: "+e.getMessage());
		}
	}


	private JPanel getTextInputField() {
		textInputArea = new JPanel();
		textInputArea.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		commandLineText = new JTextField(0);
		//commandLineText.setPreferredSize(new Dimension(10, 10));
		commandLineSend = new JButton(Translator.get("Send"));
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

		commandLineText.addKeyListener(this);
		commandLineSend.addActionListener(this);

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
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * Handle the key-released event from the text field.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			sendCommand();
		}
	}
/*
	// appends a message to the log tab and system out.
	@Override
	public void logEvent(String msg) {
		// remove the 
		//if (msg.indexOf(';') != -1) msg = msg.substring(0, msg.indexOf(';'));
		msg = msg.trim();
		if(msg.length()==0) return;
		msg = msg.replace("\n", "<br>\n") + "\n";
		msg = msg.replace("\n\n", "\n");
		
		logMessages.add(msg);
		this.repaint();
	}
	
	public void paintComponent(Graphics g2) {
		try {
			long docLen = doc.getLength();
			long caretPosition = logPane.getCaretPosition();
			
			String msg;
			int i=0;
			while( (msg = logMessages.poll()) != null && i < 100) {
				kit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
				++i;
			}
			
			int overLength = 0;
			if(docLen>2000) {
				String startingText = doc.getText(0, 200);
				overLength = startingText.indexOf("\n");
			}
			// don't let the log grow forever
			doc.remove(0, overLength);
			
			if(docLen==caretPosition) {
				logPane.setCaretPosition(doc.getLength());
			}
			if(!logMessages.isEmpty()) {
				this.repaint();
			}
		} catch (Exception e) {
			// FIXME failure here logs new error, causes infinite loop?
			Log.error("Logging error: "+e.getMessage());
		}
		
		super.paintComponent(g2);
	}

	public void clearLog() {
		try {
			doc.replace(0, doc.getLength(), "", null);
			kit.insertHTML(doc, 0, "", 0, 0, null);
			//logPane.getVerticalScrollBar().setValue(logPane.getVerticalScrollBar().getMaximum());
		} catch (BadLocationException | IOException e) {

		}
	}*/
	
	// The user has done something. respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if (subject == commandLineSend) {
			sendCommand();
		}
	}

	public void sendCommand() {
		robot.sendLineToRobot(commandLineText.getText());
		commandLineText.setText("");
	}
}
