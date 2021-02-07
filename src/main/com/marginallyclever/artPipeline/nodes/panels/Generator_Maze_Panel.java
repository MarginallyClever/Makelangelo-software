package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.maze.Generator_Maze;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_Maze}
 * @author Dan Royer
 *
 */
public class Generator_Maze_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider field_rows;
	private SelectSlider field_columns;
	private Generator_Maze generator;
	
	public Generator_Maze_Panel(Generator_Maze generator) {
		super();
		
		this.generator = generator;

		add(field_rows = new SelectSlider(Translator.get("MazeRows"),100,1,generator.getRows()));
		add(field_columns = new SelectSlider(Translator.get("MazeColumns"),100,1,generator.getCols()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Maze_generation_algorithm'>Learn more</a>"));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		generator.setRows(field_rows.getValue());
		generator.setCols(field_columns.getValue());
		generator.restart();
	}
}
