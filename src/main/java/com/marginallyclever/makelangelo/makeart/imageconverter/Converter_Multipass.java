package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectInteger;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * 
 * @author Dan Royer
 */
public class Converter_Multipass extends ImageConverter {
	static private double angle=0;
	static private int passes=4;

	public Converter_Multipass() {
		super();

		SelectDouble  selectAngle = new SelectDouble("angle",Translator.get("ConverterMultipassAngle"),getAngle());
		SelectInteger selectLevel = new SelectInteger("level",Translator.get("ConverterMultipassLevels"),getPasses());

		add(selectAngle);
		add(selectLevel);

		selectAngle.addSelectListener(evt->{
			setAngle((double)evt.getNewValue());
			fireRestart();
		});

		selectLevel.addSelectListener(evt->{
			setPasses((int)evt.getNewValue());
			fireRestart();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("ConverterMultipassName");
	}

	public double getAngle() {
		return angle;
	}
	
	public void setAngle(double value) {
		angle = value;
	}
	
	public int getPasses() {
		return passes;
	}
	
	public void setPasses(int value) {
		if(passes<1) passes=1;
		passes=value;
	}
	
	/**
	 * create parallel lines across the image.  Raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		// The picture might be in color.  Smash it to 255 shades of grey.
		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();
		
		double dx = Math.cos(Math.toRadians(angle));
		double dy = Math.sin(Math.toRadians(angle));

		double stepSize = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).
		double level = 255.0 / (double)(passes+1);

		// from top to bottom of the margin area...
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yBottom = rect.getMinY();
		double yTop    = rect.getMaxY();
		double xLeft   = rect.getMinX();
		double xRight  = rect.getMaxX();
		double height = yTop - yBottom;
		double width = xRight - xLeft;
		double maxLen = Math.sqrt(width*width+height*height);
		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
		
		boolean useError=false;
		
		int i=0;
		for(double a = -maxLen;a<maxLen;a+=stepSize) {
			double px = dx * a;
			double py = dy * a;
			double x0 = px - dy * maxLen;
			double y0 = py + dx * maxLen;
			double x1 = px + dy * maxLen;
			double y1 = py - dx * maxLen;
		
			double l2 = level * (1 + (i % passes));
			if ((i % 2) == 0) {
				if(!useError) convertAlongLine(x0, y0, x1, y1, stepSize, l2, img);
				else convertAlongLineErrorTerms(x0,y0,x1,y1,stepSize,l2,error0,error1,img);
			} else {
				if(!useError) convertAlongLine(x1, y1, x0, y0, stepSize, l2, img);
				else convertAlongLineErrorTerms(x1,y1,x0,y0,stepSize,l2,error0,error1,img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			++i;
		}

		fireConversionFinished();
	}
}
