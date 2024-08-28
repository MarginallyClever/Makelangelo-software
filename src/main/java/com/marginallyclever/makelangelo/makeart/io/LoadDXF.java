package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * @author Dan Royer
 *
 */
public class LoadDXF implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadDXF.class);
	public static final double EPSILON = 0.01;
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF R12", "dxf");
	private final Parser parser = ParserBuilder.createDefaultParser();
	private double previousX,previousY;
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
		if (in == null) throw new NullPointerException("Input stream is null");

		logger.debug("Loading...");

		// Read in the DXF file
		parser.parse(in, DXFParser.DEFAULT_ENCODING);
		
		DXFDocument doc = parser.getDocument();

		myTurtle = new Turtle();
		
		// convert each entity
		Iterator<?> layerItr = doc.getDXFLayerIterator();
		while (layerItr.hasNext()) {
			DXFLayer layer = (DXFLayer)layerItr.next();
			int color = layer.getColor();
			logger.debug("Found layer {} (color index={})", layer.getName(), color);
			
			// Some DXF layers are empty.  Only write the tool change command if there's something on this layer.
			Iterator<?> entityTypeItr = layer.getDXFEntityTypeIterator();
			if(entityTypeItr.hasNext()) {
				// ignore the color index, DXF is dumb.
				myTurtle.setColor(Color.BLACK);

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
	
	private void parseDXFLine(DXFLine entity) {
		Point start = entity.getStartPoint();
		double x = start.getX();
		double y = start.getY();
		if(Math.abs(x-previousX)>EPSILON || Math.abs(y-previousY)>EPSILON) {
			myTurtle.jumpTo(TX(x), TY(y));
		}

		Point end = entity.getEndPoint();
		double x2 = end.getX();
		double y2 = end.getY();
		myTurtle.moveTo(TX(x2),TY(y2));
		previousX = x2;
		previousY = y2;
	}

	private void parseDXFPolyline(DXFPolyline entity) {
		boolean first = true;
		int c = entity.getVertexCount();
		int count = c + (entity.isClosed()?1:0);
		DXFVertex v;
		double x,y;
		for (int j = 0; j < count; ++j) {
			v = entity.getVertex(j % c);
			x = v.getX();
			y = v.getY();
			drawPolylinePoint(x,y,first);
			first = false;
		}
	}

	private void drawPolylinePoint(double x, double y, boolean first) {
		if (first) {
			if(Math.abs(x-previousX)>EPSILON || Math.abs(y-previousY)>EPSILON) {
				myTurtle.jumpTo(TX(x),TY(y));
			}
		} else {
			myTurtle.moveTo(TX(x),TY(y));
		}
		previousX = x;
		previousY = y;
	}

	private double TX(double x) {
		return x;
	}

	private double TY(double y) {
		return y;
	}
}
