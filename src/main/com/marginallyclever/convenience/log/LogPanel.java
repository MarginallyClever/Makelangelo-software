package com.marginallyclever.convenience.log;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;


public class LogPanel extends JPanel implements LogListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2753297349917155256L;

	public static final int LOG_LENGTH = 5000;

	// logging
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private JList<String> logArea = new JList<String>(listModel);
	private JScrollPane logPane = new JScrollPane(logArea);

	// command line
	private JPanel textInputArea;
	private JTextField commandLineText;
	private JButton commandLineSend;

	private ArrayList<LogPanelListener> listeners = new ArrayList<LogPanelListener>();
	private ConcurrentLinkedQueue<String> inBoundQueue = new ConcurrentLinkedQueue<String>();
	
	public LogPanel() {
		Log.addListener(this);

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;

		con1.weightx = 1;
		con1.weighty = 1;
		con1.fill = GridBagConstraints.BOTH;
		con1.anchor = GridBagConstraints.NORTHWEST;
		this.add(logPane, con1);
		con1.gridy++;

		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.weightx = 1;
		con1.weighty = 0;
		this.add(getTextInputField(), con1);

		jumpToLogEnd();
	}

	private void jumpToLogEnd() {
		// did not work
		// JScrollBar vertical = logPane.getVerticalScrollBar();
		// vertical.setValue( vertical.getMaximum() );

		// works unreliably
		logArea.ensureIndexIsVisible(listModel.getSize() - 1);
	}

	private String cleanMessage(String msg) {
		msg = msg.trim();
		msg = msg.replace("\n", "<br>\n") + "\n";
		msg = msg.replace("\n\n", "\n");
		return msg;
	}

	// appends a message to the log tab and system out.
	@Override
	public void logEvent(String msg) {
		msg = cleanMessage(msg);
		if (msg.length() == 0)
			return;
		inBoundQueue.offer(msg);
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		addMessages();
		super.paint(g);
	}
	
	private void addMessages() {
		while(!inBoundQueue.isEmpty()) {
			String msg = inBoundQueue.poll();
			if(msg!=null) addMessage(msg);
		}
	}
	
	private void addMessage(String msg) {
		int listSize = listModel.getSize() - 1;
		int lastVisible = logArea.getLastVisibleIndex();
		boolean isLast = (lastVisible == listSize);

		listModel.addElement(msg);
		trimLogPanel();
		if(isLast) jumpToLogEnd();
	}

	private int trimLogPanel() {
		int removed = 0;
		while (listModel.size() >= LOG_LENGTH) {
			listModel.remove(0);
			removed++;
		}
		return removed;
	}

	private JPanel getTextInputField() {
		textInputArea = new JPanel();
		textInputArea.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridwidth = 4;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;

		commandLineText = new JTextField(0);
		commandLineText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) sendCommand();
			}
		});
		textInputArea.add(commandLineText, c);

		commandLineSend = new JButton(Translator.get("Send"));
		commandLineSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendCommand();
			}
		});
		c.gridwidth = 1;
		c.gridx = 4;
		c.weightx = 0;
		textInputArea.add(commandLineSend, c);

		/*
		 * JButton clearLog = new JButton(Translator.get("ClearLog"));
		 * clearLog.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { clearLog(); } });
		 * c.gridwidth=1; c.gridx=5; c.weightx=0; textInputArea.add(clearLog,c);
		 */

		// textInputArea.setMinimumSize(new Dimension(100,50));
		// textInputArea.setMaximumSize(new Dimension(10000,50));

		return textInputArea;
	}

	public void clearLog() {
		listModel.removeAllElements();
	}

	public void addListener(LogPanelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(LogPanelListener listener) {
		listeners.remove(listener);
	}

	public void sendCommand() {
		for (LogPanelListener a : listeners) {
			a.commandFromLogPanel(commandLineText.getText());
		}
		commandLineText.setText("");
	}

	public static void main(String[] argv) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(argv);
		Translator.start();
		
		JFrame frame = new JFrame("Log");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 400));
		frame.add(new LogPanel());
		frame.pack();
		frame.setVisible(true);
	}
}
