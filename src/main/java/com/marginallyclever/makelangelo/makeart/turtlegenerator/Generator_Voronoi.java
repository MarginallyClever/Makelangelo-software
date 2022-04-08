package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.imageconverter.voronoi.VoronoiDiagram;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_Voronoi extends TurtleGenerator {
	private final VoronoiDiagram diagram = new VoronoiDiagram();
	private static int numCells = 500;

	@Override
	public String getName() {
		return Translator.get("Converter_Voronoi.Name");
	}

	static public int getNumCells() {
		return numCells;
	}
	static public void setNumCells(int value) {
		numCells = value;
	}
	
	@Override
	public TurtleGeneratorPanel getPanel() {
		return new Generator_Voronoi_Panel(this);
	}
	
	@Override
	public void generate() {
		Turtle turtle = new Turtle();

		diagram.initializeCells(numCells,myPaper.getMarginRectangle(),2);
		diagram.tessellate();

		// draw all the graph edges according to the cells.
		diagram.getCells().forEach(cell -> {
			boolean first=true;
			for(Vector2d p : cell.convexHull.getPoints()) {
				if(first) turtle.jumpTo(p.x,p.y);
				else turtle.moveTo(p.x,p.y);
			}
		});

		// draw all the graph edges
		turtle.setColor(new ColorRGB(255,0,0));
		diagram.getGraphEdges().forEach(edge -> {
			turtle.jumpTo(edge.x1,edge.y1);
			turtle.moveTo(edge.x2,edge.y2);
		});

		// draw all the cell centers
		turtle.setColor(new ColorRGB(0,0,255));
		diagram.getCells().forEach(cell -> {
			turtle.jumpTo(cell.centroid.x,cell.centroid.y);
			turtle.forward(1);
			turtle.turn(90);
			turtle.forward(1);
			turtle.turn(90);
			turtle.forward(1);
			turtle.turn(90);
			turtle.forward(1);
		});

		turtle.penUp();

		notifyListeners(turtle);
	}
}
