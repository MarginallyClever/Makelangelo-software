package com.marginallyclever.makelangelo.makeArt.io.svg;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.ImageManipulator;
import com.marginallyclever.makelangelo.makeArt.io.SaveResource;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * @author Dan Royer
 * See https://www.w3.org/TR/SVG/paths.html
 */
public class SaveSVG extends ImageManipulator implements SaveResource {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeSVG"), "svg");
	
	@Override
	public String getName() { return "SVG"; }
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}
	
	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".svg"));
	}

	@Override
	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	public boolean save(OutputStream outputStream, MakelangeloRobot robot, Component parentComponent) {
		Log.message("saving...");
		turtle = robot.getTurtle();

		settings = robot.getSettings();
		double left = settings.getPaperLeft();
		double right = settings.getPaperRight();
		double top = settings.getPaperTop();
		double bottom = settings.getPaperBottom();
		
		try(OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			// header
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
			out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
			out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\""+left+" "+bottom+" "+(right-left)+" "+(top-bottom)+"\">\n");

			boolean isUp=true;
			double x0 = robot.getSettings().getHomeX();
			double y0 = robot.getSettings().getHomeY();

			for( TurtleMove m : turtle.history ) {
				switch(m.type) {
				case TRAVEL:
					if(!isUp) {
						isUp=true;
					}
					x0=m.x;
					y0=m.y;
					break;
				case DRAW:
					if(isUp) {
						isUp=false;
					} else {
						out.write("  <line");
						out.write(" x1=\""+StringHelper.formatDouble(x0)+"\"");
						out.write(" y1=\""+StringHelper.formatDouble(-y0)+"\"");
						out.write(" x2=\""+StringHelper.formatDouble(m.x)+"\"");
						out.write(" y2=\""+StringHelper.formatDouble(-m.y)+"\"");
						out.write(" stroke=\"black\"");
						//out.write(" stroke-width=\"1\"");
						out.write(" />\n");
					}
					x0=m.x;
					y0=m.y;
					
					break;
				case TOOL_CHANGE:
					break;
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
