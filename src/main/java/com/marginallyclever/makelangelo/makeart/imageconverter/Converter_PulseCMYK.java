package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.makeart.turtletool.CropTurtle;
import com.marginallyclever.makelangelo.makeart.turtletool.WaveByIntensity;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * straight lines pulsing like a heartbeat.  height and density of pulse vary with image intensity.
 * @author Dan Royer
 */
public class Converter_PulseCMYK extends ImageConverter {
	private static double blockScale = 4.0f;
	private static double angle = 0;
	private double sampleRate = 0.2;

	public Converter_PulseCMYK() {
		super();

		SelectDouble    selectSize = new SelectDouble("size",Translator.get("HilbertCurveSize"),getScale());
		add(selectSize);
		selectSize.addSelectListener(evt->{
			setScale((double) evt.getNewValue());
			fireRestart();
		});

		SelectDouble selectAngle = new SelectDouble("angle",Translator.get("ConverterMultipassAngle"),angle);
		add(selectAngle);
		selectAngle.addSelectListener(evt->{
			angle = (double)evt.getNewValue();
			fireRestart();
		});

		SelectDouble    selectSampleRate = new SelectDouble("sampleRate",Translator.get("Converter_PulseCMYK.SampleRate"),sampleRate);
		add(selectSampleRate);
		selectSampleRate.addSelectListener(evt->{
			sampleRate = (double) evt.getNewValue();
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("Converter_PulseCMYK.Name");
	}

	public double getScale() {
		return blockScale;
	}

	public void setScale(double value) {
		if(value<1) value=1;
		blockScale = value;
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterCMYK cmyk = new FilterCMYK(myImage);
		cmyk.filter();

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		outputChannel(cmyk.getY(),Color.YELLOW);
		outputChannel(cmyk.getC(),Color.CYAN);
		outputChannel(cmyk.getM(),Color.MAGENTA);
		outputChannel(cmyk.getK(),Color.BLACK);

		fireConversionFinished();
	}

	protected void outputChannel(TransformedImage img,Color channel) {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();

		Point2d a = new Point2d();
		Point2d b = new Point2d();

		Turtle newTurtle = new Turtle();
		newTurtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		var wave = new WaveByIntensity(img,blockScale/2,sampleRate,2.0);

		Vector2d majorAxis = new Vector2d(
				Math.cos(Math.toRadians(angle)),
				Math.sin(Math.toRadians(angle))
		);
		Vector2d minorAxis = new Vector2d(majorAxis.y, -majorAxis.x); // perpendicular to major axis
		double height = yTop-yBottom;
		double width = xRight-xLeft;
		double r = Math.sqrt(Math.pow(width/2,2) + Math.pow(height/2,2));

		double i=-r;
		for(double j =-r; j <= r; j+= blockScale) {
			i = -i;
			a.scale(j,majorAxis);
			b.scale(j,majorAxis);
			a.scaleAdd(-i,minorAxis,a);
			b.scaleAdd(i,minorAxis,b);
			newTurtle.add(wave.lineToWave(a,b));
		}

		for(var layer : newTurtle.getLayers()) {
			layer.setColor(channel);
		}

		CropTurtle.run(newTurtle, myPaper.getMarginRectangle());
		turtle.add(newTurtle);
	}
}
