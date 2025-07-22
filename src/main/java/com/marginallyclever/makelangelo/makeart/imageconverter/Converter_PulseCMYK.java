package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterCMYK;
import com.marginallyclever.makelangelo.makeart.turtletool.WaveByIntensity;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * straight lines pulsing like a heartbeat.  height and density of pulse vary with image intensity.
 * @author Dan Royer
 */
public class Converter_PulseCMYK extends ImageConverter {
	private static double blockScale = 4.0f;
	private static int direction = 0;
	private final String[] directionChoices = new String[]{Translator.get("horizontal"), Translator.get("vertical") };
	private double sampleRate = 0.2;

	public Converter_PulseCMYK() {
		super();

		SelectDouble    selectSize = new SelectDouble("size",Translator.get("HilbertCurveSize"),getScale());
		SelectOneOfMany selectDirection = new SelectOneOfMany("direction",Translator.get("Direction"),getDirections(),getDirectionIndex());
		SelectDouble    selectSampleRate = new SelectDouble("sampleRate",Translator.get("Converter_PulseCMYK.SampleRate"),sampleRate);

		add(selectSize);
		add(selectDirection);
		add(selectSampleRate);

		selectSize.addSelectListener(evt->{
			setScale((double) evt.getNewValue());
			fireRestart();
		});
		selectDirection.addSelectListener(evt->{
			setDirectionIndex((int) evt.getNewValue());
			fireRestart();
		});
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

	public String[] getDirections() {
		return directionChoices;
	}

	public int getDirectionIndex() {
		return direction;
	}

	public void setDirectionIndex(int value) {
		if(value<0) value=0;
		if(value>=directionChoices.length) value=directionChoices.length-1;
		direction = value;
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

		// from top to bottom of the image...
		double x, y = 0;
		int i=0;

		Point2d a = new Point2d();
		Point2d b = new Point2d();

		Turtle newTurtle = new Turtle();
		newTurtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

		var wave = new WaveByIntensity(img,blockScale/2,sampleRate);
		
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += blockScale) {
				++i;
				if ((i % 2) == 0) {
					a.set(xLeft,y);
					b.set(xRight,y);
				} else {
					a.set(xRight,y);
					b.set(xLeft,y);
				}
				newTurtle.add(wave.lineToWave(a,b));
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += blockScale) {
				++i;
				if ((i % 2) == 0) {
					a.set(x,yBottom);
					b.set(x,yTop);
				} else {
					a.set(x,yTop);
					b.set(x,yBottom);
				}
				newTurtle.add(wave.lineToWave(a,b));
			}
		}

		for(var layer : newTurtle.getLayers()) {
			layer.setColor(channel);
		}
		turtle.add(newTurtle);
	}
}
