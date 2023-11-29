package com.marginallyclever.makelangelo.plotter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog to select the starting line number.
 */
@Deprecated
public class StartAtPanel extends SelectPanel {
	private int lineNumber;
	private boolean findPreviousPenDown = true;
	private boolean addPenDownCommand = false;
	private final SelectInteger starting_line = new SelectInteger("lineNumber","StartAtLine",0);
	private final SelectOneOfMany comboBox;

	public StartAtPanel() {
		super();
		
		this.add(starting_line);

		String[] optionsList = new String[]{
				Translator.get("StartAtLastPenDown"),
				Translator.get("StartAtAddPenDown"),
				Translator.get("StartAtExactly"),
		};

		comboBox = new SelectOneOfMany("startAt",Translator.get("StartAt"), optionsList,0);

		this.add(comboBox);		
	}
	
	/**
	 * run the dialog box 
	 * @param parent
	 * @return true if the dialog succeeded and the user did not cancel the operation.
	 */
	public boolean run(Component parent) {
		int result = JOptionPane.showConfirmDialog(parent,
				this,
				Translator.get("StartAt"),
				JOptionPane.OK_CANCEL_OPTION,
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

	public int getLineNumber() {
		return lineNumber;
	}

	public boolean getFindPreviousPenDown() {
		return findPreviousPenDown;
	}

	public boolean getAddPenDownCommand() {
		return addPenDownCommand;
	}
}
