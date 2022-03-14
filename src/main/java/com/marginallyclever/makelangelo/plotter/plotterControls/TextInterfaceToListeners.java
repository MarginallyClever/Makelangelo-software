package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * {@link TextInterfaceToListeners} provides a command line and a "Send" button.
 * When enter or the send button is pressed the contents of the command line are
 * sent to all registered {@link ActionListener}s
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class TextInterfaceToListeners extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(TextInterfaceToListeners.class);
	private static final long serialVersionUID = 7996257740483513358L;
	private JTextField commandLine = new JTextField(60);
	private JButton send = new JButton("Send");

	public TextInterfaceToListeners() {
		super();

		commandLine.addActionListener((e) -> sendNow());
		send.addActionListener((e) -> sendNow());

		// this.setBorder(BorderFactory.createTitledBorder(TextInterfaceToListeners.class.getSimpleName()));
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		this.add(commandLine, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		this.add(send, c);
	}

	public void sendCommand(String str) {
		notifyListeners(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, str));
	}

	public void sendNow() {
		logger.debug("User sends '{}' to the robot", getCommand());
		sendCommand(getCommand());
		commandLine.setText("");
	}

	public void setCommand(String msg) {
		commandLine.setText(msg);
	}

	public String getCommand() {
		return commandLine.getText();
	}

	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		commandLine.setEnabled(state);
		send.setEnabled(state);
	}

	// OBSERVER PATTERN

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}

	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}

	private void notifyListeners(ActionEvent e) {
		for (ActionListener a : listeners) {
			a.actionPerformed(e);
		}
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(TextInterfaceToListeners.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TextInterfaceToListeners());
		frame.pack();
		frame.setVisible(true);
	}
}
