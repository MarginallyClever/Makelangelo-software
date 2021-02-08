package com.marginallyclever.artPipeline.nodes.svg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.nodes.LoadAndSaveFile;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.StringHelper;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodePanel;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.core.turtle.TurtleMove;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.robot.MakelangeloRobot;

/**
 * Saves {@code Turtle} to SVG
 * See https://www.w3.org/TR/SVG/paths.html
 * @author Dan Royer
 * @Since 7.25.0
 */
public class SaveSVG extends Node implements LoadAndSaveFile {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeSVG"), "svg");
	
	protected double scale,imageCenterX,imageCenterY;
	protected double toolMinimumStepSize = 1; //mm
	
	@Override
	public String getName() { return "SaveSVG"; }
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	
	@Override
	public boolean canLoad() {
		return false;
	}

	@Override
	public boolean canLoad(String filename) {
		return false;
	}

	@Override
	public boolean load(InputStream in) {
		return false;
	}
	  
	@Override
	public boolean canSave() {
		return true;
	}
	
	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".svg"));
	}

	/**
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	@Override
	public boolean save(OutputStream outputStream,ArrayList<Turtle> turtles, MakelangeloRobot robot) {
		Log.message("saving...");

		try(OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			// Find the actual bounds
			Point2D totalBottom = new Point2D();
			Point2D totalTop = new Point2D();
			Turtle.getBounds(turtles,totalTop,totalBottom);
			
			double top = totalTop.y;
			double right = totalTop.x;
			double bottom = totalBottom.y;
			double left = totalBottom.x;
			
			// header
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
			out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
			out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\""+left+" "+bottom+" "+(right-left)+" "+(top-bottom)+"\">\n");

			boolean isUp=true;
			Turtle firstTurtle = turtles.get(0);
			double x0 = firstTurtle.getX();
			double y0 = firstTurtle.getY();
			for( Turtle t : turtles ) {
				String colorName = t.getColor().toString();
				double dia = 1.0;
				
				for( TurtleMove m : t.history ) {
					if(m.isUp) {
						if(!isUp) {
							isUp=true;
						}
					} else {
						if(isUp) {
							isUp=false;
						} else {
							out.write("  <line");
							out.write(" x1=\""+StringHelper.formatDouble(x0)+"\"");
							out.write(" y1=\""+StringHelper.formatDouble(-y0)+"\"");
							out.write(" x2=\""+StringHelper.formatDouble(m.x)+"\"");
							out.write(" y2=\""+StringHelper.formatDouble(-m.y)+"\"");
							out.write(" stroke=\"+"+colorName+"\"");
							out.write(" stroke-width=\""+dia+"\"");
							out.write(" />\n");
						}
					}
					x0=m.x;
					y0=m.y;
				}
				
			}
			
			// footer
			out.write("</svg>");
			// end
			out.flush();
		}
		catch(IOException e) {
			Log.error(Translator.get("SaveError") +" "+ e.getLocalizedMessage());
			return false;
		}
		
		Log.message("done.");
		return true;
	}
}
