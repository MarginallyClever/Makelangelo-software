package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;


/**
 * Horizontal lines.  The height of each line is determined by the average intensity of the pixels in that line.
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

	public Converter_IntensityToHeight() {
		super();

		SelectSlider selectSize = new SelectSlider("size",Translator.get("Converter_IntensityToHeight.spacing"), 20,1,getSpacing());
		SelectSlider selectSampleRate = new SelectSlider("sampleRate",Translator.get("Converter_IntensityToHeight.sampleRate"),20,1,getSampleRate());
		SelectSlider selectWaveIntensity = new SelectSlider("waveIntensity",Translator.get("Converter_IntensityToHeight.waveIntensity"),50,-50,getWaveIntensity());

		add(selectSize);
		add(selectSampleRate);
		add(selectWaveIntensity);

		selectSize.addPropertyChangeListener(evt->{
			setSpacing((int) evt.getNewValue());
			fireRestart();
		});
		selectSampleRate.addPropertyChangeListener(evt->{
			setSampleRate((int) evt.getNewValue());
			fireRestart();
		});
		selectWaveIntensity.addPropertyChangeListener(evt->{
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

	protected void convertLine(TransformedImage img, double sampleSpacing, double halfStep, Point2D a, Point2D b) {
		Point2D dir = new Point2D(b.x-a.x,b.y-a.y);
		double len = dir.length();
		dir.scale(1.0/len);

		boolean first=true;

		for (double p = 0; p <= len; p += sampleSpacing) {
			double x = a.x + dir.x * p; 
			double y = a.y + dir.y * p; 
			// read a block of the image and find the average intensity in this block
			double z = img.sample( x - sampleSpacing, y - halfStep, x + sampleSpacing, y + halfStep);
			// scale the intensity value
			double scale_z = 1 - z / 255.0f;
			//scale_z *= scale_z;  // quadratic curve
			double pulseSize = waveIntensity * scale_z;
			double py=y + pulseSize - waveIntensity/2.0f;
			if(first) {
				turtle.jumpTo(x, py);
				first = false;
			} else {
				turtle.moveTo(x, py);
			}
		}
	}

	/**
	 * Converts images into zigzags in paper space instead of image space
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();
		
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop();
		double xLeft   = myPaper.getMarginLeft();
		double xRight  = myPaper.getMarginRight();

		// from top to bottom of the image...
		int i=0;
		Point2D a = new Point2D();
		Point2D b = new Point2D();
		
		turtle = new Turtle();

		// horizontal
		for (double y = yBottom; y < yTop; y += spacing) {
			if ((++i % 2) == 0) {
				a.set(xLeft,y);
				b.set(xRight,y);
			} else {
				a.set(xRight,y);
				b.set(xLeft,y);
			}
			convertLine(img,sampleRate,sampleRate/2.0,a,b);
		}

		fireConversionFinished();
	}
}
