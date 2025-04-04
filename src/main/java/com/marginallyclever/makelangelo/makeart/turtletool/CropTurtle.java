package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import java.awt.geom.Rectangle2D;

public class CropTurtle {
	private static final Logger logger = LoggerFactory.getLogger(CropTurtle.class);
	private static final double EPSILON = 1e-8;
	
	public static void run(Turtle turtle,Rectangle2D.Double rectangle) {
		logger.debug("crop start @ {}", turtle.countPoints());

		Turtle result = new Turtle();
		for( var layer : turtle.strokeLayers ) {
			StrokeLayer croppedLayer = cropLayer(rectangle,layer);
			if(!croppedLayer.isEmpty()) {
				result.strokeLayers.add(croppedLayer);
			}
		}

		turtle.set(result);

		logger.debug("crop end @ {}", turtle.countPoints());
	}

	/**
	 * Use JTS to crop a layer of the turtle.
	 * @param rectangle the rectangle to crop to
	 * @param layer the layer to crop
	 * @return a new turtle with the cropped layer
	 */
	private static StrokeLayer cropLayer(Rectangle2D.Double rectangle, StrokeLayer layer) {
		StrokeLayer newLayer = new StrokeLayer(layer.getColor(),layer.getDiameter());

		GeometryFactory gf = new GeometryFactory();
		// limits we will need for rectangle
		Geometry rectanglePolygon = gf.toGeometry(new Envelope(
				rectangle.getMinX(), rectangle.getMaxX(),
				rectangle.getMinY(), rectangle.getMaxY()));

		for( var line : layer.getAllLines()) {
			LineString lineString = createLineStringFromLine2d(gf,line);
			Geometry intersection = lineString.intersection(rectanglePolygon);
			if (!intersection.isEmpty()) {
				addIntersectionToLayer(intersection,newLayer);
			}
		}
		return newLayer;
	}

	private static void addIntersectionToLayer(Geometry intersection, StrokeLayer newLayer) {
		// merge the results into the new layer.
		if (intersection instanceof LineString lineIntersection) {
			// create a new line from the coordinates
			newLayer.add(coordinatesToLine2d(lineIntersection.getCoordinates()));
		} else if (intersection instanceof MultiLineString multiLine) {
			for (int i = 0; i < multiLine.getNumGeometries(); i++) {
				Geometry geom = multiLine.getGeometryN(i);
				if (geom instanceof LineString) {
					// create a new line from the coordinates
					newLayer.add(coordinatesToLine2d(geom.getCoordinates()));
				}
			}
		}
	}

	private static LineString createLineStringFromLine2d(GeometryFactory gf, Line2d line) {
		// convert line to jts format
		Coordinate[] list = new Coordinate[line.size()];
		for(int i=0;i<line.size();++i) {
			var p = line.get(i);
			list[i] = new Coordinate(p.x, p.y);
		}
		return gf.createLineString(list);
	}

	private static Line2d coordinatesToLine2d(Coordinate[] coords) {
		Line2d line = new Line2d();
		for (Coordinate coord : coords) {
			line.add(new Point2d(coord.x, coord.y));
		}
		return line;
	}
}
