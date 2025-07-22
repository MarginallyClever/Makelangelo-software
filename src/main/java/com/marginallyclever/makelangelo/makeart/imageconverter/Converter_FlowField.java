package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.convenience.noise.Noise;
import com.marginallyclever.convenience.noise.NoiseFactory;
import com.marginallyclever.convenience.noise.PerlinNoise;
import com.marginallyclever.donatello.select.*;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
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
	private static int stepSize = 10; // the distance between lines at the edge of the paper, in mm.
	private static int stepLength = 10;
	private static int stepVariation = 5;
	private static boolean rightAngle = false;
	private static double samplingRate = 5;  // the sampling rate along each line, in mm.
	private static boolean fromEdge = false;  // continuous lines
	private static int cutoff = 128;  // the sampling rate along each line, in mm.
	private static int seed=0;
	private static final Random random = new Random();

	private Noise noiseMaker = new PerlinNoise();

	public Converter_FlowField() {
		super();
		SelectRandomSeed fieldRandomSeed = new SelectRandomSeed("randomSeed",Translator.get("Generator.randomSeed"),seed);
		SelectOneOfMany fieldNoise = new SelectOneOfMany("noiseType",Translator.get("Generator_FlowField.noiseType"), NoiseFactory.getNames(),0);
		SelectDouble selectScaleX = new SelectDouble("scaleX", Translator.get("Generator_FlowField.scaleX"), getScaleX());
		SelectDouble selectScaleY = new SelectDouble("scaleY", Translator.get("Generator_FlowField.scaleY"), getScaleY());
		SelectDouble selectOffsetX = new SelectDouble("offsetX", Translator.get("Generator_FlowField.offsetX"), getOffsetX());
		SelectDouble selectOffsetY = new SelectDouble("offsetY", Translator.get("Generator_FlowField.offsetY"), getOffsetY());
		SelectSlider selectStepSize = new SelectSlider("stepSize", Translator.get("Generator_FlowField.stepSize"), 20, 2, getStepSize());
		SelectSlider fieldStepVariation = new SelectSlider("stepVariation",Translator.get("Generator_FlowField.stepVariation"),20,0,stepVariation);
		SelectSlider fieldStepLength = new SelectSlider("stepLength",Translator.get("Generator_FlowField.stepLength"),20,1,stepLength);
		SelectBoolean fieldFromEdge = new SelectBoolean("fromEdge",Translator.get("Generator_FlowField.fromEdge"),fromEdge);
		SelectBoolean selectRightAngle = new SelectBoolean("rightAngle", Translator.get("Generator_FlowField.rightAngle"), getRightAngle());
		SelectSlider selectCutoff = new SelectSlider("cutoff", Translator.get("Converter_VoronoiStippling.Cutoff"), 255,0, cutoff);

		add(fieldRandomSeed);
		fieldRandomSeed.addSelectListener(evt->{
			seed = (int)evt.getNewValue();
			fireRestart();
		});

		add(fieldNoise);
		fieldNoise.addSelectListener(evt->{
			noiseMaker = NoiseFactory.getNoise(fieldNoise.getSelectedIndex());
			fireRestart();
		});

		add(selectScaleX);
		add(selectScaleY);
		add(selectOffsetX);
		add(selectOffsetY);
		add(selectStepSize);
		add(fieldStepVariation);
		add(fieldStepLength);
		add(fieldFromEdge);
		add(selectRightAngle);
		add(selectCutoff);

		selectScaleX.addSelectListener((evt)->{
			setScaleX((double)evt.getNewValue());
			fireRestart();
		});
		selectScaleY.addSelectListener((evt)->{
			setScaleY((double)evt.getNewValue());
			fireRestart();
		});
		selectOffsetX.addSelectListener((evt)->{
			setOffsetX((double)evt.getNewValue());
			fireRestart();
		});
		selectOffsetY.addSelectListener((evt)->{
			setOffsetY((double)evt.getNewValue());
			fireRestart();
		});
		selectStepSize.addSelectListener((evt)->{
			setStepSize((int)evt.getNewValue());
			fireRestart();
		});
		fieldStepLength.addSelectListener((evt)->{
			setStepLength((int)evt.getNewValue());
			fireRestart();
		});
		fieldStepVariation.addSelectListener((evt)->{
			setStepVariation((int)evt.getNewValue());
			fireRestart();
		});
		fieldFromEdge.addSelectListener((evt)->{
			setFromEdge((boolean)evt.getNewValue());
			fireRestart();
		});
		selectRightAngle.addSelectListener((evt)->{
			setRightAngle((boolean)evt.getNewValue());
			fireRestart();
		});
		selectCutoff.addSelectListener((evt)->{
			setCutoff((int)evt.getNewValue());
			fireRestart();
		});

		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Perlin_noise'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

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
	public static void setStepLength(int stepLength) {
		Converter_FlowField.stepLength = stepLength;
	}
	public static void setStepVariation(int stepVariation) {
		Converter_FlowField.stepVariation = stepVariation;
	}
	public static void setFromEdge(boolean fromEdge) {
		Converter_FlowField.fromEdge = fromEdge;
	}
	public static void setRightAngle(boolean rightAngle) {
		Converter_FlowField.rightAngle = rightAngle;
	}
	public static void setCutoff(int cutoff) {
		Converter_FlowField.cutoff = cutoff;
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

	private static class SampleAt {
		public Point2d p;
		public Vector2d n;
		public double value;

		public SampleAt(Point2d p, Vector2d n,double value) {
			this.p = p;
			this.n = n;
			this.value = value;
		}
	}

	// move several times along the same line, changing the offset each time based on the intensity of the image.
	protected void convertLine(TransformedImage img, Turtle line) {
		SampleAt [] samples = calculateSamplesOnce(img,line);

		// TODO number of passes should be based on the size of the pen tip.
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();

		double passes=4;
		double halfPasses=(passes-1)/2;
		boolean first = true;

		for(int j=0;j<passes;++j) {
			double offset = (double)j-halfPasses;
			for(SampleAt sample : samples) {
				// move to the adjusted point
				Point2d p3 = new Point2d(sample.n);
				p3.scale(offset*sample.value);
				p3.add(sample.p);
				if(first) {
					turtle.jumpTo(px + p3.x, py + p3.y);
					first=false;
				} else {
					turtle.moveTo(px + p3.x, py + p3.y);
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
		TurtlePathWalker walker = new TurtlePathWalker(line);

		Point2d p = walker.walk(0);
		for(int i=0;i<numSamples;i++) {
			Point2d p2 = walker.walk(samplingRate);

			double value = 255.0 - (img.sample(p2.x, p2.y, 5.0));
			value = Math.max(0,Math.min(255,value));

			var n = new Vector2d(p2.y-p.y,-(p2.x-p.x));
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
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		random.setSeed(seed);
		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		turtle.getLayers().clear();

		if(fromEdge) {
			// get all the flow lines.
			List<Turtle> list = fromEdge();
			if (rightAngle) {
				rightAngle = false;
				list.addAll(fromEdge());
				rightAngle = true;
			}

			// make the line thicc.
			turtle = new Turtle();
			turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
			for (Turtle t : list) {
				convertLine(img, t);
			}
		} else {
			asGrid(image);
		}

		fireConversionFinished();
	}

	private List<Turtle> fromEdge() {
		List<Turtle> list = new ArrayList<>();
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xMin = rect.getMinX()+stepSize;
		double xMax = rect.getMaxX()-stepSize;
		double yMin = rect.getMinY()+stepSize;
		double yMax = rect.getMaxY()-stepSize;
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
		line.setStroke(turtle.getColor(),settings.getDouble(PlotterSettings.DIAMETER));
		line.jumpTo(x,y);
		// if the first step at this position would be outside the rectangle, reverse the direction.
		double v = noiseMaker.noise(line.getX() * scaleX + offsetX, line.getY() * scaleY + offsetY, 0);
		line.setAngle(v * 180 + (rightAngle?90:0));
		Vector2d nextStep = line.getHeading();
		nextStep.scale(samplingRate);
		nextStep.add(line.getPosition());
		continueLine(line, r,!r.contains(nextStep.x,nextStep.y));
		return line;
	}

	private void continueLine(Turtle line, Rectangle r, boolean reverse) {
		for(int i=0;i<200;++i) {
			double v = noiseMaker.noise(line.getX() * scaleX + offsetX, line.getY() * scaleY + offsetY, 0);
			line.setAngle(v * 180 + (rightAngle?90:0));
			Vector2d nextStep = line.getHeading();
			nextStep.scale(reverse?-samplingRate : samplingRate);
			nextStep.add(line.getPosition());
			// stop if we leave the rectangle
			if(!r.contains(nextStep.x,nextStep.y)) break;
			line.moveTo(nextStep.x,nextStep.y);
		}
	}

	private void asGrid(TransformedImage img) {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();
		Rectangle r = new Rectangle((int)xMin,(int)yMin,(int)(xMax-xMin),(int)(yMax-yMin));
		r.grow(1,1);
		for(double y = yMin; y<yMax; y+=stepSize) {
			for (double x = xMin; x < xMax; x += stepSize) {
				followLine(img,x,y,r);
			}
		}
	}

	private void followLine(TransformedImage img,double x,double y,Rectangle r) {
		double xx = x + stepVariation * (random.nextDouble() * 2.0 - 1.0);
		double yy = y + stepVariation * (random.nextDouble() * 2.0 - 1.0);

		turtle.jumpTo(xx, yy);
		followLine(img, r, 2);
		turtle.jumpTo(xx, yy);
		followLine(img, r, -2);
	}

	private void followLine(TransformedImage img,Rectangle r, int step) {
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();

		for(int i=0;i<stepLength/2;++i) {
			double value = 255.0 - (img.sample(turtle.getX()-px, turtle.getY()-py, 5.0));
			value /= 255.0;

			if(value + (random.nextDouble() - 0.5) > (cutoff/255.0)) turtle.penDown();
			else turtle.penUp();

			double v = noiseMaker.noise(turtle.getX() * scaleX + offsetX, turtle.getY() * scaleY + offsetY, 0);
			turtle.setAngle(v * 180);
			Vector2d nextStep = turtle.getHeading();
			nextStep.scale(step);
			nextStep.add(turtle.getPosition());
			if(!r.contains(nextStep.x,nextStep.y)) break;
			turtle.forward(step);
		}
	}
}
