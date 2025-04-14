package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectInteger;
import com.marginallyclever.donatello.select.SelectRandomSeed;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_Voronoi extends TurtleGenerator {
	private static int numCells = 500;
	private static boolean showCenters = false;
	private static int seed=0;
	private static final Random random = new Random();

	public Generator_Voronoi() {
		super();

		SelectRandomSeed selectRandomSeed = new SelectRandomSeed("randomSeed",Translator.get("Generator.randomSeed"),seed);
		add(selectRandomSeed);
		selectRandomSeed.addSelectListener((evt)->{
			seed = (int)evt.getNewValue();
			random.setSeed(seed);
			generate();
		});

		SelectInteger cells;
		add(cells = new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),getNumCells()));
		cells.addSelectListener(evt->{
			setNumCells(Math.max(1,cells.getValue()));
			generate();
		});

		SelectBoolean showCenterChoice;
		add(showCenterChoice = new SelectBoolean("showCenters",Translator.get("Converter_Voronoi.ShowCenters"),false));
		showCenterChoice.addSelectListener(evt->{
			showCenters = showCenterChoice.isSelected();
			generate();
		});

	}

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
	public void generate() {
		Turtle turtle = new Turtle();

		Rectangle2D bounds = myPaper.getMarginRectangle();
		List<VoronoiCell> points = seedRandomPoints(bounds);
		// generate the voronoi diagram
		VoronoiTesselator2 diagram = new VoronoiTesselator2();
		diagram.tessellate(points,bounds,0.0001);

		drawGraphEdges(turtle,diagram);
		if(showCenters) drawCellCenters(turtle,points);
		turtle.penUp();

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
	}

	// seed random points on the paper.
	private List<VoronoiCell> seedRandomPoints(Rectangle2D bounds) {
		List<VoronoiCell> points = new ArrayList<>();
		for(int i=0;i<numCells;++i) {
			points.add(new VoronoiCell(
					random.nextDouble()*bounds.getWidth()  + bounds.getMinX(),
					random.nextDouble()*bounds.getHeight() + bounds.getMinY()));
		}
		return points;
	}

	// draw all the graph edges according to the cells.
	private void drawGraphEdges(Turtle turtle, VoronoiTesselator2 diagram) {
		for(int i=0;i<diagram.getNumHulls();++i) {
			boolean first = true;
			Polygon poly = diagram.getHull(i);
			for (Coordinate p : poly.getExteriorRing().getCoordinates()) {
				if (first) {
					turtle.jumpTo(p.x, p.y);
					first=false;
				} else turtle.moveTo(p.x, p.y);
			}
		}
	}

	private void drawCellCenters(Turtle turtle, List<VoronoiCell> points) {
		// draw all the cell centers
		turtle.setStroke(Color.BLUE);

		for( VoronoiCell p : points ) {
			// jump to corner
			turtle.jumpTo(p.center.x-0.5,p.center.y-0.5);
			// box
			for(int i=0;i<4;++i) {
				turtle.forward(1);
				turtle.turn(90);
			}
			// point in center
			turtle.jumpTo(p.center.x,p.center.y);
			turtle.forward(0.1);
		}
	}
}
