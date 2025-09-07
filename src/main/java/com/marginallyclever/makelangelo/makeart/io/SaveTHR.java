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
 * Save {@link Turtle} into a THR file for sand plotters.
 * @author Dan Royer
 * See <a href="https://sisyphus-industries.com/wp-content/uploads/wpforo/default_attachments/1568584503-sisyphus-table-programming-logic3.pdf">THR</a>
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

		double previousTheta = 0;

		for( var layer : turtle.getLayers() ) {
            if(!layer.isVisible()) continue;
			for( var line : layer.getAllLines() ) {
				for( var p : line.getAllPoints() ) {
					// turn x,y to theta,rho
					double rho = Math.sqrt(p.x * p.x + p.y * p.y) / radius;
					double theta = Math.PI/2 - Math.atan2(p.y,p.x);
					// handle the case where the angle wraps around.
					if(theta<previousTheta-Math.PI) theta += Math.PI*2;
					if(theta>previousTheta+Math.PI) theta -= Math.PI*2;
					previousTheta = theta;

					out.write(String.format("%.5f %.5f\n",theta,rho));
				}
			}
		}

		out.flush();
		logger.debug("done.");
		return true;
	}
}
