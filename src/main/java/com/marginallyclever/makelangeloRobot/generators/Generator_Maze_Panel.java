package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Generator_Maze_Panel extends ImageGeneratorPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SelectInteger field_rows,field_columns;
	Generator_Maze generator;
	
	
	Generator_Maze_Panel(Generator_Maze generator) {
		this.generator = generator;

		field_rows = new SelectInteger();
		field_rows.setValue(generator.getRows());
		field_columns = new SelectInteger();
		field_columns.setValue(generator.getCols());
		
		setLayout(new GridLayout(0, 1));
		add(new JLabel(Translator.get("MazeRows")));
		add(field_rows);
		add(new JLabel(Translator.get("MazeColumns")));
		add(field_columns);
		
		field_rows.getDocument().addDocumentListener(this);
		field_columns.getDocument().addDocumentListener(this);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		validate();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		validate();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		validate();
	}
	
	@Override
	public void validate() {
		generator.setRows(((Number)field_rows.getValue()).intValue());
		generator.setCols(((Number)field_columns.getValue()).intValue());
		makelangeloRobotPanel.regenerate(generator);
	}
}
