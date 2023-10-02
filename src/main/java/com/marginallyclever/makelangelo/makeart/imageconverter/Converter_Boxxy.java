package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * A grid of boxes across the paper, and make the boxes bigger if the image is darker in that area.
 * @author Dan Royer
 *
 */
public class Converter_Boxxy extends ImageConverter {
	public static int boxMaxSize=4; // 0.8*5
	public static int cutoff=127;

	public Converter_Boxxy() {
		super();

		SelectSlider size = new SelectSlider("size",Translator.get("BoxGeneratorMaxSize"),40,2,getBoxMasSize());
		size.addPropertyChangeListener((evt)->{
			setBoxMaxSize((int)evt.getNewValue());
			fireRestart();
		});
		add(size);

		SelectSlider cutoff = new SelectSlider("cutoff",Translator.get("BoxGeneratorCutoff"),255,0,getCutoff());
		cutoff.addPropertyChangeListener((evt)->{
			setCutoff((int)evt.getNewValue());
			fireRestart();
		});
		add(cutoff);
	}

	@Override
	public String getName() {
		return Translator.get("BoxGeneratorName");
	}

	public void setBoxMaxSize(int arg0) {
		boxMaxSize=arg0;
	}
	
	public int getBoxMasSize() {
		return boxMaxSize;
	}
	
	public void setCutoff(int arg0) {
		cutoff = arg0; 
	}
	public int getCutoff() {
		return cutoff;
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop();
		double xLeft   = myPaper.getMarginLeft();
		double xRight  = myPaper.getMarginRight();
		double pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		double fullStep = boxMaxSize;
		double halfStep = fullStep / 2.0f;
		
		double steps = pw / fullStep;
		if (steps < 1) steps = 1;

		turtle = new Turtle();

		double lowpass = cutoff/255.0;
		
		// from top to bottom of the image...
		double x, y, z;
		int i = 0;
		for (y = yBottom + halfStep; y < yTop - halfStep; y += fullStep) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				for (x = xLeft; x < xRight; x += fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x, y - halfStep, x + fullStep, y + halfStep );
					// scale the intensity value
					double scaleZ =  (255.0f - z) / 255.0;
					if (scaleZ > lowpass) {
						double ratio = (scaleZ-lowpass)/(1.0-lowpass);
						drawBox(x,y,ratio,halfStep);
					}
				}
			} else {
				// every odd line move right to left
				for (x = xRight; x > xLeft; x -= fullStep) {
					// read a block of the image and find the average intensity in this block
					z = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);
					// scale the intensity value
					double scaleZ = (255.0f - z) / 255.0f;
					if (scaleZ > lowpass) {
						double ratio = (scaleZ-lowpass)/(1.0-lowpass);
						drawBox(x,y,ratio,halfStep);
					}
				}
			}
		}

		fireConversionFinished();
	}

	private void drawBox(double x,double y,double ratio,double halfStep) {
		double pulseSize = (halfStep - 0.5f) * ratio;
		double xmin = x - halfStep - pulseSize;
		double xmax = x - halfStep + pulseSize;
		double ymin = y + halfStep - pulseSize;
		double ymax = y + halfStep + pulseSize;
		// draw a square.  the diameter is relative to the intensity.
		turtle.jumpTo(xmin, ymin);
		turtle.moveTo(xmax, ymin);
		turtle.moveTo(xmax, ymax);
		turtle.moveTo(xmin, ymax);
		turtle.moveTo(xmin, ymin);
		// fill in the square
		boolean flip = false;
		for(double yy=ymin;yy<ymax;yy+=boxMaxSize) {
			turtle.moveTo(flip?xmin:xmax,yy);
			flip = !flip;
		}
	}
}
