package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

import java.beans.PropertyChangeEvent;

/**
 * Panel for {@link Generator_Voronoi}
 * @author Dan Royer
 * @since 2022-04-06
 */
public class Generator_Voronoi_Panel extends TurtleGeneratorPanel {
	private static final long serialVersionUID = 1L;
	private SelectInteger cells;
	private Generator_Voronoi generator;

	Generator_Voronoi_Panel(Generator_Voronoi generator) {
		super();
		this.generator = generator;

		add(cells = new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),generator.getNumCells()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		int count = cells.getValue();
		if(count<1) count=1;
		if(count != Generator_Voronoi.getNumCells()) {
			Generator_Voronoi.setNumCells(count);
			generator.generate();
		}
	}
}
