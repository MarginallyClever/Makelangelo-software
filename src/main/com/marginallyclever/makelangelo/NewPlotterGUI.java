package com.marginallyclever.makelangelo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.core.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class NewPlotterGUI extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5021459944436828726L;
	
	private String myChoice="";

	// hardware model choice
	public NewPlotterGUI() {
		JPanel panel = this;
		setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		GridBagConstraints d = new GridBagConstraints();
		// the panes for the selected machine configuration
		d.fill = GridBagConstraints.BOTH;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.weighty = 0;

		JLabel modelLabel = new JLabel(Translator.get("HardwareVersion"));
		modelLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		panel.add(modelLabel, d);

		ArrayList<String> availableHardwareVersions = new ArrayList<String>();
		ServiceLoader<Plotter> knownHardware = ServiceLoader.load(Plotter.class);
		Iterator<Plotter> i = knownHardware.iterator();
		while (i.hasNext()) {
			Plotter hw = i.next();
			// Set the visible name of each machine type.
			availableHardwareVersions.add(hw.getName());
		}

		d.gridx = 1;
		d.gridwidth = 2;
		String [] names = (String[])availableHardwareVersions.toArray(new String[1]);
		JComboBox<String> hardwareVersionChoices = new JComboBox<String>(names);
		panel.add(hardwareVersionChoices, d);
		
		hardwareVersionChoices.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myChoice = availableHardwareVersions.get(hardwareVersionChoices.getSelectedIndex());
			}
		});
		
		// set the default after the action listener to the listener fires once.
		hardwareVersionChoices.setSelectedIndex(0);		
	}

	public String getMyChoice() {
		return myChoice;
	}

	public Plotter getNewPlotter() throws Exception {
		ServiceLoader<Plotter> knownHardware = ServiceLoader.load(Plotter.class);
		Iterator<Plotter> i = knownHardware.iterator();
		while (i.hasNext()) {
			Plotter hw = i.next();
			// Compare to the visible name of each machine type.
			if(hw.getName().contentEquals(myChoice)) {
				return hw.getClass().getDeclaredConstructor().newInstance();
			}
		}

		return null;
	}
}
