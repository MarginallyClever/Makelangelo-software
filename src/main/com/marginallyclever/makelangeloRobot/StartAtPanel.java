package com.marginallyclever.makelangeloRobot;

import java.awt.Component;
import javax.swing.JOptionPane;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;

public class StartAtPanel extends SelectPanel {
	public int lineNumber;
	public boolean findPreviousPenDown;
	public boolean addPenDownCommand;

	protected SelectInteger starting_line = new SelectInteger("StartAtLine",0);
	protected SelectOneOfMany comboBox;
	protected String [] optionsList;
	
	public StartAtPanel() {
		super();
		
		findPreviousPenDown = true;
		addPenDownCommand = false;
		
		this.add(starting_line);
		
		optionsList = new String [] {
			Translator.get("StartAtLastPenDown"),
			Translator.get("StartAtAddPenDown"),
			Translator.get("StartAtExactly"),
		};

		comboBox = new SelectOneOfMany(Translator.get("StartAt"),optionsList,0);

		this.add(comboBox);		
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
