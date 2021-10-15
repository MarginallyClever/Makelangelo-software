package com.marginallyclever.makelangelo.plotter.plotterControls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class TextInterfaceWithHistory extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5542831703742185676L;
	private TextInterfaceToListeners myInterface = new TextInterfaceToListeners();
	private ConversationHistory myHistory = new ConversationHistory();
	
	public TextInterfaceWithHistory() {
		super();

		setLayout(new GridBagLayout());

		//this.setBorder(BorderFactory.createTitledBorder(TextInterfaceWithHistory.class.getSimpleName()));
		GridBagConstraints c = new GridBagConstraints();
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx=1;
		c.weighty=1;
		add(myHistory,c);
		myHistory.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty=0;
		add(myInterface,c);
		
		myInterface.addActionListener((e)->addToHistory("You",e.getActionCommand()));
		myHistory.addListSelectionListener((e)->{
			if(e.getValueIsAdjusting()) return;
			int i = myHistory.getSelectedIndex();
			if(i!=-1) myInterface.setCommand(myHistory.getSelectedValue());
		});
	}

	public void addToHistory(String who,String actionCommand) {
		myHistory.addElement(who,actionCommand);
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
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}

		JFrame frame = new JFrame(TextInterfaceWithHistory.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TextInterfaceWithHistory());
		frame.pack();
		frame.setVisible(true);
	}
}
