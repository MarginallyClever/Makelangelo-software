package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
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
public class Converter_Pulse extends ImageConverter {
	private static double blockScale = 6.0f;
	private static double angle = 0;
	private final String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") };
	private int cutOff = 16;
	private double sampleRate = 0.1;

	public Converter_Pulse() {
		super();

		SelectDouble selectSize = new SelectDouble("size",Translator.get("HilbertCurveSize"),getScale());
		add(selectSize);
		selectSize.addSelectListener(evt->{
			setScale((double) evt.getNewValue());
			fireRestart();
		});

		SelectDouble selectAngle = new SelectDouble("order",Translator.get("HilbertCurveOrder"),angle);
		add(selectAngle);
		selectAngle.addSelectListener(evt->{
			setAngle((double)evt.getNewValue());
			fireRestart();
		});

		SelectDouble selectSampleRate = new SelectDouble("sampleRate",Translator.get("Converter_PulseCMYK.SampleRate"),sampleRate);
		add(selectSampleRate);
		selectSampleRate.addSelectListener(evt->{
			sampleRate = (double) evt.getNewValue();
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("PulseLineName");
	}

	public double getScale() {
		return blockScale;
	}
	public void setScale(double value) {
		if(value<1) value=1;
		blockScale = value;
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double value) {
		angle = value;
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		convertOneLayer(img);

		fireConversionFinished();
	}

	protected void convertOneLayer(TransformedImage img) {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();
		
		// figure out how many lines we're going to have on this image.
		double stepSize = blockScale;

		// from top to bottom of the image...
		double x, y = 0;

		Point2d a = new Point2d();
		Point2d b = new Point2d();
		
		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		var wave = new WaveByIntensity(img,blockScale/2,sampleRate);

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
			turtle.add(wave.lineToWave(a,b));
		}

		CropTurtle.run(turtle, myPaper.getMarginRectangle());
	}

    public int getCutoff() {
		return cutOff;
    }

	public void setCutoff(int value) {
		cutOff=value;
	}
}
