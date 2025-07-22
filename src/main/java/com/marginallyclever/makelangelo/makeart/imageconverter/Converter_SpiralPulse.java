package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_SpiralPulse extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_SpiralPulse.class);
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.
	private static double zigDensity = 1.2f;  // increase to tighten zigzags
	private static double spacing = 2.5f;
	private static double height = 4.0f;

	public Converter_SpiralPulse() {
		super();
		SelectDouble selectIntensity = new SelectDouble("intensity", Translator.get("SpiralPulse.intensity"),getIntensity());
		add(selectIntensity);
		selectIntensity.addSelectListener(evt->{
			setIntensity((double)evt.getNewValue());
			fireRestart();
		});
		SelectDouble selectSpacing = new SelectDouble("spacing",Translator.get("SpiralPulse.spacing"),getSpacing());
		add(selectSpacing);
		selectSpacing.addSelectListener(evt->{
			setSpacing((double)evt.getNewValue());
			fireRestart();
		});
		SelectDouble selectHeight = new SelectDouble("height",Translator.get("SpiralPulse.height"),getHeight());
		add(selectHeight);
		selectHeight.addSelectListener(evt->{
			setHeight((double)evt.getNewValue());
			fireRestart();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("SpiralPulseName");
	}

	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		// black and white
		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		double toolDiameter = 1;

		double maxr;

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		if (convertToCorners) {
			// go right to the corners
			double h2 = rect.getHeight();
			double w2 = rect.getWidth();
			maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
		} else {
			// do the largest circle that still fits in the margin.
			double w = rect.getWidth()/2.0f;
			double h = rect.getHeight()/2.0f;
			maxr = Math.min(h, w);
		}
		
		double r = maxr - toolDiameter*5.0f, f;
		double fx, fy;
		int numRings = 0;
		double stepSize = toolDiameter * height;
		double halfStep = stepSize / 2.0f;
		double zigZagSpacing = toolDiameter;
		int n=1;
		double PULSE_MINIMUM = 0.1f;
		double ringSize = halfStep*spacing;
		boolean init = false;
		int i;
		int z = 0;
		double r2,scale_z,pulse_size,nx,ny;

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();
		
		while (r > toolDiameter) {
			// find circumference of current circle
			double circumference =  Math.floor((2.0f * r - toolDiameter) * Math.PI)*zigDensity;
			//if (circumference > 360.0f) circumference = 360.0f;
			
			for (i = 0; i <= circumference; ++i) {
				// tweak the diameter to make it look like a spiral
				r2 = r - ringSize * (float)i / circumference;
				
				f = Math.PI * 2.0f * (float)i / circumference;
				fx = Math.cos(f) * r2;
				fy = Math.sin(f) * r2;
				// clip to paper boundaries
				if( rect.contains(fx, fy) ) {
					z = img.sample( fx, fy, halfStep);
					scale_z = (255.0f - z) / 255.0f;
					pulse_size = halfStep * scale_z;
					nx = (halfStep+pulse_size*n) * fx / r2;
					ny = (halfStep+pulse_size*n) * fy / r2;

					if (!init) {
						turtle.moveTo(px+fx+nx, py+fy+ny);
						init = true;
					}
					if(pulse_size < PULSE_MINIMUM) turtle.penUp();
					else turtle.penDown();
					turtle.moveTo(px+fx+nx, py+fy + ny);
					n = -n;
				} else {
					if (!init) {
						init = true;
					}
					turtle.penUp();
					turtle.moveTo(px+fx, py+fy);
				}
			}
			n = -n;
			r -= ringSize;
			++numRings;
		}

		logger.debug("{} rings.", numRings);

		fireConversionFinished();
	}

	public void setIntensity(double v) {
		if(v<0.1) v=0.1f;
		if(v>3.0) v=3.0f;
		zigDensity=v;
	}
	public double getIntensity() {
		return zigDensity;
	}

	public void setSpacing(double v) {
		if(v<0.5f) v=0.5f;
		if(v>10) v=10;
		spacing=v;
	}
	public double getSpacing() {
		return spacing;
	}

	public void setHeight(double v) {
		if(v<0.1) v=1;
		if(v>10) v=10;
		height = v;
	}
	public double getHeight() {
		return height;
	}
}