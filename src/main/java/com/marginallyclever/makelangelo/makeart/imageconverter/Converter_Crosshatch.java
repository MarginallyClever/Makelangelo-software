package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Crosshatch is a technique for creating a shading effect by drawing intersecting lines.
 * This converter applies multiple passes of crosshatch lines at different angles and intensities.
 * Adaptive intensities idea by Twitch user Harkish.
 */
public class Converter_Crosshatch extends ImageConverter {
	public static class CrosshatchPass {
		public double angle; // 0...360
		public double cutoff;  // 0...255
		public double intensity;  // 1...100

		public CrosshatchPass(double angle, double cutoff, double intensity) {
			this.angle = angle;
			this.cutoff = cutoff;
			this.intensity = intensity;
		}
	}
	private static final List<CrosshatchPass> passes = new ArrayList<CrosshatchPass>();

	public Converter_Crosshatch() {
		super();

		if(passes.isEmpty()) {
			passes.add(new CrosshatchPass(90,16, 16.0/10.0));
			passes.add(new CrosshatchPass(75,32, 32.0/10.0));
			passes.add(new CrosshatchPass(15,64, 64.0/10.0));
			passes.add(new CrosshatchPass(45,192, 192.0/10.0));
		}

		for(int i=0;i<passes.size();++i) {
			CrosshatchPass pass = passes.get(i);
			addPassUI(pass,i==0);
		}
	}

	private void addPassUI(CrosshatchPass pass,boolean isFirst) {
		if(!isFirst) {
			// add a separator between passes
		}

		SelectSlider selectIntesity = new SelectSlider("intensity", Translator.get("ConverterIntensity"), 25, 1, (int) (pass.intensity));
		selectIntesity.addSelectListener(evt->{
			pass.intensity = (int)evt.getNewValue();
			fireRestart();
		});
		add(selectIntesity);

		SelectSlider selectCutoff = new SelectSlider("cutoff", Translator.get("Converter_VoronoiStippling.Cutoff"), 255, 0, (int) pass.cutoff);
		selectCutoff.addSelectListener(evt->{
			pass.cutoff = (int)evt.getNewValue();
			fireRestart();
		});
		add(selectCutoff);
	}

	@Override
	public String getName() {
		return Translator.get("Crosshatch");
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();
		
		turtle = new Turtle();
        for (CrosshatchPass pass : passes) {
            finishPass(new int[]{(int) pass.cutoff}, pass, img);
        }

		fireConversionFinished();
	}
	
	private void finishPass(int [] passes,CrosshatchPass pass,TransformedImage img) {
		double dx = Math.cos(Math.toRadians(pass.angle));
		double dy = Math.sin(Math.toRadians(pass.angle));

		// figure out how many lines we're going to have on this image.
		double stepSize = pass.intensity/2.0;
		if (stepSize < 1) stepSize = 1;

		int stepSize2 = Math.max(1,(int)(stepSize/2));

		// Color values are from 0...255 inclusive.  255 is white, 0 is black.
		// Lift the pen any time the color value is > level (128 or more).

		// from top to bottom of the margin area...
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xLeft   = rect.getMinX();
		double yBottom = rect.getMinY();
		double xRight  = rect.getMaxX();
		double yTop    = rect.getMaxY();
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
		
			double cutoff = passes[(i % passes.length)];
			if ((i % 2) == 0) {
				if(!useError) convertAlongLine(x0, y0, x1, y1, stepSize2, cutoff, img);
				else convertAlongLineErrorTerms(x0,y0,x1,y1,stepSize2,cutoff,error0,error1,img);
			} else {
				if(!useError) convertAlongLine(x1, y1, x0, y0, stepSize2, cutoff, img);
				else convertAlongLineErrorTerms(x1,y1,x0,y0,stepSize2,cutoff,error0,error1,img);
			}
			for(int j=0;j<error0.length;++j) {
				error0[j]=error1[error0.length-1-j];
				error1[error0.length-1-j]=0;
			}
			++i;
		}
	}
}
