package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
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
import java.io.InputStream;
import java.util.*;

/**
 * @author Dan Royer
 *
 */
public class LoadDXF implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadDXF.class);
	
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF R12", "dxf");
	private Parser parser = ParserBuilder.createDefaultParser();
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

	/**
	 * Put every entity into a bucket.
	 * @param doc
	 * @param grid
	 * @param groups
	 */
	private void sortEntitiesIntoBucketsAndGroups(DXFDocument doc,DXFLayer layer,DXFBucketGrid grid,List<DXFGroup> groups) {
		//logger.debug("Sorting layer "+layer.getName()+" into buckets...");

		Iterator<?> entityTypeIter = layer.getDXFEntityTypeIterator();
		while (entityTypeIter.hasNext()) {
			String entityType = (String)entityTypeIter.next();
			List<?> entityList = layer.getDXFEntities(entityType);
			for (Object o : entityList) {
				DXFEntity e = (DXFEntity) o;
				DXFBucketEntity be = new DXFBucketEntity(e);

				if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
					DXFLine line = (DXFLine) e;
					grid.addEntity(be, line.getStartPoint());
					grid.addEntity(be, line.getEndPoint());
					continue;
				}
				if (e.getType().equals(DXFConstants.ENTITY_TYPE_CIRCLE)) {
					DXFCircle circle = (DXFCircle) e;
					double r = circle.getRadius();
					Point center = circle.getCenterPoint();
					double cx = center.getX();
					double cy = center.getY();

					Point a = new Point(cx + r, cy, 0);
					Point b = new Point();
					for (double i = 1; i <= 40; ++i) {  // hard coded 40?  gross!
						double v = (Math.PI * 2.0) * (i / 40.0);
						double s = r * Math.sin(v);
						double c = r * Math.cos(v);
						b.setX(cx + c);
						b.setY(cy + s);
						grid.addEntity(be, a);
						grid.addEntity(be, b);
						a.setX(b.getX());
						a.setY(b.getY());
					}
					continue;
				}
				if (e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
					e = DXFSplineConverter.toDXFPolyline((DXFSpline) e);
					// fall through to the next case, polyline.
				}
				if (e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
					DXFPolyline polyLine = (DXFPolyline) e;

					if (!polyLine.isClosed()) {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(polyLine.getVertexCount() - 1).getPoint());
					} else {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
					}
					continue;
				}
				if (e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
					DXFLWPolyline polyLine = (DXFLWPolyline) e;
					if (!polyLine.isClosed()) {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(polyLine.getVertexCount() - 1).getPoint());
					} else {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
					}
					continue;
				}
				//if(e.getType().equals(DXFConstants.ENTITY_TYPE_ARC)) {}
				//if(e.getType().equals(DXFConstants.ENTITY_TYPE_CIRCLE)) {}
				// I don't know this entity type.
				logger.error("Unknown DXF type {}", e.getType());
			}
		}
		
		//grid.countEntitiesInBuckets();
	}
	
	@Override
	public Turtle load(InputStream in) throws Exception {
		logger.debug("{}...", Translator.get("FileTypeDXF2"));

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
			if (!entityTypeIter.hasNext()) {
				continue;
			}

			// ignore the color index, DXF is dumb.
			myTurtle.setColor(new ColorRGB(0,0,0));
			
			// Sort the entities on this layer into the buckets.
			// Buckets are arranged in an XY grid.
			// All non-closed entities would appear in two buckets.
			// One Entity might be in the same bucket twice.
			Point topLeft = new Point();
			Point bottomRight = new Point();
			topLeft.setX(bounds.getMinimumX());
			topLeft.setY(bounds.getMinimumY());
			bottomRight.setX(bounds.getMaximumX());
			bottomRight.setY(bounds.getMaximumY());
			DXFBucketGrid grid = new DXFBucketGrid(15,15,topLeft,bottomRight);
			List<DXFGroup> groups = new LinkedList<DXFGroup>();

			sortEntitiesIntoBucketsAndGroups(doc,layer,grid,groups);

			// Use the buckets to narrow the search field and find neighboring entities
			//grid.sortEntitiesIntoContinguousGroups(groups,0.1);
			grid.dumpEverythingIntoABucket(groups);
			removeDuplicates(groups);

			for (DXFGroup g : groups) {
				for (DXFBucketEntity dxfBucketEntity : g.entities) {
					parseEntity(dxfBucketEntity.entity);
				}
			}
		}

		return myTurtle;
	}

	private void parseEntity(DXFEntity e) {
		if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
			parseDXFLine((DXFLine)e);
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
			DXFPolyline polyLine = DXFSplineConverter.toDXFPolyline((DXFSpline)e);
			parseDXFPolyline(polyLine);
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)
				|| e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
			parseDXFPolyline((DXFPolyline)e);
		}
	}
		
	/**
	 * http://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
	 * @param groups
	 */
	private void removeDuplicates(List<DXFGroup> groups) {
		int totalRemoved=0;
		
		Iterator<DXFGroup> g = groups.iterator();
		while(g.hasNext()) {
			DXFGroup group = g.next();
			int before = group.entities.size();
			Set<DXFBucketEntity> hs = new LinkedHashSet<DXFBucketEntity>();
			hs.addAll(group.entities);
			group.entities.clear();
			group.entities.addAll(hs);
			int after = group.entities.size();
			totalRemoved += before - after;
		}
		if(totalRemoved!=0) logger.debug("{} duplicates removed.", totalRemoved);
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
