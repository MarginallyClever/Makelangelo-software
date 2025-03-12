package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Horizontal lines.  The height of each line is determined by the average intensity of the pixels in that line.
 * Masks the lines in the "back" by the lines in the "front" using a height buffer.
 * @author Dan Royer
 * @since 7.40.3
 */
public class Converter_IntensityToHeight extends ImageConverter {
	// vertical distance between lines
	private static int spacing = 2;
	// horizontal distance between samples.  more samples = more detail.
	private static int sampleRate = 5;
	// max height of the wave will be +/-(waveIntensity/2)
	private static int waveIntensity = 30;
	// track the height of each line as they are added.  Use this to mask the later lines.
	private final List<Double> heights = new ArrayList<>();

	public Converter_IntensityToHeight() {
		super();

		SelectSlider selectSize = new SelectSlider("size",Translator.get("Converter_IntensityToHeight.spacing"), 20,1,getSpacing());
		SelectSlider selectSampleRate = new SelectSlider("sampleRate",Translator.get("Converter_IntensityToHeight.sampleRate"),20,1,getSampleRate());
		SelectSlider selectWaveIntensity = new SelectSlider("waveIntensity",Translator.get("Converter_IntensityToHeight.waveIntensity"),50,-50,getWaveIntensity());

		add(selectSize);
		add(selectSampleRate);
		add(selectWaveIntensity);

		selectSize.addSelectListener(evt->{
			setSpacing((int) evt.getNewValue());
			fireRestart();
		});
		selectSampleRate.addSelectListener(evt->{
			setSampleRate((int) evt.getNewValue());
			fireRestart();
		});
		selectWaveIntensity.addSelectListener(evt->{
			setWaveIntensity((int) evt.getNewValue());
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("Converter_IntensityToHeight.name");
	}

	public int getSpacing() {
		return spacing;
	}
	public void setSpacing(int value) {
		spacing = Math.max(1,value);
	}

	public int getSampleRate() {
		return sampleRate;
	}
	public void setSampleRate(int value) {
		sampleRate = Math.max(1,value);
	}

	public int getWaveIntensity(){
		return waveIntensity;
	}
	public void setWaveIntensity(int value){
		waveIntensity = value;
	}

	/**
	 * Travel from a to b, sampling the image at regular intervals.  Move the turtle in the y-axis by the sampled height.
	 * @param a start point
	 * @param b end point
	 * @param img the image to sample
	 * @param numSamples how many samples to take
	 * @param sampleRadius how far to move the turtle in the y-axis
	 * @return a list of points
	 */
	protected List<Point2d> convertLine(TransformedImage img, Point2d a, Point2d b, int numSamples, double sampleRadius) {
		List<Point2d> points = new ArrayList<>();

		var dir = new Vector2d(b.x-a.x,b.y-a.y);
		double len = dir.length();
		dir.scale(1.0/len);


		for(double p = 0; p <= numSamples; ++p) {
			double fraction = p * len / numSamples;
			double x = a.x + dir.x * fraction;
			double y = a.y + dir.y * fraction;
			// sample the image and scale the result.
			double z = 1.0 - img.sample( x, y, sampleRadius ) / 255.0f;
			//z *= z;  // quadratic curve
			double py = y + waveIntensity * z - waveIntensity/2.0f;
			points.add(new Point2d(x,py));
		}
		return points;
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();

		// from bottom to top of the image...
		int i=0;
		Point2d lineStart = new Point2d();
		Point2d lineEnd = new Point2d();
		
		turtle = new Turtle();

		heights.clear();
		// heights should contain (xRight-xLeft) / sampleRate values
		int numSamples = (int)Math.ceil(Math.abs(xRight-xLeft)/sampleRate);
		for(int j=0;j<=numSamples;++j) {
			heights.add(yBottom-1);
		}

		// horizontal
		for (double y = yBottom; y < yTop; y += spacing) {
			// flip the direction of the line so the pen makes a zigzag
			if ((++i % 2) == 0) {
				lineStart.set(xLeft,y);
				lineEnd.set(xRight,y);
			} else {
				lineStart.set(xRight,y);
				lineEnd.set(xLeft,y);
			}
			// because the line direction is flipped every turn, the height buffer must also be flipped every turn.
			Collections.reverse(heights);

			// sample the image along the line
			List<Point2d> points = convertLine(img,lineStart,lineEnd,numSamples,sampleRate);

			boolean first=true;
			// mask the line using the heights values and update heights as we go
			for(int j=0;j<=numSamples;++j) {
				Point2d p = points.get(j);
				double x = p.x;
				double heightNew = p.y;
				double heightOld = heights.get(j);
				if(heightNew < heightOld) {
					heightNew = heightOld;
				}
				heights.set(j,heightNew);

				if(first) {
					turtle.jumpTo(px+x, py+heightNew);
					first = false;
				} else {
					turtle.moveTo(px+x, py+heightNew);
				}
			}
		}

		fireConversionFinished();
	}
}
