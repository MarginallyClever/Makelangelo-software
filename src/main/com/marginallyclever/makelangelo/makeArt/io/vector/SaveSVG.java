package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.makelangelo.makeArt.ImageManipulator;
import com.marginallyclever.makelangeloRobot.Plotter;

/**
 * @author Dan Royer
 * See https://www.w3.org/TR/SVG/paths.html
 */
public class SaveSVG extends ImageManipulator implements TurtleSaver {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("Scaleable Vector Graphics 1.1", "svg");
	
	@Override
	public String getName() { return "SVG"; }
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	@Override
	public boolean save(OutputStream outputStream, Plotter robot) throws Exception {
		Log.message("saving...");
		turtle = robot.getTurtle();

		Rectangle2D.Double dim= turtle.getBounds();
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		// header
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
		out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\""+dim.getX()+" "+dim.getY()+" "+dim.getWidth()+" "+dim.getHeight()+"\">\n");

		boolean isUp=true;
		double x0 = robot.getSettings().getHomeX();
		double y0 = robot.getSettings().getHomeY();

		for( TurtleMove m : turtle.history ) {
			switch(m.type) {
			case TurtleMove.TRAVEL:
				if(!isUp) {
					isUp=true;
				}
				x0=m.x;
				y0=m.y;
				break;
			case TurtleMove.DRAW:
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
			case TurtleMove.TOOL_CHANGE:
				break;
			}
		}
		
		out.write("</svg>");
		out.flush();
		Log.message("done.");
		return true;
	}
}
