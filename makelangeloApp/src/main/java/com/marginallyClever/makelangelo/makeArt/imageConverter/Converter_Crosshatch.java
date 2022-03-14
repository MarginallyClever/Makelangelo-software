package com.marginallyClever.makelangelo.makeArt.imageConverter;

import java.beans.PropertyChangeEvent;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.makeArt.TransformedImage;
import com.marginallyClever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyClever.makelangelo.turtle.Turtle;

public class Converter_Crosshatch extends ImageConverter {
	private static double intensity=2.0f;
	private static double pass90=8.0f;
	private static double pass75=16.0f;
	private static double pass15=64.0f;
	private static double pass45=128.0f;
	
	@Override
	public String getName() {
		return Translator.get("Crosshatch");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("intensity")) {
			setIntensity((float)((int)evt.getNewValue())/10.0f);
		}
		if(evt.getPropertyName().equals("pass90")) pass90=(int)evt.getNewValue();
		if(evt.getPropertyName().equals("pass75")) pass75=(int)evt.getNewValue();
		if(evt.getPropertyName().equals("pass15")) pass15=(int)evt.getNewValue();
		if(evt.getPropertyName().equals("pass45")) pass45=(int)evt.getNewValue();
		
	}

	public void setIntensity(double arg0) {
		intensity=arg0;
	}
	
	public double getIntensity() {
		return intensity;
	}
	
	public double getPass90() {
		return pass90;
	}

	public double getPass75() {
		return pass75;
	}

	public double getPass15() {
		return pass15;
	}

	public double getPass45() {
		return pass45;
	} 
	
	@Override
	public void finish() {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(myImage);
		
		turtle = new Turtle();
		finishPass(new int[]{(int)pass90},90,img);
		finishPass(new int[]{(int)pass75},15,img);
		finishPass(new int[]{(int)pass15},75,img);
		finishPass(new int[]{(int)pass45},45,img);
	}
	
	private void finishPass(int [] passes,double angleDeg,TransformedImage img) {
		double dx = Math.cos(Math.toRadians(angleDeg));
		double dy = Math.sin(Math.toRadians(angleDeg));

		// figure out how many lines we're going to have on this image.
		double stepSize = intensity/2.0;
		if (stepSize < 1) stepSize = 1;

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).

		// from top to bottom of the margin area...
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop();
		double xLeft   = myPaper.getMarginLeft();
		double xRight  = myPaper.getMarginRight();
		double height = yTop - yBottom;
		double width = xRight - xLeft;
		double maxLen = Math.sqrt(width*width+height*height);
		double [] error0 = new double[(int)Math.ceil(maxLen)];
		double [] error1 = new double[(int)Math.ceil(maxLen)];
		boolean useError=false;
		
		int i=0;
		for(double a = -maxLen;a<maxLen;a+=stepSize) {
			double px = dx * a;
			double py = dy * a;
			double x0 = px - dy * maxLen;
			double y0 = py + dx * maxLen;
			double x1 = px + dy * maxLen;
			double y1 = py - dx * maxLen;
		
			double l2 = passes[(i % passes.length)];
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
	}
}
