package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.ColorPalette;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Dan Royer
 * See <a href="https://www.w3.org/TR/SVG/paths.html">...</a>
 */
public class SaveSVG implements TurtleSaver {
	private static final Logger logger = LoggerFactory.getLogger(SaveSVG.class);
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("Scalable Vector Graphics 1.1", "svg");
	
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

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));
		String viewBox = StringHelper.formatDouble(dim.getMinX())+" "
						+StringHelper.formatDouble(-dim.getMaxY())+" "
						+StringHelper.formatDouble(dim.getWidth())+" "
						+StringHelper.formatDouble(dim.getHeight())+"\"";
		// header
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		out.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
		out.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n"); // viewbox would go in svg tag

		for( var layer : turtle.getLayers() ) {
			if(layer.isEmpty()) continue;
			StringBuilder b = new StringBuilder();
			b.append("  <path fill='none' stroke='")
					.append(ColorPalette.getHexCode(layer.getColor()))
					.append("' d='");

			for (var line : layer.getAllLines()) {
				if(line.size()<2) continue;
				var iter = line.getAllPoints().iterator();
				var p = iter.next();

				b.append("M ")
						.append(StringHelper.formatDouble( p.x)).append(" ")
						.append(StringHelper.formatDouble(-p.y)).append(" ")
						.append("L ");
				while(iter.hasNext()) {
					p = iter.next();
					b.append(StringHelper.formatDouble( p.x)).append(" ")
					 .append(StringHelper.formatDouble(-p.y)).append(" ");
				}
			}

			b.append("'/>\n");
			out.write(b.toString());
		}

		out.write("</svg>");
		out.flush();
		logger.debug("done.");
		return true;
	}
}
