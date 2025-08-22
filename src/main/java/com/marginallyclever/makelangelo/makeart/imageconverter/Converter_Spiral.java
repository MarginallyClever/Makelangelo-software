package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Generate a spiral pattern across the image.
 * Darken areas of the image by lowering the pen to draw more closely spaced lines.
 */
public class Converter_Spiral extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_Spiral.class);
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.

	public Converter_Spiral() {
		super();
		SelectBoolean selectToCorners = new SelectBoolean("toCorners", Translator.get("Spiral.toCorners"),getToCorners());
		add(selectToCorners);

		selectToCorners.addSelectListener((evt) -> {
			setToCorners((boolean)evt.getNewValue());
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("SpiralName");
	}


	public boolean getToCorners() {
		return convertToCorners;
	}
	
	public void setToCorners(boolean arg0) {
		convertToCorners=arg0;
	}
	
	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		double cx = paper.getCenterX();
		double cy = paper.getCenterY();

		// black and white
		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		double maxr;
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double h2 = rect.getHeight()/2.0f;
		double w2 = rect.getWidth()/2.0f;
		if (convertToCorners) {
			maxr = Math.sqrt(h2 * h2 + w2 * w2);
		} else {
			// do the largest circle that still fits in the image.
			maxr = Math.min(h2, w2);
		}

		double toolDiameter = 1;

		int i, j;
		final int steps = 4;
		double leveladd = 255.0 / (double)(steps+1);
		double level;
		int z = 0;

		double r = maxr;
		double fx, fy;
		int numRings = 0;
		j = 0;
		while (r > toolDiameter) {
			++j;
			double level0 = leveladd * (1+(j%steps));
			double level1 = leveladd * (1+((j+1)%steps));
			
			// find circumference of current circle
			double c1 = Math.floor((2.0 * r - toolDiameter) * Math.PI);

			for (i = 0; i < c1; ++i) {
				double p = (double)i / c1;
				double f = Math.PI * 2.0 * p;
				double r1 = r - toolDiameter * p;
				fx = Math.cos(f) * r1;
				fy = Math.sin(f) * r1;

				if(rect.contains(fx, fy)) {
					try {
						z = img.sample(fx, fy,1);
					} catch(Exception e) {
						logger.error("Failed to sample", e);
					}

					level = (level1-level0)*p + level0;
					if(z<level) turtle.penDown();
					else turtle.penUp();
				} else turtle.penUp();

				turtle.moveTo(cx + fx, cy + fy);
			}
			r -= toolDiameter;
			++numRings;
		}

		logger.debug("{} rings.", numRings);

		fireConversionFinished();
	}
}