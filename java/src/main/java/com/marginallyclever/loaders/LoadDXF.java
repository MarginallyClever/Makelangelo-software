package com.marginallyclever.loaders;

import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
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

import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class LoadDXF extends ImageManipulator implements LoadFileType {
	boolean shouldScaleOnLoad=true;
	
	@Override
	public String getName() { return "DXF"; }
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return new FileNameExtensionFilter(Translator.get("FileTypeDXF"), "dxf");
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		final JCheckBox checkScale = new JCheckBox(Translator.get("DXFScaleOnLoad"));

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(checkScale);
		checkScale.setSelected(shouldScaleOnLoad);

		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			shouldScaleOnLoad = checkScale.isSelected();
			
			return loadNow(in,robot);
		}
		return false;
	}
	

	@SuppressWarnings("unchecked")
	private boolean loadNow(InputStream in,MakelangeloRobot robot) {
		String destinationFile = System.getProperty("user.dir") + "/temp.ngc";
		Log.message(Translator.get("Converting") + " " + destinationFile);

		try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
				Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

			machine = robot.getSettings();
			tool = robot.getSettings().getCurrentTool();
			// gcode preamble
			out.write(robot.getSettings().getConfigLine() + ";\n");
			out.write(robot.getSettings().getBobbinLine() + ";\n");
			out.write(robot.getSettings().getSetStartAtHomeLine()+";\n");
			setAbsoluteMode(out);
			tool.writeChangeTo(out);
			liftPen(out);

			// start parser
			Parser parser = ParserBuilder.createDefaultParser();
			parser.parse(in, DXFParser.DEFAULT_ENCODING);
			DXFDocument doc = parser.getDocument();
			Bounds b = doc.getBounds();
			double imageCenterX = (b.getMaximumX() + b.getMinimumX()) / 2.0;
			double imageCenterY = (b.getMaximumY() + b.getMinimumY()) / 2.0;

			// find the scale to fit the image on the paper without
			// altering the aspect ratio
			double imageWidth  = (b.getMaximumX() - b.getMinimumX());
			double imageHeight = (b.getMaximumY() - b.getMinimumY());
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
			
			double toolDiameterSquared = tool.getDiameter() * tool.getDiameter();
			boolean isLifted=true;
			double dxf_x2 = 0;
			double dxf_y2 = 0;

			// count all entities in all layers
			Iterator<DXFLayer> layer_iter = (Iterator<DXFLayer>) doc.getDXFLayerIterator();
			//int entity_total = 0;
			while (layer_iter.hasNext()) {
				DXFLayer layer = (DXFLayer) layer_iter.next();
				Log.message("Found layer " + layer.getName());
				Iterator<String> entity_iter = (Iterator<String>) layer.getDXFEntityTypeIterator();
				while (entity_iter.hasNext()) {
					String entity_type = (String) entity_iter.next();
					List<DXFEntity> entity_list = (List<DXFEntity>) layer.getDXFEntities(entity_type);
					Log.message("Found " + entity_list.size() + " of type " + entity_type);
					//entity_total += entity_list.size();
				}
			}

			// convert each entity
			layer_iter = doc.getDXFLayerIterator();
			while (layer_iter.hasNext()) {
				DXFLayer layer = (DXFLayer) layer_iter.next();

				Iterator<String> entity_type_iter = (Iterator<String>) layer.getDXFEntityTypeIterator();
				while (entity_type_iter.hasNext()) {
					String entity_type = (String) entity_type_iter.next();
					List<DXFEntity> entity_list = layer.getDXFEntities(entity_type);

					if (entity_type.equals(DXFConstants.ENTITY_TYPE_LINE)) {
						Iterator<DXFEntity> iter = entity_list.iterator();
						while (iter.hasNext()) {
							DXFLine entity = (DXFLine) iter.next();
							Point start = entity.getStartPoint();
							Point end = entity.getEndPoint();

							double x = (start.getX() - imageCenterX) * scale;
							double y = (start.getY() - imageCenterY) * scale;
							double x2 = (end.getX() - imageCenterX) * scale;
							double y2 = (end.getY() - imageCenterY) * scale;

							double dx = dxf_x2 - x;
							double dy = dxf_y2 - y;
							if (dx * dx + dy * dy > toolDiameterSquared) {
								if (!isLifted) {
									liftPen(out);
									isLifted=true;
								}
								moveTo(out, (float) x, (float) y,true);
							}
							if (isLifted) {
								lowerPen(out);
								isLifted=false;
							}
							moveTo(out, (float) x2, (float) y2,false);
							dxf_x2 = x2;
							dxf_y2 = y2;
						}
					} else if (entity_type.equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
						Iterator<DXFEntity> iter = entity_list.iterator();
						while (iter.hasNext()) {
							DXFSpline entity = (DXFSpline) iter.next();
							entity.setLineWeight(30);
							DXFPolyline polyLine = DXFSplineConverter.toDXFPolyline(entity);
							boolean first = true;
							int count = polyLine.getVertexCount() + (polyLine.isClosed()?1:0);
							for (int j = 0; j < count; ++j) {
								DXFVertex v = polyLine.getVertex(j % polyLine.getVertexCount());
								double x = (v.getX() - imageCenterX) * scale;
								double y = (v.getY() - imageCenterY) * scale;
								double dx = dxf_x2 - x;
								double dy = dxf_y2 - y;

								if (first == true) {
									first = false;
									if (dx * dx + dy * dy > toolDiameterSquared) {
										// line does not start at last tool location, lift and move.
										if (!isLifted) {
											liftPen(out);
											isLifted=true;
										}
										moveTo(out, (float) x, (float) y,true);
									}
									// else line starts right here, pen is down, do nothing extra.
								} else {
									// not the first point, draw.
									if (isLifted) {
										lowerPen(out);
										isLifted=false;
									}
									if (j < polyLine.getVertexCount() - 1 && dx * dx + dy * dy <= toolDiameterSquared)
										continue; // points too close together
									moveTo(out, (float) x, (float) y,false);
								}
								dxf_x2 = x;
								dxf_y2 = y;
							}
						}
					} else if (entity_type.equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
						Iterator<DXFEntity> iter = entity_list.iterator();
						while (iter.hasNext()) {
							DXFPolyline entity = (DXFPolyline) iter.next();
							boolean first = true;
							int count = entity.getVertexCount() + (entity.isClosed()?1:0);
							for (int j = 0; j < count; ++j) {
								DXFVertex v = entity.getVertex(j % entity.getVertexCount());
								double x = (v.getX() - imageCenterX) * scale;
								double y = (v.getY() - imageCenterY) * scale;
								double dx = dxf_x2 - x;
								double dy = dxf_y2 - y;

								if (first == true) {
									first = false;
									if (dx * dx + dy * dy > toolDiameterSquared) {
										// line does not start at last tool location, lift and move.
										if (!isLifted) {
											liftPen(out);
											isLifted=true;
										}
										moveTo(out, (float) x, (float) y,true);
									}
									// else line starts right here, pen is down, do nothing extra.
								} else {
									// not the first point, draw.
									if (isLifted) {
										lowerPen(out);
										isLifted=false;
									}
									if (j < entity.getVertexCount() - 1
											&& dx * dx + dy * dy < toolDiameterSquared)
										continue; // points too close together
									moveTo(out, (float) x, (float) y,false);
								}
								dxf_x2 = x;
								dxf_y2 = y;
							}
						}
					}
				}
			}

			// entities finished. Close up file.
			liftPen(out);
			moveTo(out, 0, 0,true);

			LoadGCode loader = new LoadGCode();
			InputStream fileInputStream = new FileInputStream(destinationFile);
			loader.load(fileInputStream,robot);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		return true;
	}
}
