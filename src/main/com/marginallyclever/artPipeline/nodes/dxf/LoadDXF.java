package com.marginallyclever.artPipeline.nodes.dxf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.LoadAndSaveFile;
import com.marginallyclever.core.ColorRGB;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnectorExistingFile;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.robot.MakelangeloRobot;

/**
 * Reads in DXF file and converts it to a Turtle.
 * @author Dan Royer
 * @since 7.25.0
 */
public class LoadDXF extends Node implements LoadAndSaveFile {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("LoadDXF.filter"), "dxf");
	private NodeConnectorExistingFile inputFile = new NodeConnectorExistingFile("LoadDXF.inputFile",filter,""); 
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageConverter.outputTurtle");

	private double previousX,previousY;
	private double imageCenterX,imageCenterY;
	
	public LoadDXF() {
		super();
		inputs.add(inputFile);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("LoadDXF.name");
	}
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return false;
	}
	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	@Override
	public boolean canSave(String filename) {
		return false;
	}


	// count all entities in all layers
	@SuppressWarnings("unchecked")
	protected void countAllEntities(DXFDocument doc) {
		Iterator<DXFLayer> layerIter = (Iterator<DXFLayer>) doc.getDXFLayerIterator();
		int entityTotal = 0;
		while (layerIter.hasNext()) {
			DXFLayer layer = (DXFLayer) layerIter.next();
			int color = layer.getColor();
			Log.message("Found layer " + layer.getName() + "(RGB="+color+")");
			Iterator<String> entityIter = (Iterator<String>) layer.getDXFEntityTypeIterator();
			while (entityIter.hasNext()) {
				String entityType = (String) entityIter.next();
				List<DXFEntity> entityList = (List<DXFEntity>) layer.getDXFEntities(entityType);
				Log.message("Found " + entityList.size() + " of type " + entityType);
				entityTotal += entityList.size();
			}
		}
		Log.message(entityTotal + " total entities.");
	}

	
	/**
	 * Put every entity into a bucket.
	 * @param doc
	 * @param grid
	 * @param groups
	 */
	@SuppressWarnings("unchecked")
	protected void sortEntitiesIntoBucketsAndGroups(DXFDocument doc,DXFLayer layer,DXFBucketGrid grid,List<DXFGroup> groups) {
		//Log.message("Sorting layer "+layer.getName()+" into buckets...");

		Iterator<String> entityTypeIter = (Iterator<String>) layer.getDXFEntityTypeIterator();
		while (entityTypeIter.hasNext()) {
			String entityType = (String) entityTypeIter.next();
			List<DXFEntity> entityList = (List<DXFEntity>)layer.getDXFEntities(entityType);
			Iterator<DXFEntity> iter = entityList.iterator();
			while(iter.hasNext()) {
				DXFEntity e = iter.next();
				DXFBucketEntity be = new DXFBucketEntity(e);
				
				if(e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
					DXFLine line = (DXFLine)e;
					grid.addEntity(be, line.getStartPoint());
					grid.addEntity(be, line.getEndPoint());
					continue;
				}
				if(e.getType().equals(DXFConstants.ENTITY_TYPE_CIRCLE)) {
					DXFCircle circle = (DXFCircle)e;
					double r = circle.getRadius();
					Point center = circle.getCenterPoint();
					double cx = center.getX();
					double cy = center.getY();

					Point a = new Point(cx+r,cy,0);
					Point b = new Point();
					for(double i=1;i<=40;++i) {  // hard coded 40?  gross!
						double v = (Math.PI*2.0) * (i/40.0);
						double s=r*Math.sin(v);
						double c=r*Math.cos(v);
						b.setX(cx+c);
						b.setY(cy+s);
						grid.addEntity(be, a);
						grid.addEntity(be, b);
						a.setX(b.getX());
						a.setY(b.getY());
					}
					continue;
				}
				if(e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
					e = DXFSplineConverter.toDXFPolyline((DXFSpline)e);
					// fall through to the next case, polyline.
				}
				if(e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
					DXFPolyline polyLine = (DXFPolyline)e;
					
					if(!polyLine.isClosed()) {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(polyLine.getVertexCount()-1).getPoint());
					} else {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
					}
					continue;
				}
				if(e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
					DXFLWPolyline polyLine = (DXFLWPolyline)e;
					if(!polyLine.isClosed()) {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(polyLine.getVertexCount()-1).getPoint());
					} else {
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
						grid.addEntity(be, polyLine.getVertex(0).getPoint());
					}
					continue;
				}
				//if(e.getType().equals(DXFConstants.ENTITY_TYPE_ARC)) {}
				//if(e.getType().equals(DXFConstants.ENTITY_TYPE_CIRCLE)) {}
				// I don't know this entity type.
				Log.error("Unknown DXF type "+e.getType());
			}
		}
		
		//grid.countEntitiesInBuckets();
	}
	
	
	/**
	 * 
	 * @param in
	 * @param robot
	 * @return true if load is successful.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean load(InputStream in) {
		Log.message("Loading...");
		Turtle turtle = new Turtle();
		
		// Read in the DXF file
		Parser parser = ParserBuilder.createDefaultParser();
		try {
			parser.parse(in, DXFParser.DEFAULT_ENCODING);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		DXFDocument doc = parser.getDocument();
		Bounds bounds = doc.getBounds();
		imageCenterX = (bounds.getMaximumX() + bounds.getMinimumX()) / 2.0;
		imageCenterY = (bounds.getMaximumY() + bounds.getMinimumY()) / 2.0;

		// convert each entity
		Iterator<DXFLayer> layerIter = doc.getDXFLayerIterator();
		while (layerIter.hasNext()) {
			DXFLayer layer = (DXFLayer) layerIter.next();
			int color = layer.getColor();
			Log.message("Found layer " + layer.getName() + "(color index="+color+")");
			
			// Some DXF layers are empty.  Only write the tool change command if there's something on this layer.
			Iterator<String> entityTypeIter = (Iterator<String>)layer.getDXFEntityTypeIterator();
			if (!entityTypeIter.hasNext()) {
				continue;
			}

			// ignore the color index, DXF is dumb.
			turtle.setColor(new ColorRGB(0,0,0));
			
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

			Iterator<DXFGroup> groupIter = groups.iterator();
			while(groupIter.hasNext()) {
				DXFGroup g = groupIter.next();
				Iterator<DXFBucketEntity> ents = g.entities.iterator();
				while(ents.hasNext()) {
					parseEntity(turtle,ents.next().entity);
				}
			}
		}

		outputTurtle.setValue(turtle);
		return true;
	}

	protected void parseEntity(Turtle turtle,DXFEntity e) {
		if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
			parseDXFLine(turtle,(DXFLine)e);
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
			DXFPolyline polyLine = DXFSplineConverter.toDXFPolyline((DXFSpline)e);
			parseDXFPolyline(turtle,polyLine);
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)
				|| e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
			parseDXFPolyline(turtle,(DXFPolyline)e);
		}
	}
	
	protected double distanceFromPrevious(Point p) {
		double dx = previousX - p.getX();
		double dy = previousY - p.getY();
		return dx*dx+dy*dy;
	}
	
	@Deprecated
	protected Point getEntityStart(DXFEntity e) {
		if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
			DXFLine line = (DXFLine)e;
			return line.getStartPoint();
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
			DXFPolyline line = DXFSplineConverter.toDXFPolyline((DXFSpline)e);
			DXFVertex v = line.getVertex(0);
			return new Point(v.getX(),v.getY(),v.getZ());
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)
				|| e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
			DXFPolyline line = (DXFPolyline)e;
			DXFVertex v = line.getVertex(0);
			return new Point(v.getX(),v.getY(),v.getZ());
		}
		assert(false);
		return null;
	}

	@Deprecated
	protected Point getEntityEnd(DXFEntity e) {
		if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
			DXFLine line = (DXFLine)e;
			return line.getEndPoint();
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
			DXFPolyline line = DXFSplineConverter.toDXFPolyline((DXFSpline)e);
			int n=0;
			if(!line.isClosed()) n=line.getVertexCount()-1;
			DXFVertex v = line.getVertex(n);
			return new Point(v.getX(),v.getY(),v.getZ());
		} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)
				|| e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
			DXFPolyline line = (DXFPolyline)e;
			int n=0;
			if(!line.isClosed()) n=line.getVertexCount()-1;
			DXFVertex v = line.getVertex(n);
			return new Point(v.getX(),v.getY(),v.getZ());
		}
		assert(false);
		return null;
	}
	
	/**
	 * http://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
	 * @param groups
	 */
	protected void removeDuplicates(List<DXFGroup> groups) {
		int totalRemoved=0;
		
		Iterator<DXFGroup> g = groups.iterator();
		while(g.hasNext()) {
			DXFGroup group = g.next();
			int before = group.entities.size();
			Set<DXFBucketEntity> hs = new LinkedHashSet<>();
			hs.addAll(group.entities);
			group.entities.clear();
			group.entities.addAll(hs);
			int after = group.entities.size();
			totalRemoved += before - after;
		}
		if(totalRemoved!=0) Log.message(totalRemoved+" duplicates removed.");
	}
	
	protected double TX(double x) {
		return (x-imageCenterX);
	}
	protected double TY(double y) {
		return (y-imageCenterY);
	}
	
	protected void parseDXFLine(Turtle turtle,DXFLine entity) {
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
			parseDXFLineEnds(turtle,x,y,x2,y2);
		} else {
			parseDXFLineEnds(turtle,x2,y2,x,y);
		}
	}
	
	protected void parseDXFLineEnds(Turtle turtle,double x,double y,double x2,double y2) {
		turtle.jumpTo(x,y);
		turtle.moveTo(x2,y2);
		previousX = x2;
		previousY = y2;
	}

	
	protected void parseDXFPolyline(Turtle turtle,DXFPolyline entity) {
		if(entity.isClosed()) {
			// only one end to care about
			parseDXFPolylineForward(turtle,entity);
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
				parseDXFPolylineForward(turtle,entity);
			} else {
				// last point is closer
				parseDXFPolylineBackward(turtle,entity);
			}
		}
	}
	
	protected void parseDXFPolylineForward(Turtle turtle,DXFPolyline entity) {
		boolean first = true;
		int c = entity.getVertexCount();
		int count = c + (entity.isClosed()?1:0);
		DXFVertex v;
		double x,y;
		for (int j = 0; j < count; ++j) {
			v = entity.getVertex(j % c);
			x = TX(v.getX());
			y = TY(v.getY());
			parsePolylineShared(turtle,x,y,first,j<count-1);
			first = false;
		}
	}
	
	protected void parseDXFPolylineBackward(Turtle turtle,DXFPolyline entity) {
		boolean first = true;
		int c = entity.getVertexCount();
		int count = c + (entity.isClosed()?1:0);
		DXFVertex v;
		double x,y;
		for (int j = 0; j < count; ++j) {
			v = entity.getVertex((c*2-1-j) % c);
			x = TX(v.getX());
			y = TY(v.getY());
			parsePolylineShared(turtle,x,y,first,j<count-1);
			first = false;
		}
	}
	
	protected void parsePolylineShared(Turtle turtle,double x,double y,boolean first,boolean notLast) {
		if (first == true) {
			turtle.jumpTo(x,y);
		} else {
			turtle.penDown();
			turtle.moveTo(x,y);
		}
		previousX = x;
		previousY = y;
	}


	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	@Override
	public boolean save(OutputStream outputStream,ArrayList<Turtle> turtles, MakelangeloRobot robot) {
		return false;
	}
}
