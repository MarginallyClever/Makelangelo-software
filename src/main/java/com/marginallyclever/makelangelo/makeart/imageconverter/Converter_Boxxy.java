package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.turtletool.InfillTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A grid of boxes across the paper, and make the boxes bigger if the image is darker in that area.
 * @author Dan Royer
 *
 */
public class Converter_Boxxy extends ImageConverter {
	protected static int boxMaxSize=4; // 0.8*5
	protected static int cutoff=127;
	protected static boolean fillShapes = false;

	public Converter_Boxxy() {
		super();

		SelectSlider size = new SelectSlider("size",Translator.get("Converter_Boxxy.MaxSize"),40,2,getBoxMasSize());
		size.addSelectListener((evt)->{
			setBoxMaxSize((int)evt.getNewValue());
			fireRestart();
		});
		add(size);

		SelectSlider cutoff = new SelectSlider("cutoff",Translator.get("Converter_Boxxy.Cutoff"),255,0,getCutoff());
		cutoff.addSelectListener((evt)->{
			setCutoff((int)evt.getNewValue());
			fireRestart();
		});
		add(cutoff);

		SelectBoolean fillShape = new SelectBoolean("fillShape",Translator.get("Converter_Boxxy.fill"),Converter_Boxxy.fillShapes);
		fillShape.addSelectListener((evt)->{
			Converter_Boxxy.fillShapes = (boolean)evt.getNewValue();
			fireRestart();
		});
		add(fillShape);
	}

	@Override
	public String getName() {
		return Translator.get("Converter_Boxxy.Name");
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

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();
		double cx = paper.getCenterX();
		double cy = paper.getCenterY();
		double pw = xRight - xLeft;
		
		// figure out how many lines we're going to have on this image.
		double fullStep = boxMaxSize;
		double halfStep = fullStep / 2.0f;
		
		double steps = pw / fullStep;
		if (steps < 1) steps = 1;

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));


		double lowpass = cutoff/255.0;
		
		// from top to bottom of the image...
		double x, y, z;
		int i = 0;
		for (y = yBottom + halfStep; y < yTop - halfStep; y += fullStep) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				for (x = xLeft + halfStep; x < xRight; x += fullStep) {
					addBox(img,x,y,halfStep,lowpass,cx,cy);
				}
			} else {
				// every odd line move right to left
				for (x = xRight + halfStep; x > xLeft; x -= fullStep) {
					addBox(img,x,y,halfStep,lowpass,cx,cy);
				}
			}
		}

		fireConversionFinished();
	}

	private void addBox(TransformedImage img, double x, double y, double halfStep, double lowpass,double cx,double cy) {
		// read a block of the image and find the average intensity in this block
		var z = img.sample( x, y, halfStep);

		// scale the intensity value
		double scaleZ = (255.0f - z) / 255.0f;
		if (scaleZ > lowpass) {
			double ratio = (scaleZ-lowpass)/(1.0-lowpass);

			Turtle t2 = new Turtle();
			t2.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

			drawBox(t2, cx+x,cy+y,ratio,halfStep);

			if(fillShapes) {
				InfillTurtle filler = new InfillTurtle();
				filler.setPenDiameter(turtle.getDiameter());
				try {
					var t3 = filler.run(t2);
					turtle.add(t3);
				} catch(Exception ignore) {}
			}
			turtle.add(t2);
		}
	}

	private void drawBox(Turtle t2,double x,double y,double ratio,double halfStep) {
		double pulseSize = (halfStep - 0.5f) * ratio;
		double xmin = x - halfStep - pulseSize;
		double xmax = x - halfStep + pulseSize;
		double ymin = y + halfStep - pulseSize;
		double ymax = y + halfStep + pulseSize;
		// draw a square.  the diameter is relative to the intensity.
		t2.jumpTo(xmin, ymin);
		t2.moveTo(xmax, ymin);
		t2.moveTo(xmax, ymax);
		t2.moveTo(xmin, ymax);
		t2.moveTo(xmin, ymin);
	}
}
