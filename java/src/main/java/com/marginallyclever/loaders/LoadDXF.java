package com.marginallyclever.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
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

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class LoadDXF implements LoadFileType {

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return new FileNameExtensionFilter(Translator.get("FileTypeDXF"), "dxf");
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	public boolean load(String filename,MakelangeloRobot robot,Makelangelo gui) {
		final String destinationFile = gui.getTempDestinationFile();

		final ProgressMonitor pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);

		final SwingWorker<Void, Void> s = new SwingWorker<Void, Void>() {
			public boolean ok = false;

			@SuppressWarnings("unchecked")
			@Override
			public Void doInBackground() {
				Log.message(Translator.get("Converting") + " " + destinationFile);

				Parser parser = ParserBuilder.createDefaultParser();

				double dxf_x2 = 0;
				double dxf_y2 = 0;

				try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
						Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
					DrawingTool tool = robot.settings.getCurrentTool();
					out.write(robot.settings.getConfigLine() + ";\n");
					out.write(robot.settings.getBobbinLine() + ";\n");
					out.write("G00 G90;\n");
					tool.writeChangeTo(out);
					tool.writeOff(out);

					parser.parse(filename, DXFParser.DEFAULT_ENCODING);
					DXFDocument doc = parser.getDocument();
					Bounds b = doc.getBounds();
					double imageCenterX = (b.getMaximumX() + b.getMinimumX()) / 2.0f;
					double imageCenterY = (b.getMaximumY() + b.getMinimumY()) / 2.0f;

					// find the scale to fit the image on the paper without
					// altering the aspect ratio
					double imageWidth = (b.getMaximumX() - b.getMinimumX());
					double imageHeight = (b.getMaximumY() - b.getMinimumY());
					double paperHeight = robot.settings.getPaperHeight() * 10 * robot.settings.getPaperMargin();
					double paperWidth = robot.settings.getPaperWidth() * 10 * robot.settings.getPaperMargin();

					double innerAspectRatio = imageWidth / imageHeight;
					double outerAspectRatio = paperWidth / paperHeight;
					double scale = (innerAspectRatio >= outerAspectRatio) ? (paperWidth / imageWidth)
							: (paperHeight / imageHeight);

					// count all entities in all layers
					Iterator<DXFLayer> layer_iter = (Iterator<DXFLayer>) doc.getDXFLayerIterator();
					int entity_total = 0;
					int entity_count = 0;
					while (layer_iter.hasNext()) {
						DXFLayer layer = (DXFLayer) layer_iter.next();
						Log.message("Found layer " + layer.getName());
						Iterator<String> entity_iter = (Iterator<String>) layer.getDXFEntityTypeIterator();
						while (entity_iter.hasNext()) {
							String entity_type = (String) entity_iter.next();
							List<DXFEntity> entity_list = (List<DXFEntity>) layer.getDXFEntities(entity_type);
							Log.message("Found " + entity_list.size() + " of type " + entity_type);
							entity_total += entity_list.size();
						}
					}
					// set the progress meter
					pm.setMinimum(0);
					pm.setMaximum(entity_total);

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
									pm.setProgress(entity_count++);
									DXFLine entity = (DXFLine) iter.next();
									Point start = entity.getStartPoint();
									Point end = entity.getEndPoint();

									double x = (start.getX() - imageCenterX) * scale;
									double y = (start.getY() - imageCenterY) * scale;
									double x2 = (end.getX() - imageCenterX) * scale;
									double y2 = (end.getY() - imageCenterY) * scale;
									double dx, dy;
									// *
									// is it worth drawing this line?
									dx = x2 - x;
									dy = y2 - y;
									if (dx * dx + dy * dy < tool.getDiameter() / 2.0) {
										continue;
									}
									// */
									dx = dxf_x2 - x;
									dy = dxf_y2 - y;

									if (dx * dx + dy * dy > tool.getDiameter() / 2.0) {
										if (tool.isDrawOn()) {
											tool.writeOff(out);
										}
										tool.writeMoveTo(out, (float) x, (float) y);
									}
									if (tool.isDrawOff()) {
										tool.writeOn(out);
									}
									tool.writeMoveTo(out, (float) x2, (float) y2);
									dxf_x2 = x2;
									dxf_y2 = y2;
								}
							} else if (entity_type.equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while (iter.hasNext()) {
									pm.setProgress(entity_count++);
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
											if (dx * dx + dy * dy > tool.getDiameter() / 2.0) {
												// line does not start at last
												// tool location, lift and move.
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											// else line starts right here, do
											// nothing.
										} else {
											// not the first point, draw.
											if (tool.isDrawOff())
												tool.writeOn(out);
											if (j < polyLine.getVertexCount() - 1
													&& dx * dx + dy * dy < tool.getDiameter() / 2.0)
												continue; // less than 1mm
															// movement? Skip
															// it.
											tool.writeMoveTo(out, (float) x, (float) y);
										}
										dxf_x2 = x;
										dxf_y2 = y;
									}
								}
							} else if (entity_type.equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while (iter.hasNext()) {
									pm.setProgress(entity_count++);
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
											if (dx * dx + dy * dy > tool.getDiameter()) {
												// line does not start at last
												// tool location, lift and move.
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											// else line starts right here, do nothing.
										} else {
											// not the first point, draw.
											if (tool.isDrawOff())
												tool.writeOn(out);
											if (j < entity.getVertexCount() - 1
													&& dx * dx + dy * dy < tool.getDiameter()*3)
												continue; // less than 1mm
															// movement? Skip
															// it.
											tool.writeMoveTo(out, (float) x, (float) y);
										}
										dxf_x2 = x;
										dxf_y2 = y;
									}
								}
							}
						}
					}

					// entities finished. Close up file.
					tool.writeOff(out);
					tool.writeMoveTo(out, 0, 0);

					ok = true;
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}

				pm.setProgress(100);
				return null;
			}

			@Override
			public void done() {
				pm.close();
				Log.message(Translator.get("Finished"));
				SoundSystem.playConversionFinishedSound();
				if (ok) {
					LoadGCode loader = new LoadGCode();
					loader.load(destinationFile, robot, gui);
				}
				gui.halt();
			}
		};

		s.addPropertyChangeListener(new PropertyChangeListener() {
			// Invoked when task's progress property changes.
			public void propertyChange(PropertyChangeEvent evt) {
				if (Objects.equals("progress", evt.getPropertyName())) {
					int progress = (Integer) evt.getNewValue();
					pm.setProgress(progress);
					String message = String.format("%d%%\n", progress);
					pm.setNote(message);
					if (s.isDone()) {
						Log.message(Translator.get("Finished"));
					} else if (s.isCancelled() || pm.isCanceled()) {
						if (pm.isCanceled()) {
							s.cancel(true);
						}
						Log.message(Translator.get("Cancelled"));
					}
				}
			}
		});

		s.execute();
		return true;
	}
}
