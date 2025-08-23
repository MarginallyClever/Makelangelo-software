package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.makeart.turtletool.CropTurtle;
import com.marginallyclever.makelangelo.makeart.turtletool.WaveByIntensity;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_SpiralPulseCMYK extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_SpiralPulseCMYK.class);
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.
	private static double zigDensity = 2.0f;  // increase to tighten zigzags
	private static double spacing = 2.5f;
	private static double height = 4.0f;
	private static double sampleRate = 0.1;

	public Converter_SpiralPulseCMYK() {
		super();

		SelectBoolean toCorners = new SelectBoolean("toCorners", Translator.get("Converter_SpiralPulse.toCorners"), convertToCorners);
		toCorners.addSelectListener(evt->{
			convertToCorners = (boolean)evt.getNewValue();
			fireRestart();
		});
		add(toCorners);

		SelectSlider selectIntensity = new SelectSlider("intensity", Translator.get("Converter_SpiralPulse.intensity"),30,1,(int)(zigDensity*10));
		add(selectIntensity);
		selectIntensity.addSelectListener(evt->{
			zigDensity = (int)evt.getNewValue() / 10.0;
			fireRestart();
		});

		SelectDouble selectSpacing = new SelectDouble("spacing",Translator.get("Converter_SpiralPulse.spacing"),getSpacing());
		add(selectSpacing);
		selectSpacing.addSelectListener(evt->{
			setSpacing((double)evt.getNewValue());
			fireRestart();
		});

		SelectDouble selectHeight = new SelectDouble("height",Translator.get("Converter_SpiralPulse.height"),getHeight());
		add(selectHeight);
		selectHeight.addSelectListener(evt->{
			setHeight((double)evt.getNewValue());
			fireRestart();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_SpiralPulseCMYK.name");
	}

	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK, settings.getDouble(PlotterSettings.DIAMETER));

		outputChannel(cmyk.getY(), Color.YELLOW);
		outputChannel(cmyk.getC(), Color.CYAN);
		outputChannel(cmyk.getM(), Color.MAGENTA);
		outputChannel(cmyk.getK(), Color.BLACK);

		fireConversionFinished();
	}

	private void outputChannel(TransformedImage img,Color channel) {
		Turtle newTurtle = new Turtle();
		newTurtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		double toolDiameter = newTurtle.getDiameter();

		double maxr;

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		if (convertToCorners) {
			// go right to the corners
			double h2 = rect.getHeight();
			double w2 = rect.getWidth();
			maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
		} else {
			// do the largest circle that still fits in the margin.
			double w = rect.getWidth() / 2.0f;
			double h = rect.getHeight() / 2.0f;
			maxr = Math.min(h, w);
		}

		double r = maxr - toolDiameter * 5.0f;
		double stepSize = toolDiameter * height;
		double halfStep = stepSize / 2.0f;
		int n = 1;
		double ringSize = halfStep * spacing;

		var wave = new WaveByIntensity(img,halfStep,sampleRate,zigDensity);

		Point2d a = new Point2d();
		Point2d b = new Point2d();

		a.set(Math.cos(0) * r, Math.sin(0) * r);

		while (r > toolDiameter) {
			// find circumference of current circle
			double circumference =  Math.floor((2.0f * r - toolDiameter) * Math.PI)*toolDiameter;
			//if (circumference > 360.0f) circumference = 360.0f;
			
			for (int i = 0; i <= circumference; ++i) {
				// tweak the diameter to make it look like a spiral
				double r2 = r - ringSize * (float)i / circumference;
				
				double f = Math.PI * 2.0f * (float)i / circumference;
				b.set(Math.cos(f) * r2, Math.sin(f) * r2);

				newTurtle.add(wave.lineToWave(a,b));
				a.set(b);
			}
			n = -n;
			r -= ringSize;
		}

		for(var layer : newTurtle.getLayers()) {
			layer.setColor(channel);
		}

		CropTurtle.run(newTurtle, myPaper.getMarginRectangle());

		turtle.add(newTurtle);
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