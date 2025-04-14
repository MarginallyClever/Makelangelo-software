package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

/**
 * Save {@link Turtle} to any bitmap format supported by {@link ImageIO}.
 * @author Dan Royer
 */
public class SaveBitmap implements TurtleSaver {
	private static final Logger logger = LoggerFactory.getLogger(SaveBitmap.class);
	private final FileNameExtensionFilter filter;
	private final boolean supportsAlpha;
	private final String extension;

	SaveBitmap(String extension,boolean supportsAlpha) {
		super();
		this.extension = extension;
		this.supportsAlpha = supportsAlpha;
		this.filter = new FileNameExtensionFilter(extension,extension);
	}

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean save(OutputStream outputStream, Turtle myTurtle, PlotterSettings settings) throws Exception {
		logger.debug("saving {}...",extension);

		Rectangle2D r = myTurtle.getBounds();
		int h = (int)Math.ceil(r.getHeight());
		int w = (int)Math.ceil(r.getWidth());
		BufferedImage img = new BufferedImage(w,h,supportsAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		if(!supportsAlpha) {
			g.setColor(Color.WHITE);
			g.fillRect(0,0,w,h);
		}
		g.translate(-r.getX(),-r.getY());

		for( var layer : myTurtle.getLayers() ) {
			if(layer.isEmpty()) continue;
			g.setColor(layer.getColor());
			g.setStroke(new BasicStroke((int)layer.getDiameter()));
			for (var line : layer.getAllLines()) {
				if(line.size()<2) continue;
				var iter = line.getAllPoints().iterator();
				var prev = iter.next();
				while(iter.hasNext()) {
					var next = iter.next();
					g.drawLine((int)prev.x,(int)-prev.y,
							(int)next.x,(int)-next.y);
					prev = next;
				}
			}
		}

		ImageIO.write(img, extension, outputStream);
		// flush to ensure all data is written
		outputStream.flush();

		logger.debug("done.");
		return true;
	}
}
