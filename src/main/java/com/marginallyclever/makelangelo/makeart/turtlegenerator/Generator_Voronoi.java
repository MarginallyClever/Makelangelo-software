package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_Voronoi extends TurtleGenerator {
	private static int numCells = 500;

	public Generator_Voronoi() {
		super();

		SelectInteger cells;
		add(cells = new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),getNumCells()));
		cells.addPropertyChangeListener(evt->{
			setNumCells(Math.max(1,cells.getValue()));
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

		List<VoronoiCell> points = new ArrayList<>();
		for(int i=0;i<numCells;++i) {
			points.add(new VoronoiCell(
					Math.random()*bounds.getWidth() +bounds.getMinX(),
					Math.random()*bounds.getHeight()+bounds.getMinY()));
		}

		VoronoiTesselator2 diagram = new VoronoiTesselator2();
		diagram.tessellate(points,bounds,0.0001);

		// draw all the graph edges according to the cells.
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

		// draw all the cell centers
		turtle.setColor(new ColorRGB(0,0,255));
		for( VoronoiCell p : points ) {
			turtle.jumpTo(p.center.x,p.center.y);
			turtle.forward(1);
			turtle.turn(90);
			turtle.forward(1);
			turtle.turn(90);
			turtle.forward(1);
			turtle.turn(90);
			turtle.forward(1);
		}

		turtle.penUp();

		notifyListeners(turtle);
	}
}
