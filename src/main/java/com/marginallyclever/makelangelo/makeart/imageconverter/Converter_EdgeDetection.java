package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterExtendedDifferenceOfGaussians;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterGaussianBlur;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Uses <a href="http://en.wikipedia.org/wiki/Marching_squares">marching squares</a> to detect edges.
 * @author Dan Royer
 * @since 2022-03-31
 */
public class Converter_EdgeDetection extends ImageConverter {
	private static int passes=5;
	private static int stepSize=10;
	private static int sampleSize=2;
	private static boolean border=false;

	private int edge;
	private TransformedImage img;
	private double px,py;

	public Converter_EdgeDetection() {
		super();
		SelectSlider selectPasses     = new SelectSlider("passes", Translator.get("Converter_EdgeDetection.passes"), 20, 1, (int) (getPasses()));
		SelectSlider selectStepSize   = new SelectSlider("stepSize", Translator.get("Converter_EdgeDetection.stepSize"), 25, 2, (int) getStepSize());
		SelectSlider selectSampleSize = new SelectSlider("sampleSize", Translator.get("Converter_EdgeDetection.sampleSize"), 5, 1, (int) getSampleSize());
		SelectBoolean selectBorder    = new SelectBoolean("border", Translator.get("Converter_EdgeDetection.border"), border);

		add(selectPasses);
		add(selectStepSize);
		add(selectSampleSize);

		selectPasses.addSelectListener(evt->{
			setPasses((int)evt.getNewValue());
			fireRestart();
		});
		selectStepSize.addSelectListener(evt->{
			setStepSize((int)evt.getNewValue());
			fireRestart();
		});
		selectSampleSize.addSelectListener(evt->{
			setSampleSize((int)evt.getNewValue());
			fireRestart();
		});
		selectBorder.addSelectListener(evt->{
			setBorder((boolean)evt.getNewValue());
			fireRestart();
		});
	}

	@Override
	public String getName() {
		return Translator.get("Converter_EdgeDetection.name");
	}

	public int getPasses() {
		return passes;
	}

	public int getStepSize() {
		return stepSize;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public void setBorder(boolean newValue) {
		border=newValue;
	}
	
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate desaturates = new FilterDesaturate(myImage);
		img = desaturates.filter();

		FilterGaussianBlur blur1 = new FilterGaussianBlur(img, 1);
		FilterGaussianBlur blur2 = new FilterGaussianBlur(img, 4);
		TransformedImage img1 = blur1.filter();
		TransformedImage img2 = blur2.filter();
		FilterExtendedDifferenceOfGaussians dog = new FilterExtendedDifferenceOfGaussians(img1,img2,20);
		img = dog.filter();

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
		px = myPaper.getCenterX();
		py = myPaper.getCenterY();

		int edgeStep = 255/(passes+1);
		edge = 255-edgeStep;

		for(int i=0;i<passes;++i) {
			marchingSquares();
			edge -= edgeStep;
		}

		if(border) {
			// add border
			Rectangle2D.Double rect = myPaper.getMarginRectangle();
			double xLeft   = rect.getMinX();
			double yBottom = rect.getMinY();
			double xRight  = rect.getMaxX();
			double yTop    = rect.getMaxY();

			turtle.jumpTo(px+xLeft, py+yBottom);
			turtle.moveTo(px+xRight, py+yBottom);
			turtle.moveTo(px+xRight, py+yTop);
			turtle.moveTo(px+xLeft, py+yTop);
			turtle.moveTo(px+xLeft, py+yBottom);
		}

		fireConversionFinished();
	}

	void marchingSquares() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double height  = rect.getHeight();
		double width   = rect.getWidth();

		int stepsOnY = (int)Math.floor(height / stepSize);
		int stepsOnX = (int)Math.floor(width / stepSize);

		for(int y=0;y<stepsOnY;++y) {
			for(int x=0;x<stepsOnX;++x) {
				marchSquare((int)rect.getMinX() + x*stepSize,
						(int)rect.getMinY() + y*stepSize);
			}
		}
	}

	private int brightness(int color) {
		return color & 0xFF;
	}

	void marchSquare(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		int in0 = brightness(img.sample(x0,y0,sampleSize)) >= edge ? 1:0;
		int in1 = brightness(img.sample(x1,y0,sampleSize)) >= edge ? 2:0;
		int in2 = brightness(img.sample(x1,y1,sampleSize)) >= edge ? 4:0;
		int in3 = brightness(img.sample(x0,y1,sampleSize)) >= edge ? 8:0;
		int code = in0 | in1 | in2 | in3;

		// 15 is a mirror of 1 and so on.
		if(code > 7) code = 15 - code;

		switch(code) {
			case 0:  break;
			case 1:  case1(x0,y0);  break;
			case 2:  case2(x0,y0);  break;
			case 3:  case3(x0,y0);  break;
			case 4:  case4(x0,y0);  break;
			case 5:  case5(x0,y0);  break;
			case 6:  case6(x0,y0);  break;
			case 7:  case7(x0,y0);  break;
		}
	}

	float lerp(float a,float b,float v) {
		return a + (b - a) * v;
	}
	
	Point2d lerpEdge(int x0, int y0, int x1, int y1) {
		float in0 = brightness(img.sample(x0,y0,sampleSize));
		float in1 = brightness(img.sample(x1,y1,sampleSize));

		float v = (edge-in0) / (in1-in0);
		v=Math.max(0,Math.min(1,v));
		float x3 = lerp((float)x0,(float)x1,v);
		float y3 = lerp((float)y0,(float)y1,v);
		return new Point2d(x3,y3);
	}

	void line(Point2d a,Point2d b) {
		turtle.jumpTo(px+a.x,py+a.y);
		turtle.moveTo(px+b.x,py+b.y);
	}

	void case1(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2d a = lerpEdge(x0,y0,x0,y1);
		Point2d b = lerpEdge(x0,y0,x1,y0);
		line(a,b);
	}

	void case2(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2d a = lerpEdge(x1,y0,x0,y0);
		Point2d b = lerpEdge(x1,y0,x1,y1);
		line(a,b);
	}

	// 1 + 2
	void case3(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2d a = lerpEdge(x0,y0,x0,y1);
		Point2d b = lerpEdge(x1,y0,x1,y1);
		line(a,b);
	}

	void case4(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2d a = lerpEdge(x1,y1,x0,y1);
		Point2d b = lerpEdge(x1,y1,x1,y0);
		line(a,b);
	}

	// 1 + 4
	void case5(int x0,int y0) {
		case1(x0,y0);
		case4(x0,y0);
	}

	// 2 + 4
	void case6(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2d a = lerpEdge(x0,y0,x1,y0);
		Point2d b = lerpEdge(x0,y1,x1,y1);
		line(a,b);
	}

	// 1+2+4
	void case7(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2d a = lerpEdge(x0,y1,x0,y0);
		Point2d b = lerpEdge(x0,y1,x1,y1);
		line(a,b);
	}

	public static void setPasses(int newValue) {
		Converter_EdgeDetection.passes=newValue;
	}

	public static void setSampleSize(int sampleSize) {
		Converter_EdgeDetection.sampleSize = sampleSize;
	}

	public static void setStepSize(int stepSize) {
		Converter_EdgeDetection.stepSize = stepSize;
	}
}
