package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
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

	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 */
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

		TurtleMove previousMove = null;
		Color downColor = Color.BLACK;

		for (TurtleMove m : myTurtle.history) {
			if (m == null) throw new NullPointerException();

			switch (m.type) {
				case TRAVEL -> {
					previousMove = m;
				}
				case DRAW_LINE -> {
					if (previousMove != null) {
						g.setColor(downColor);
						g.drawLine((int) previousMove.x, (int) -previousMove.y, (int) m.x, (int) -m.y);
					}
					previousMove = m;
				}
				case TOOL_CHANGE -> {
					downColor = m.getColor();
					g.setStroke(new BasicStroke((int) m.getDiameter()));
				}
			}
		}

		ImageIO.write(img, extension, outputStream);
		// webp requires a flush or there will be a zero-length file.
		outputStream.flush();

		logger.debug("done.");
		return true;
	}
}
