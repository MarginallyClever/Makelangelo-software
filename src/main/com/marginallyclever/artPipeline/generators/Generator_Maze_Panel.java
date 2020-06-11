package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_Maze_Panel extends ImageGeneratorPanel {
	private SelectInteger field_rows,field_columns;
	private Generator_Maze generator;
	
	Generator_Maze_Panel(Generator_Maze generator) {
		super();
		
		this.generator = generator;

		add(field_rows = new SelectInteger(Translator.get("MazeRows"),generator.getRows()));
		add(field_columns = new SelectInteger(Translator.get("MazeColumns"),generator.getCols()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		generator.setRows(field_rows.getValue());
		generator.setCols(field_columns.getValue());
		makelangeloRobotPanel.regenerate(generator);
	}
}
