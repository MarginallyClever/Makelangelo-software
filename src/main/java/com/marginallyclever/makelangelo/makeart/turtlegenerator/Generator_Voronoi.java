package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.awt.geom.Rectangle2D;

/**
 * 1cm and 10cm grid lines
 * @author Dan Royer
 */
public class Generator_Voronoi extends TurtleGenerator {
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

		Rectangle2D bounds = myPaper.getMarginRectangle();

		Point2D [] points = new Point2D[numCells];
		for(int i=0;i<numCells;++i) {
			points[i] = new Point2D(
					Math.random()*bounds.getWidth()+bounds.getMinX(),
					Math.random()*bounds.getHeight()+bounds.getMinY());
		}

		VoronoiTesselator2 diagram = new VoronoiTesselator2(points,bounds,0.0001);

		// draw all the graph edges according to the cells.
		System.out.println("i="+diagram.getNumHulls());
		int j=0;
		for(int i=0;i<diagram.getNumHulls();++i) {
			boolean first = true;
			Polygon poly = diagram.getHull(i);
			for (Coordinate p : poly.getExteriorRing().getCoordinates()) {
				++j;
				if (first) {
					turtle.jumpTo(p.x, p.y);
					first=false;
				} else turtle.moveTo(p.x, p.y);
			}
		}
		System.out.println("j="+j);

		// draw all the cell centers
		turtle.setColor(new ColorRGB(0,0,255));
		for( Point2D p : points ) {
			turtle.jumpTo(p.x,p.y);
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
