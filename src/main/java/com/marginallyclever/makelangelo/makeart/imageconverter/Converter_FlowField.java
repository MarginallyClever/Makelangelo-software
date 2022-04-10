package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.PerlinNoise;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Uses perlin noise to generate a flow field, then makes the flow lines thicker where the image is darker.
 * @author Dan Royer
 * @since 2022-04-05
 */
public class Converter_FlowField extends ImageConverter {
	private static double  scaleX = 0.01; // controls complexity of curve
	private static double  scaleY = 0.01; // controls complexity of curve
	private static double  offsetX = 0; // controls complexity of curve
	private static double  offsetY = 0; // controls complexity of curve
	private static int     stepSize = 10; // the distance between lines at the edge of the paper, in mm.
	private static boolean rightAngle = false;
	private static double samplingRate = 5;  // the sampling rate along each line, in mm.

	@Override
	public String getName() {
		return Translator.get("Generator_FlowField.name");
	}

	public static void setScaleX(double scaleX) {
		Converter_FlowField.scaleX = scaleX;
	}
	public static void setScaleY(double scaleY) {
		Converter_FlowField.scaleY = scaleY;
	}
	public static void setOffsetX(double offsetX) {
		Converter_FlowField.offsetX = offsetX;
	}
	public static void setOffsetY(double offsetY) {
		Converter_FlowField.offsetY = offsetY;
	}
	public static void setStepSize(int stepSize) {
		Converter_FlowField.stepSize = stepSize;
	}
	public static void setRightAngle(boolean rightAngle) {
		Converter_FlowField.rightAngle = rightAngle;
	}

	public static double getScaleX() {
		return scaleX;
	}
	public static double getScaleY() {
		return scaleY;
	}
	public static double getOffsetX() {
		return offsetX;
	}
	public static double getOffsetY() {
		return offsetY;
	}
	public static int getStepSize() {
		return stepSize;
	}
	public static boolean getRightAngle() {
		return rightAngle;
	}

	private class SampleAt {
		public Point2D p;
		public Point2D n;
		public double value;

		public SampleAt(Point2D p, Point2D n,double value) {
			this.p = p;
			this.n = n;
			this.value = value;
		}
	}

	// move several times along the same line, changing the offset each time based on the intensity of the image.
	protected void convertLine(TransformedImage img, Turtle line) {
		SampleAt [] samples = calculateSamplesOnce(img,line);

		// TODO number of passses should be based on the size of the pen tip.
		double passes=4;
		double halfPasses=(passes-1)/2;
		boolean first = true;

		for(int j=0;j<passes;++j) {
			double offset = (double)j-halfPasses;
			for(SampleAt sample : samples) {
				// move to the adjusted point
				Point2D p3 = new Point2D(sample.n);
				p3.scale(offset*sample.value);
				p3.add(sample.p);
				if(first) {
					turtle.jumpTo(p3.x,p3.y);
					first=false;
				} else {
					turtle.moveTo(p3.x, p3.y);
				}
			}
			// reverse the samples, which reverses the line direction.
			Collections.reverse(Arrays.asList(samples));
		}
	}

	private SampleAt[] calculateSamplesOnce(TransformedImage img, Turtle line) {
		double len = line.getDrawDistance();
		int numSamples = (int)(len/samplingRate);
		SampleAt [] samples = new SampleAt[numSamples];

		Point2D p = line.interpolate(0.0);
		for(int i=0;i<numSamples;i++) {
			Point2D p2 = line.interpolate((double)(i+1)* samplingRate);

			double value = Math.abs(1.0 - (img.sample(p2.x, p2.y, 5.0)/255.0));
			value = Math.max(0,Math.min(255,value));

			Point2D n = new Point2D(p2.y-p.y,-(p2.x-p.x));
			n.normalize();

			samples[i] = new SampleAt(p, n, value);
			p=p2;
		}
		return samples;
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void finish() {
		com.marginallyclever.makelangelo.makeart.imagefilter.Filter_BlackAndWhite bw = new com.marginallyclever.makelangelo.makeart.imagefilter.Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);

		// get all the flow lines.
		List<Turtle> list = fromEdge();
		if(rightAngle) {
			rightAngle=false;
			list.addAll(fromEdge());
			rightAngle=true;
		}

		// make the line thicc.
		turtle = new Turtle();
		for(Turtle t : list) {
			convertLine(img,t);
		}
	}

	private List<Turtle> fromEdge() {
		List<Turtle> list = new ArrayList<Turtle>();

		double xMin = myPaper.getMarginLeft()+stepSize;
		double yMin = myPaper.getMarginBottom()+stepSize;
		double yMax = myPaper.getMarginTop()-stepSize;
		double xMax = myPaper.getMarginRight()-stepSize;
		Rectangle r = new Rectangle((int)xMin,(int)yMin,(int)(xMax-xMin),(int)(yMax-yMin));
		r.grow(1,1);

		for(double y = yMin; y<yMax; y+=stepSize) {
			list.add(makeLine(r, xMin, y));
			//list.add(makeLine(r, xMax, y));
		}
		for(double x = xMin; x<xMax; x+=stepSize) {
			list.add(makeLine(r, x, yMin));
			//list.add(makeLine(r, x, yMax));
		}
		return list;
	}

	private Turtle makeLine(Rectangle r, double x, double y) {
		Turtle line = new Turtle();
		line.jumpTo(x,y);
		// if the first step at this position would be outside the rectangle, reverse the direction.
		double v = PerlinNoise.noise(line.getX() * scaleX + offsetX, line.getY() * scaleY + offsetY, 0);
		line.setAngle(v * 180 + (rightAngle?90:0));
		Vector2d nextStep = line.getHeading();
		nextStep.scale(samplingRate);
		nextStep.add(line.getPosition());
		continueLine(line, r,!r.contains(nextStep.x,nextStep.y));
		return line;
	}

	private void continueLine(Turtle line, Rectangle r, boolean reverse) {
		for(int i=0;i<200;++i) {
			double v = PerlinNoise.noise(line.getX() * scaleX + offsetX, line.getY() * scaleY + offsetY, 0);
			line.setAngle(v * 180 + (rightAngle?90:0));
			Vector2d nextStep = line.getHeading();
			nextStep.scale(reverse?-samplingRate : samplingRate);
			nextStep.add(line.getPosition());
			// stop if we leave the rectangle
			if(!r.contains(nextStep.x,nextStep.y)) break;
			line.moveTo(nextStep.x,nextStep.y);
		}
	}
}
