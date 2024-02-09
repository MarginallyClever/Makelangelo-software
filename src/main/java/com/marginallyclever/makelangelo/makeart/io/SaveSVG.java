package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.ColorPalette;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Dan Royer
 * See <a href="https://www.w3.org/TR/SVG/paths.html">...</a>
 */
public class SaveSVG implements TurtleSaver {
	private static final Logger logger = LoggerFactory.getLogger(SaveSVG.class);
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("Scaleable Vector Graphics 1.1", "svg");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	/**
     * see <a href="http://paulbourke.net/dataformats/dxf/min3d.html">paulbourke.net</a> for details
     */
	@Override
	public boolean save(OutputStream outputStream, Turtle turtle, PlotterSettings settings) throws Exception {
		logger.debug("saving...");

		Rectangle2D.Double dim= turtle.getBounds();
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		String viewBox = StringHelper.formatDouble(dim.getMinX())+" "
						+StringHelper.formatDouble(-dim.getMaxY())+" "
						+StringHelper.formatDouble(dim.getWidth())+" "
						+StringHelper.formatDouble(dim.getHeight())+"\"";
		// header
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
		out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n"); // viewbox would go in svg tag

		boolean isUp=true;
		double x0 = turtle.history.get(0).x;
		double y0 = turtle.history.get(0).y;
		boolean hasStarted=false;
		for( TurtleMove m : turtle.history ) {
			switch(m.type) {
			case TRAVEL:
				isUp=true;
				x0=m.x;
				y0=m.y;
				break;
			case DRAW_LINE:
				if(isUp) {
					isUp=false;
					out.write("M ");
					out.write(StringHelper.formatDouble(x0)+" ");
					out.write(StringHelper.formatDouble(-y0)+" ");
					out.write("L ");
				}
				out.write(StringHelper.formatDouble(m.x)+" ");
				out.write(StringHelper.formatDouble(-m.y)+" ");
				x0=m.x;
				y0=m.y;
				
				break;
			case TOOL_CHANGE:
				if(hasStarted) {
					out.write("'/>\n");
				}
				out.write("  <path fill='none' stroke='"+ ColorPalette.getHexCode(m.getColor())+"' d='");
				isUp=true;
				hasStarted=true;
				break;
			}
		}
		if(hasStarted) {
			out.write("'/>\n");
		}

		out.write("</svg>");
		out.flush();
		logger.debug("done.");
		return true;
	}
}
