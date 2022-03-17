package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyClever.convenience.ColorRGB;
import com.marginallyClever.makelangelo.turtle.Turtle;
import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.InputStream;
import java.util.*;

/**
 * @author Dan Royer
 *
 */
public class LoadDXF implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadDXF.class);
	
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF R12", "dxf");
	private final Parser parser = ParserBuilder.createDefaultParser();
	private double previousX,previousY;
	private double imageCenterX,imageCenterY;
	private Turtle myTurtle;
	
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	@Override
	public Turtle load(InputStream in) throws Exception {
		if (in == null) {
			throw new NullPointerException("Input stream is null");
		}

		logger.debug("Loading...");

		// Read in the DXF file
		parser.parse(in, DXFParser.DEFAULT_ENCODING);
		
		DXFDocument doc = parser.getDocument();
		Bounds bounds = doc.getBounds();
		imageCenterX = (bounds.getMaximumX() + bounds.getMinimumX()) / 2.0;
		imageCenterY = (bounds.getMaximumY() + bounds.getMinimumY()) / 2.0;

		myTurtle = new Turtle();
		
		// convert each entity
		Iterator<?> layerIter = doc.getDXFLayerIterator();
		while (layerIter.hasNext()) {
			DXFLayer layer = (DXFLayer)layerIter.next();
			int color = layer.getColor();
			logger.debug("Found layer {}(color index={})", layer.getName(), color);
			
			// Some DXF layers are empty.  Only write the tool change command if there's something on this layer.
			Iterator<?> entityTypeIter = layer.getDXFEntityTypeIterator();
			if(entityTypeIter.hasNext()) {
				// ignore the color index, DXF is dumb.
				myTurtle.setColor(new ColorRGB(0,0,0));

				parseLayer(layer);
			}
		}

		return myTurtle;
	}

	private void parseLayer(DXFLayer layer) {
		logger.debug("Sorting layer "+layer.getName()+" into buckets...");

		Iterator<?> entityTypeIter = layer.getDXFEntityTypeIterator();
		while (entityTypeIter.hasNext()) {
			String entityType = (String) entityTypeIter.next();
			List<?> entityList = layer.getDXFEntities(entityType);
			for (Object o : entityList) {
				DXFEntity e = (DXFEntity) o;

				switch(e.getType()) {
					case DXFConstants.ENTITY_TYPE_LINE -> parseDXFLine((DXFLine)e);
					case DXFConstants.ENTITY_TYPE_SPLINE -> parseDXFPolyline(DXFSplineConverter.toDXFPolyline((DXFSpline)e));
					case DXFConstants.ENTITY_TYPE_POLYLINE,
							DXFConstants.ENTITY_TYPE_LWPOLYLINE -> parseDXFPolyline((DXFPolyline)e);
					default -> logger.error("Unknown DXF type {}", e.getType());
				}
			}
		}
	}

	private double TX(double x) {
		return (x-imageCenterX);
	}
	
	private double TY(double y) {
		return (y-imageCenterY);
	}
	
	private void parseDXFLine(DXFLine entity) {
		Point start = entity.getStartPoint();
		Point end = entity.getEndPoint();

		double x = TX(start.getX());
		double y = TY(start.getY());
		double x2 = TX(end.getX());
		double y2 = TY(end.getY());
			
		// which end is closer to the previous point?
		double dx = previousX - x;
		double dy = previousY - y;
		double dx2 = previousX - x2;
		double dy2 = previousY - y2;
		if ( dx * dx + dy * dy < dx2 * dx2 + dy2 * dy2 ) {
			parseDXFLineEnds(x,y,x2,y2);
		} else {
			parseDXFLineEnds(x2,y2,x,y);
		}
	}
	
	private void parseDXFLineEnds(double x,double y,double x2,double y2) {
		myTurtle.jumpTo(x,y);
		myTurtle.moveTo(x2,y2);
		previousX = x2;
		previousY = y2;
	}

	private void parseDXFPolyline(DXFPolyline entity) {
		if(entity.isClosed()) {
			// only one end to care about
			parseDXFPolylineForward(entity);
		} else {
			// which end is closest to the previous (x,y)?
			int n = entity.getVertexCount()-1;
			double x = TX(entity.getVertex(0).getX());
			double y = TY(entity.getVertex(0).getY());
			double x2 = TX(entity.getVertex(n).getX());
			double y2 = TY(entity.getVertex(n).getY());

			// which end is closer to the previous (x,y) ?
			double dx = x - previousX;
			double dy = y - previousY;
			double dx2 = x2 - previousX;
			double dy2 = y2 - previousY;
			if ( dx * dx + dy * dy < dx2 * dx2 + dy2 * dy2 ) {
				// first point is closer
				parseDXFPolylineForward(entity);
			} else {
				// last point is closer
				parseDXFPolylineBackward(entity);
			}
		}
	}
	
	private void parseDXFPolylineForward(DXFPolyline entity) {
		boolean first = true;
		int c = entity.getVertexCount();
		int count = c + (entity.isClosed()?1:0);
		DXFVertex v;
		double x,y;
		for (int j = 0; j < count; ++j) {
			v = entity.getVertex(j % c);
			x = TX(v.getX());
			y = TY(v.getY());
			parsePolylineShared(x,y,first,j<count-1);
			first = false;
		}
	}
	
	private void parseDXFPolylineBackward(DXFPolyline entity) {
		boolean first = true;
		int c = entity.getVertexCount();
		int count = c + (entity.isClosed()?1:0);
		DXFVertex v;
		double x,y;
		for (int j = 0; j < count; ++j) {
			v = entity.getVertex((c*2-1-j) % c);
			x = TX(v.getX());
			y = TY(v.getY());
			parsePolylineShared(x,y,first,j<count-1);
			first = false;
		}
	}
	
	private void parsePolylineShared(double x,double y,boolean first,boolean notLast) {
		if (first) {
			myTurtle.jumpTo(x,y);
		} else {
			myTurtle.penDown();
			myTurtle.moveTo(x,y);
		}
		previousX = x;
		previousY = y;
	}
}
