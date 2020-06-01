package com.marginallyclever.makelangeloRobot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.batik.ext.swing.GridBagConstants;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class StartAtPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;
	
	public int lineNumber;
	public boolean findPreviousPenDown;
	public boolean addPenDownCommand;

	protected SelectInteger starting_line = new SelectInteger(0);
	protected JComboBox<String> comboBox;
	protected String [] optionsList;
	
	public StartAtPanel() {
		findPreviousPenDown = true;
		addPenDownCommand = false;
		
		this.setLayout(new GridBagLayout());
		starting_line = new SelectInteger(0);
		
		Dimension d = starting_line.getPreferredSize();
		d.width = 100;
		starting_line.setPreferredSize(d);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		this.add(new JLabel(Translator.get("StartAtLine")), c);
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(0,5,0,0);
		c.fill = GridBagConstants.HORIZONTAL;
		this.add(starting_line, c);
		
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=2;
		c.insets = new Insets(0, 0, 0, 0);
		optionsList = new String [] {
				Translator.get("StartAtLastPenDown"),
				Translator.get("StartAtAddPenDown"),
				Translator.get("StartAtExactly"),
		};

		comboBox = new JComboBox<String>(optionsList);
		comboBox.setSelectedIndex(0);

		this.add(comboBox,c);		
	}
	
	/**
	 * run the dialog box 
	 * @param parent
	 * @return true if the dialog succeeded and the user did not cancel the operation.
	 */
	public boolean run(Component parent) {
		int result = JOptionPane.showConfirmDialog(parent, this, Translator.get("StartAt"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if(result != JOptionPane.OK_OPTION) return false;
		
		try {
			lineNumber = ((Number)starting_line.getValue()).intValue();
		} catch (Exception e) {
			return false;
		}

		int index = comboBox.getSelectedIndex();
		if(index==0) {
			findPreviousPenDown=true;
			addPenDownCommand=false;
		} else if(index==1) {
			findPreviousPenDown=false;
			addPenDownCommand=true;
		} else {
			findPreviousPenDown=false;
			addPenDownCommand=false;
		}
		
		return true;
	}
}
