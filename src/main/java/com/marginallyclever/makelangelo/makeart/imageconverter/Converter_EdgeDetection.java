package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_Greyscale;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Uses <a href="http://en.wikipedia.org/wiki/Marching_squares">marching squares</a> to detect edges.
 * @author Dan Royer
 * @since 2022-03-31
 */
public class Converter_EdgeDetection extends ImageConverter {
	private static int passes=5;
	private static int stepSize=10;
	private static int sampleSize=2;

	private int edge;
	private TransformedImage img;

	public Converter_EdgeDetection() {
		super();
		SelectSlider selectPasses     = new SelectSlider("passes", Translator.get("Converter_EdgeDetection.passes"), 20, 1, (int) (getPasses()));
		SelectSlider selectStepSize   = new SelectSlider("stepSize", Translator.get("Converter_EdgeDetection.stepSize"), 25, 2, (int) getStepSize());
		SelectSlider selectSampleSize = new SelectSlider("sampleSize", Translator.get("Converter_EdgeDetection.sampleSize"), 5, 1, (int) getSampleSize());

		add(selectPasses);
		add(selectStepSize);
		add(selectSampleSize);

		selectPasses.addPropertyChangeListener(evt->{
			setPasses((int)evt.getNewValue());
			fireRestart();
		});
		selectStepSize.addPropertyChangeListener(evt->{
			setStepSize((int)evt.getNewValue());
			fireRestart();
		});
		selectSampleSize.addPropertyChangeListener(evt->{
			setSampleSize((int)evt.getNewValue());
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
	
	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		Filter_Greyscale bw = new Filter_Greyscale(255);
		img = bw.filter(myImage);
		turtle = new Turtle();

		int edgeStep = 255/(passes+1);
		int d = 40;
		edge = 255-edgeStep;

		for(int i=0;i<passes;++i) {
			marchingSquares();
			edge -= edgeStep;
		}
/*
		// add border
		turtle.jumpTo(myPaper.getMarginLeft(),myPaper.getMarginBottom());
		turtle.moveTo(myPaper.getMarginRight(),myPaper.getMarginBottom());
		turtle.moveTo(myPaper.getMarginRight(),myPaper.getMarginTop());
		turtle.moveTo(myPaper.getMarginLeft(),myPaper.getMarginTop());
		turtle.moveTo(myPaper.getMarginLeft(),myPaper.getMarginBottom());*/

		fireConversionFinished();
	}

	void marchingSquares() {
		double height  = myPaper.getMarginTop() - myPaper.getMarginBottom();
		double width   = myPaper.getMarginRight() - myPaper.getMarginLeft();

		int stepsOnY = (int)Math.floor(height / stepSize);
		int stepsOnX = (int)Math.floor(width / stepSize);

		for(int y=0;y<stepsOnY;++y) {
			for(int x=0;x<stepsOnX;++x) {
				marchSquare((int)myPaper.getMarginLeft() + x*stepSize,
						(int)myPaper.getMarginBottom() + y*stepSize);
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
		if(code>7) code = 15-code;

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
	
	Point2D lerpEdge(int x0, int y0, int x1, int y1) {
		float in0 = brightness(img.sample(x0,y0,sampleSize));
		float in1 = brightness(img.sample(x1,y1,sampleSize));

		float v = (edge-in0) / (in1-in0);
		v=Math.max(0,Math.min(1,v));
		float x3 = lerp((float)x0,(float)x1,v);
		float y3 = lerp((float)y0,(float)y1,v);
		return new Point2D(x3,y3);
	}

	void line(Point2D a,Point2D b) {
		turtle.jumpTo(a.x,a.y);
		turtle.moveTo(b.x,b.y);
	}

	void case1(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2D a = lerpEdge(x0,y0,x0,y1);
		Point2D b = lerpEdge(x0,y0,x1,y0);
		line(a,b);
	}

	void case2(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2D a = lerpEdge(x1,y0,x0,y0);
		Point2D b = lerpEdge(x1,y0,x1,y1);
		line(a,b);
	}

	// 1 + 2
	void case3(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2D a = lerpEdge(x0,y0,x0,y1);
		Point2D b = lerpEdge(x1,y0,x1,y1);
		line(a,b);
	}

	void case4(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2D a = lerpEdge(x1,y1,x0,y1);
		Point2D b = lerpEdge(x1,y1,x1,y0);
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
		Point2D a = lerpEdge(x0,y0,x1,y0);
		Point2D b = lerpEdge(x0,y1,x1,y1);
		line(a,b);
	}

	// 1+2+4
	void case7(int x0,int y0) {
		int x1 = x0+stepSize;
		int y1 = y0+stepSize;
		Point2D a = lerpEdge(x0,y1,x0,y0);
		Point2D b = lerpEdge(x0,y1,x1,y1);
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
