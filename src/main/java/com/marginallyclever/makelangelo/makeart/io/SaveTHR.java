package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.MakelangeloVersion;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
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
public class SaveTHR implements TurtleSaver {
	private static final Logger logger = LoggerFactory.getLogger(SaveTHR.class);

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return LoadTHR.filter;
	}

	/**
     * see <a href="http://paulbourke.net/dataformats/dxf/min3d.html">paulbourke.net</a> for details
     */
	@Override
	public boolean save(OutputStream outputStream, Turtle turtle, PlotterSettings settings) throws Exception {
		logger.debug("saving...");

		Rectangle2D.Double dim = turtle.getBounds();
		var diameter = Math.max(dim.width, dim.height);
		if(diameter == 0) diameter = 1;
		var radius = diameter / 2.0;
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		// header
		out.write("# Makelangelo "+ MakelangeloVersion.VERSION + "\n");

		for( var t : turtle.history) {
			double rho = Math.sqrt(t.x * t.x + t.y * t.y) / diameter;
			double theta = Math.atan2(t.y,t.x);
			out.write(String.format("%.2f %.2f\n",theta,rho));
		}

		out.flush();
		logger.debug("done.");
		return true;
	}
}
