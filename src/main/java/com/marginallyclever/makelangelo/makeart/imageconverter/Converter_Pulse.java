package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectSlider;
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
	private static double zigDensity = 2.0f;  // increase to tighten zigzags
	private int cutOff = 16;
	private double sampleRate = 0.1;

	public Converter_Pulse() {
		super();

		SelectSlider selectSize = new SelectSlider("size",Translator.get("HilbertCurveSize"),40,1,(int)blockScale);
		add(selectSize);
		selectSize.addSelectListener(evt->{
			blockScale = (int)evt.getNewValue();
			fireRestart();
		});

		SelectSlider selectIntensity = new SelectSlider("intensity", Translator.get("Converter_SpiralPulse.intensity"),30,1,(int)(zigDensity*10));
		add(selectIntensity);
		selectIntensity.addSelectListener(evt->{
			zigDensity = (int)evt.getNewValue() / 10.0;
			fireRestart();
		});

		SelectSlider selectAngle = new SelectSlider("angle", Translator.get("ConverterMultipassAngle"),90,0,(int)angle);
		add(selectAngle);
		selectAngle.addSelectListener(evt->{
			angle = (int)evt.getNewValue();
			fireRestart();
		});

		SelectSlider selectSampleRate = new SelectSlider("sampleRate",Translator.get("Converter_PulseCMYK.SampleRate"),20,1,(int)(sampleRate*10));
		add(selectSampleRate);
		selectSampleRate.addSelectListener(evt->{
			sampleRate = (int) evt.getNewValue() / 10.0;
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("PulseLineName");
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

		Point2d a = new Point2d();
		Point2d b = new Point2d();
		
		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		var wave = new WaveByIntensity(img,blockScale/2,sampleRate,zigDensity);

		Vector2d majorAxis = new Vector2d(
				Math.cos(Math.toRadians(angle)),
				Math.sin(Math.toRadians(angle))
		);
		Vector2d minorAxis = new Vector2d(majorAxis.y, -majorAxis.x); // perpendicular to major axis
		double height = yTop-yBottom;
		double width = xRight-xLeft;
		double r = Math.sqrt(Math.pow(width/2,2) + Math.pow(height/2,2));
        boolean isFirst=true;
		double i=-r;
		for(double j =-r; j <= r; j+= blockScale) {
			i = -i;
			a.scale(j,majorAxis);
			b.scale(j,majorAxis);
			a.scaleAdd(-i,minorAxis,a);
			b.scaleAdd(i,minorAxis,b);
			wave.lineToWave(turtle,a,b,isFirst);
            isFirst=false;
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
