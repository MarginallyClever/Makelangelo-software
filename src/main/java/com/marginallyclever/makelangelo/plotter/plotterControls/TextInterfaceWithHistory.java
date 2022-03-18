package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * {@link TextInterfaceWithHistory} provides a log of the two way communication
 * with a network connected device with {@link ConversationHistory} 
 * and a method to send text messages through {@link TextInterfaceToListeners}.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class TextInterfaceWithHistory extends JPanel {
	private static final long serialVersionUID = 5542831703742185676L;
	private TextInterfaceToListeners myInterface = new TextInterfaceToListeners();
	private ConversationHistory myHistory = new ConversationHistory();

	public TextInterfaceWithHistory() {
		super();

		setLayout(new GridBagLayout());

		// this.setBorder(BorderFactory.createTitledBorder(TextInterfaceWithHistory.class.getSimpleName()));
		GridBagConstraints c = new GridBagConstraints();
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		add(myHistory, c);
		myHistory.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		add(myInterface, c);

		myInterface.addActionListener((e) -> addToHistory("You", e.getActionCommand()));
		myHistory.addListSelectionListener((e) -> {
			if (e.getValueIsAdjusting())
				return;
			int i = myHistory.getSelectedIndex();
			if (i != -1)
				myInterface.setCommand(myHistory.getSelectedValue());
		});
	}

	public void addToHistory(String who, String actionCommand) {
		myHistory.addElement(who, actionCommand);
	}

	public void addActionListener(ActionListener e) {
		myInterface.addActionListener(e);
	}

	public void removeActionListener(ActionListener e) {
		myInterface.removeActionListener(e);
	}

	public String getCommand() {
		return myInterface.getCommand();
	}

	public void setCommand(String str) {
		myInterface.setCommand(str);
	}

	public void sendCommand(String str) {
		myInterface.sendCommand(str);
	}

	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		myInterface.setEnabled(state);
	}

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(TextInterfaceWithHistory.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TextInterfaceWithHistory());
		frame.pack();
		frame.setVisible(true);
	}
}
