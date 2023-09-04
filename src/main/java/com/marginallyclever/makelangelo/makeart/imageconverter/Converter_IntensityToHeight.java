package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_Greyscale;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;


/**
 * Horizontal lines.  The height of each line is determined by the average intensity of the pixels in that line.
 * @author Dan Royer
 * @since 7.40.3
 */
public class Converter_IntensityToHeight extends ImageConverter {
	private static int spacing = 2;
	private static int maxHeight = 10;
	private static int sampleRate = 5;

	public Converter_IntensityToHeight() {
		super();

		SelectSlider selectSize = new SelectSlider("size",Translator.get("Converter_IntensityToHeight.spacing"), 20,1,getSpacing());
		SelectSlider selectMaxHeight = new SelectSlider("maxHeight",Translator.get("Converter_IntensityToHeight.maxHeight"),20,1,getMaxHeight());
		SelectSlider selectSampleRate = new SelectSlider("sampleRate",Translator.get("Converter_IntensityToHeight.sampleRate"),20,1,getSampleRate());

		add(selectSize);
		add(selectMaxHeight);
		add(selectSampleRate);

		selectSize.addPropertyChangeListener(evt->{
			setSpacing((int) evt.getNewValue());
			fireRestart();
		});
		selectMaxHeight.addPropertyChangeListener(evt->{
			setMaxHeight((int) evt.getNewValue());
			fireRestart();
		});
		selectSampleRate.addPropertyChangeListener(evt->{
			setSampleRate((int) evt.getNewValue());
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

	public int getMaxHeight() {
		return maxHeight;
	}
	public void setMaxHeight(int value) {
		maxHeight = Math.max(1,value);
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
			double scale_z = z / 255.0f;
			scale_z = (scale_z-0.5)*2.0;
			//scale_z *= scale_z;  // quadratic curve
			double pulseSize = halfStep * scale_z;
			double py=y + pulseSize;
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

		Filter_Greyscale bw = new Filter_Greyscale(255);
		TransformedImage img = bw.filter(myImage);
		
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
			convertLine(img,sampleRate,maxHeight,a,b);
		}

		fireConversionFinished();
	}
}
