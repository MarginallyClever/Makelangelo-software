package com.marginallyclever.makelangeloRobot.loadAndSave;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.kabeja.dxf.Bounds;
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

import com.marginallyclever.gcode.GCodeFile;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.ImageManipulator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * Reads in DXF file and converts it to a temporary gcode file, then calls LoadGCode. 
 * @author Dan Royer
 *
 */
public class LoadAndSaveDXF extends ImageManipulator implements LoadAndSaveFileType {
	private static boolean shouldScaleOnLoad=true;
	private static boolean shouldInfillOnLoad=true;
	private static boolean shouldOptimizePathingOnLoad=false;
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeDXF"), "dxf");
	private double previousX,previousY;

	
	@Override
	public String getName() { return "DXF"; }
	
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
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		final JCheckBox checkScale = new JCheckBox(Translator.get("DXFScaleOnLoad"));
		final JCheckBox checkInfill = new JCheckBox(Translator.get("DXFInfillOnLoad"));
		final JCheckBox checkOptimize = new JCheckBox(Translator.get("DXFOptimizeOnLoad"));

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(checkScale);
		//panel.add(checkInfill);
		panel.add(checkOptimize);
		checkScale.setSelected(shouldScaleOnLoad);
		checkInfill.setSelected(shouldInfillOnLoad);
		checkOptimize.setSelected(shouldOptimizePathingOnLoad);

		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			shouldScaleOnLoad = checkScale.isSelected();
			shouldInfillOnLoad = checkInfill.isSelected();
			shouldOptimizePathingOnLoad = checkOptimize.isSelected();
			
			return loadNow(in,robot);
		}
		return false;
	}

	
	// count all entities in all layers
	@SuppressWarnings("unchecked")
	protected void countAllEntities(DXFDocument doc) {
		Iterator<DXFLayer> layerIter = (Iterator<DXFLayer>) doc.getDXFLayerIterator();
		int entityTotal = 0;
		while (layerIter.hasNext()) {
			DXFLayer layer = (DXFLayer) layerIter.next();
			Log.message("Found layer " + layer.getName());
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


	protected void sortGroupsByProximity(List<DXFGroup> groups) {
		//Log.message("Sorting groups by proximity...");
	}

	/**
	 * Put every entity into a bucket.
	 * @param doc
	 * @param grid
	 * @param groups
	 */
	@SuppressWarnings("unchecked")
	protected void sortEntitiesIntoBucketsAndGroups(DXFDocument doc,DXFLayer layer,DXFBucketGrid grid,List<DXFGroup> groups) {
		Log.message("Sorting layer "+layer.getName()+" into buckets...");

			Iterator<String> entityTypeIter = (Iterator<String>) layer.getDXFEntityTypeIterator();
			while (entityTypeIter.hasNext()) {
				String entityType = (String) entityTypeIter.next();
				List<DXFEntity> entityList = (List<DXFEntity>)layer.getDXFEntities(entityType);
				Iterator<DXFEntity> iter = entityList.iterator();
				while(iter.hasNext()) {
					DXFEntity e = iter.next();
					DXFBucketEntity be = new DXFBucketEntity(e);
					
					if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
						DXFLine line = (DXFLine)e;
						grid.addEntity(be, line.getStartPoint());
						grid.addEntity(be, line.getEndPoint());
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
					// I don't know this entity type.
					Log.error("Unknown DXF type "+e.getType());
				}
		}
		
		grid.countEntitiesInBuckets();
	}
	
	
	/**
	 * 
	 * @param in
	 * @param robot
	 * @return true if load is successful.
	 */
	@SuppressWarnings("unchecked")
	private boolean loadNow(InputStream in,MakelangeloRobot robot) {
		Log.message(Translator.get("FileTypeDXF2")+"...");

		File tempFile;
		try {
			tempFile = File.createTempFile("temp", ".ngc");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		tempFile.deleteOnExit();
		Log.message(Translator.get("Converting") + " " + tempFile.getName());

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
		double imageCenterX = (bounds.getMaximumX() + bounds.getMinimumX()) / 2.0;
		double imageCenterY = (bounds.getMaximumY() + bounds.getMinimumY()) / 2.0;

		// find the scale to fit the image on the paper without
		// altering the aspect ratio
		double imageWidth  = (bounds.getMaximumX() - bounds.getMinimumX());
		double imageHeight = (bounds.getMaximumY() - bounds.getMinimumY());
		double paperHeight = robot.getSettings().getPaperHeight() * 10.0 * robot.getSettings().getPaperMargin();
		double paperWidth  = robot.getSettings().getPaperWidth () * 10.0 * robot.getSettings().getPaperMargin();

		double innerAspectRatio = imageWidth / imageHeight;
		double outerAspectRatio = paperWidth / paperHeight;
		double scale = 1;

		if(shouldScaleOnLoad) {
			scale = (innerAspectRatio >= outerAspectRatio) ?
					(paperWidth / imageWidth) :
					(paperHeight / imageHeight);
		}

		//countAllEntities(doc);

		try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
				Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

			// prepare for exporting
			machine = robot.getSettings();
			double toolDiameterSquared = Math.pow(machine.getDiameter()/2, 2);
			double toolMinimumStepSize = Math.pow(machine.getDiameter(), 2);

			// gcode preamble
			//out.write(machine.getGCodeConfig() + ";\n");
			//out.write(machine.getGCodeBobbin() + ";\n");
			//out.write(machine.getGCodeSetPositionAtHome()+";\n");
			setAbsoluteMode(out);
			liftPen(out);
			previousX = machine.getHomeX();
			previousY = machine.getHomeY();

			// convert each entity
			Iterator<DXFLayer> layerIter = doc.getDXFLayerIterator();
			while (layerIter.hasNext()) {
				DXFLayer layer = (DXFLayer) layerIter.next();
				
				// Some DXF layers are empty.  Only write the tool change command if there's something on this layer.
				Iterator<String> entityTypeIter = (Iterator<String>) layer.getDXFEntityTypeIterator();
				if (entityTypeIter.hasNext()) {
					layer.getColor();
					machine.writeChangeTo(out,layer.getName());
				}
				
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

				//DXFGroup infillGroup=null;
				if(shouldInfillOnLoad) {
					//infillGroup = infillClosedAreas(layer);
				}

				if(shouldOptimizePathingOnLoad) {
					// Use the buckets to narrow the search field and find neighboring entities
					grid.sortEntitiesIntoContinguousGroups(groups);
				} else {
					grid.dumpEverythingIntoABucket(groups);
				}
				
				//if(infillGroup!=null) {
				//	groups.add(infillGroup);
				//}

				if(shouldOptimizePathingOnLoad) {
					sortGroupsByProximity(groups);
				}

				// output all entities
				Iterator<DXFGroup> groupIter = groups.iterator();
				while(groupIter.hasNext()) {
					DXFGroup g = groupIter.next();
					Iterator<DXFBucketEntity> iter = g.entities.iterator();
					while(iter.hasNext()) {
						DXFEntity e = iter.next().entity;
						if (e.getType().equals(DXFConstants.ENTITY_TYPE_LINE)) {
							parseDXFLine(out,(DXFLine)e,scale,imageCenterX,imageCenterY,toolDiameterSquared);
						} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
							parseDXFPolyline(out,DXFSplineConverter.toDXFPolyline((DXFSpline)e),scale,imageCenterX,imageCenterY,toolMinimumStepSize);
						} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
							parseDXFPolyline(out,(DXFPolyline)e,scale,imageCenterX,imageCenterY,toolMinimumStepSize);
						} else if (e.getType().equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
							parseDXFLWPolyline(out,(DXFLWPolyline)e,scale,imageCenterX,imageCenterY,toolMinimumStepSize);
						}
					}
				}
				
				// layer finished.
				liftPen(out);
			}

			// entities finished. Close up file.
		    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
			
			out.flush();
			out.close();

			Log.message("Done!");
			LoadAndSaveGCode loader = new LoadAndSaveGCode();
			InputStream fileInputStream = new FileInputStream(tempFile);
			loader.load(fileInputStream,robot);
		} catch (IOException e) {
			e.printStackTrace();
		}

		tempFile.delete();

		return true;
	}
	
	protected void parseDXFLine(Writer out,DXFLine entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		Point start = entity.getStartPoint();
		Point end = entity.getEndPoint();

		double x = (start.getX() - imageCenterX) * scale;
		double y = (start.getY() - imageCenterY) * scale;
		double x2 = (end.getX() - imageCenterX) * scale;
		double y2 = (end.getY() - imageCenterY) * scale;

		// skip extremely short lines?
		if( Math.abs(x-x2)<0.001 && Math.abs(y-y2)<0.001) {
			return;
		}
			
		// which end is closer to the previous (x,y) ?
		double dx = previousX - x;
		double dy = previousY - y;
		double dx2 = previousX - x2;
		double dy2 = previousY - y2;
		if ( dx * dx + dy * dy < dx2 * dx2 + dy2 * dy2 ) {
			parseDXFLineEnds(out,toolDiameterSquared,x,y,x2,y2);
		} else {
			parseDXFLineEnds(out,toolDiameterSquared,x2,y2,x,y);
		}
	}
	
	protected void parseDXFPolyline(Writer out,DXFPolyline entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		if(entity.isClosed()) {
			// only one end to care about
			parseDXFPolylineForward(out,entity,scale,imageCenterX,imageCenterY,toolDiameterSquared);
		} else {
			// which end is closest to the previous (x,y)?
			int n = entity.getVertexCount()-1;
			double x = (entity.getVertex(0).getX() - imageCenterX) * scale;
			double y = (entity.getVertex(0).getY() - imageCenterY) * scale;
			double x2 = (entity.getVertex(n).getX() - imageCenterX) * scale;
			double y2 = (entity.getVertex(n).getY() - imageCenterY) * scale;

			// which end is closer to the previous (x,y) ?
			double dx = previousX - x;
			double dy = previousY - y;
			double dx2 = previousX - x2;
			double dy2 = previousY - y2;
			if ( dx * dx + dy * dy < dx2 * dx2 + dy2 * dy2 ) {
				// first point is closer
				parseDXFPolylineForward(out,entity,scale,imageCenterX,imageCenterY,toolDiameterSquared);
			} else {
				// last point is closer
				parseDXFPolylineBackward(out,entity,scale,imageCenterX,imageCenterY,toolDiameterSquared);
			}
		}
	}
	
	protected void parseDXFLWPolylineForward(Writer out,DXFLWPolyline entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		boolean first = true;
		int count = entity.getVertexCount() + (entity.isClosed()?1:0);
		for (int j = 0; j < count; ++j) {
			DXFVertex v = entity.getVertex(j % entity.getVertexCount());
			double x = (v.getX() - imageCenterX) * scale;
			double y = (v.getY() - imageCenterY) * scale;
			parseCurvingLine(out,x,y,toolDiameterSquared,first,j<count-1);
			first = false;
		}
	}
	
	protected void parseDXFLWPolylineBackward(Writer out,DXFLWPolyline entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		boolean first = true;
		int count = entity.getVertexCount() + (entity.isClosed()?1:0);
		for (int j = 0; j < count; ++j) {
			DXFVertex v = entity.getVertex((count+count-1-j) % entity.getVertexCount());
			double x = (v.getX() - imageCenterX) * scale;
			double y = (v.getY() - imageCenterY) * scale;
			parseCurvingLine(out,x,y,toolDiameterSquared,first,j<count-1);
			first = false;
		}
	}

	protected void parseDXFLWPolyline(Writer out,DXFLWPolyline entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		if(entity.isClosed()) {
			// only one end to care about
			parseDXFLWPolylineForward(out,entity,scale,imageCenterX,imageCenterY,toolDiameterSquared);
		} else {
			// which end is closest to the previous (x,y)?
			int n = entity.getVertexCount()-1;
			double x = (entity.getVertex(0).getX() - imageCenterX) * scale;
			double y = (entity.getVertex(0).getY() - imageCenterY) * scale;
			double x2 = (entity.getVertex(n).getX() - imageCenterX) * scale;
			double y2 = (entity.getVertex(n).getY() - imageCenterY) * scale;

			// which end is closer to the previous (x,y) ?
			double dx = previousX - x;
			double dy = previousY - y;
			double dx2 = previousX - x2;
			double dy2 = previousY - y2;
			if ( dx * dx + dy * dy < dx2 * dx2 + dy2 * dy2 ) {
				// first point is closer
				parseDXFLWPolylineForward(out,entity,scale,imageCenterX,imageCenterY,toolDiameterSquared);
			} else {
				// last point is closer
				parseDXFLWPolylineBackward(out,entity,scale,imageCenterX,imageCenterY,toolDiameterSquared);
			}
		}
	}
	
	protected void parseDXFPolylineForward(Writer out,DXFPolyline entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		boolean first = true;
		int count = entity.getVertexCount() + (entity.isClosed()?1:0);
		for (int j = 0; j < count; ++j) {
			DXFVertex v = entity.getVertex(j % entity.getVertexCount());
			double x = (v.getX() - imageCenterX) * scale;
			double y = (v.getY() - imageCenterY) * scale;
			parseCurvingLine(out,x,y,toolDiameterSquared,first,j<count-1);
			first = false;
		}
	}
	
	protected void parseDXFPolylineBackward(Writer out,DXFPolyline entity,double scale,double imageCenterX,double imageCenterY,double toolDiameterSquared) throws IOException {
		boolean first = true;
		int count = entity.getVertexCount() + (entity.isClosed()?1:0);
		for (int j = 0; j < count; ++j) {
			DXFVertex v = entity.getVertex((count+count-1-j) % entity.getVertexCount());
			double x = (v.getX() - imageCenterX) * scale;
			double y = (v.getY() - imageCenterY) * scale;
			parseCurvingLine(out,x,y,toolDiameterSquared,first,j<count-1);
			first = false;
		}
	}
	
	protected void parseCurvingLine(Writer out,double x,double y,double limitSquared,boolean first,boolean notLast) throws IOException {
		double dx = x - previousX;
		double dy = y - previousY;

		if (first == true) {
			if (dx * dx + dy * dy > limitSquared) {
				// line does not start at last tool location, lift and move.
				if (!lastUp) liftPen(out);
				moveTo(out, (float) x, (float) y, true);
				previousX = x;
				previousY = y;
			}
			// else line starts right here, pen is down, do nothing extra.
		} else {
			if (lastUp) lowerPen(out);
			// not the first point, draw.
			if (notLast && dx * dx + dy * dy < limitSquared)
				return; // points too close together
			moveTo(out, (float) x, (float) y, false);
			previousX = x;
			previousY = y;
		}
	}
	
	protected void parseDXFLineEnds(Writer out,double toolDiameterSquared,double x,double y,double x2,double y2) throws IOException {
		double dx = previousX - x;
		double dy = previousY - y;
		if (dx * dx + dy * dy > toolDiameterSquared) {
			if (!lastUp) liftPen(out);
			moveTo(out, (float) x, (float) y, true);
			previousX = x;
			previousY = y;
		}
		if (lastUp) lowerPen(out);
		moveTo(out, (float) x2, (float) y2, false);
		previousX = x2;
		previousY = y2;
	}

	@Override
	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	public boolean save(OutputStream outputStream, MakelangeloRobot robot) {
		Log.message("saving...");
		GCodeFile sourceMaterial = robot.gCode;
		sourceMaterial.setLinesProcessed(0);
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		try {
			// header
			out.write("999\nDXF created by Makelangelo software v"+Makelangelo.VERSION+"\n");
			out.write("0\nSECTION\n");
			out.write("2\nHEADER\n");
			out.write("9\n$ACADVER\n1\nAC1006\n");
			out.write("9\n$INSBASE\n");
			out.write("10\n"+robot.getSettings().getPaperLeft()+"\n");
			out.write("20\n"+robot.getSettings().getPaperBottom()+"\n");
			out.write("30\n0.0\n");
			out.write("9\n$EXTMIN\n");
			out.write("10\n"+robot.getSettings().getPaperLeft()+"\n");
			out.write("20\n"+robot.getSettings().getPaperBottom()+"\n");
			out.write("30\n0.0\n");
			out.write("9\n$EXTMAX\n");
			out.write("10\n"+robot.getSettings().getPaperRight()+"\n");
			out.write("20\n"+robot.getSettings().getPaperTop()+"\n");
			out.write("30\n0.0\n");
			out.write("0\nENDSEC\n");

			// tables section
			out.write("0\nSECTION\n");
			out.write("2\nTABLES\n");
			// line type
			out.write("0\nTABLE\n");
			out.write("2\nLTYPE\n");
			out.write("70\n1\n");
			out.write("0\nLTYPE\n");
			out.write("2\nCONTINUOUS\n");
			out.write("70\n64\n");
			out.write("3\nSolid line\n");
			out.write("72\n65\n");
			out.write("73\n0\n");
			out.write("40\n0.000\n");
			out.write("0\nENDTAB\n");
			// layers
			out.write("0\nTABLE\n");
			out.write("2\nLAYER\n");
			out.write("70\n6\n");
			out.write("0\nLAYER\n");
			out.write("2\n1\n");
			out.write("70\n64\n");
			out.write("62\n7\n");
			out.write("6\nCONTINUOUS\n");
			out.write("0\nLAYER\n");
			out.write("2\n2\n");
			out.write("70\n64\n");
			out.write("62\n7\n");
			out.write("6\nCONTINUOUS\n");
			out.write("0\nENDTAB\n");
			out.write("0\nTABLE\n");
			out.write("2\nSTYLE\n");
			out.write("70\n0\n");
			out.write("0\nENDTAB\n");
			// end tables
			out.write("0\nENDSEC\n");

			// empty blocks section (good form?)
			out.write("0\nSECTION\n");
			out.write("0\nBLOCKS\n");
			out.write("0\nENDSEC\n");
			// now the lines
			out.write("0\nSECTION\n");
			out.write("2\nENTITIES\n");

			boolean penUp=true;
			float x0 = (float) robot.getSettings().getHomeX();
			float y0 = (float) robot.getSettings().getHomeY();
			float x1;
			float y1;
			
			String matchUp = robot.getSettings().getPenUpString();
			String matchDown = robot.getSettings().getPenDownString();
			if(matchUp.contains(";")) {
				matchUp = matchUp.substring(0, matchUp.indexOf(";"));
			}
			if(matchDown.contains(";")) {
				matchDown = matchDown.substring(0, matchDown.indexOf(";"));
			}
			
			int total=sourceMaterial.getLinesTotal();
			Log.message(total+" total lines to save.");
			for(int i=0;i<total;++i) {
				String str = sourceMaterial.nextLine();
				// trim comments
				if(str.contains(";")) {
					str = str.substring(0, str.indexOf(";"));
				}
				if(str.contains(matchUp)) {
					penUp=true;
				}
				if(str.contains(matchDown)) {
					penUp=false;
				}
				if(str.startsWith("G0") || str.startsWith("G1")) {
					// move command
					String[] tokens = str.split(" ");
					x1=x0;
					y1=y0;
					int j;
					for(j=0;j<tokens.length;++j) {
						String tok = tokens[j];
						if(tok.startsWith("X")) {
							x1=Float.parseFloat(tok.substring(1));
						} else if(tok.startsWith("Y")) {
							y1=Float.parseFloat(tok.substring(1));
						}
					}
					if(penUp==false && ( x1!=x0 || y1!=y0 ) ) {
						out.write("0\nLINE\n");
						out.write("8\n1\n");  // layer 1
						out.write("10\n"+x0+"\n");
						out.write("20\n"+y0+"\n");
						out.write("11\n"+x1+"\n");
						out.write("21\n"+y1+"\n");
					}
					x0=x1;
					y0=y1;
				}
			}
			// wrap it up
			out.write("0\nENDSEC\n");
			out.write("0\nEOF\n");
			out.flush();
		}
		catch(IOException e) {
			Log.error(Translator.get("SaveError") +" "+ e.getLocalizedMessage());
			return false;
		}
		
		Log.message("done.");
		return true;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return true;
	}
}
