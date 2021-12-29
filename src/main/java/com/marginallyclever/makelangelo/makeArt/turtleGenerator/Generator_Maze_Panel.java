package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_Maze}
 * @author Dan Royer
 *
 */
public class Generator_Maze_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider field_rows;
	private SelectSlider field_columns;
	private Generator_Maze generator;
	
	Generator_Maze_Panel(Generator_Maze generator) {
		super();
		
		this.generator = generator;

		add(field_rows = new SelectSlider("rows",Translator.get("MazeRows"),100,1,generator.getRows()));
		add(field_columns = new SelectSlider("columns",Translator.get("MazeColumns"),100,1,generator.getCols()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Maze_generation_algorithm'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		generator.setRows(field_rows.getValue());
		generator.setCols(field_columns.getValue());
		generator.generate();
	}
}
