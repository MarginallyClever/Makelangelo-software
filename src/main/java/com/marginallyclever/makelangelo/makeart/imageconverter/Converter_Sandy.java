package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectOneOfMany;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * based on polagraph style by Sandy Noble.
 * @author Dan Royer
 */
public class Converter_Sandy extends ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(Converter_Sandy.class);
	private static int blockScale=150;
	private static int direction=0;
	private final String [] directionChoices = new String[] {
		Translator.get("topRight"),
		Translator.get("topLeft"),
		Translator.get("bottomLeft"),
		Translator.get("bottomRight"),
		Translator.get("center")
	};

	public Converter_Sandy() {
		super();
		SelectSlider selectRings = new SelectSlider("rings",Translator.get("SandyNoble.rings"),300,10,getScale());
		add(selectRings);
		selectRings.addSelectListener(evt->{
			setScale((int)evt.getNewValue());
			fireRestart();
		});
		SelectOneOfMany selectDirection = new SelectOneOfMany("direction",Translator.get("SandyNoble.center"),getDirections(),getDirectionIndex());
		add(selectDirection);
		selectDirection.addSelectListener(evt->{
			setDirection((int)evt.getNewValue());
			fireRestart();
		});
	}
	
	@Override
	public String getName() {
		return Translator.get("SandyNoble.title");
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		double yBottom = myPaper.getPaperBottom();
		double yTop    = myPaper.getPaperTop();
		double xLeft   = myPaper.getPaperLeft();
		double xRight  = myPaper.getPaperRight();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double pLeft   = rect.getMinX() +1.0;
		double pBottom = rect.getMinY() +1.0;
		double pRight  = rect.getMaxX() -1.0;
		double pTop    = rect.getMaxY() -1.0;

		double cx,cy;
		double last_x=0,last_y=0;

		boolean wasDrawing=false;

		switch (direction) {
			case 0 -> {
				cx = xRight;
				cy = yTop;
				last_x = pRight;
				last_y = pTop;
			}
			case 1 -> {
				cx = xLeft;
				cy = yTop;
				last_x = pLeft;
				last_y = pTop;
			}
			case 2 -> {
				cx = xLeft;
				cy = yBottom;
				last_x = pLeft;
				last_y = pBottom;
			}
			case 3 -> {
				cx = xRight;
				cy = yBottom;
				last_x = pRight;
				last_y = pBottom;
			}
			default -> {
				cx = 0;
				cy = 0;
				last_x = 0;
				last_y = 0;
			}
		}

		double x, y, z, scaleZ;

		double dx = xRight - xLeft; 
		double dy = yTop - yBottom;
		double rMax = Math.sqrt(dx*dx+dy*dy);
		double rMin = 0;

		double rStep = (rMax-rMin)/(double)blockScale;
		double r;
		double t_dir=1;
		double pulseFlip=1;
		double t,t_step;
		double flipSum;
		double pulseSize = rStep*0.5 - 0.5;//r_step * 0.6 * scale_z;
		double sampleSize = pulseSize/2.0;

		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
		logger.debug("Sandy started.");
		//Thread.dumpStack();
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();

		turtle.lock();
		try {
			// make concentric circles that get bigger and bigger.
			for(r=rMin;r<rMax;r+=rStep) {
				// go around in a circle
				t=0;
				t_step = 1.0/r;
				flipSum=0;
				// go around the circle
				for(t=0;t<Math.PI*2;t+=t_step) {
					dx = Math.cos(t_dir *t);
					dy = Math.sin(t_dir *t);
					x = cx + dx * r;
					y = cy + dy * r;
					if(!rect.contains(x,y)) {
						if(wasDrawing) {
							turtle.jumpTo(px+last_x,py+last_y);
							wasDrawing=false;
						}
						continue;
					}
	
					last_x=x;
					last_y=y;
					// read a block of the image and find the average intensity in this block
					z = img.sample( x,y, sampleSize );
					// scale the intensity value
					if(z<0) z=0;
					if(z>255) z=255;
					scaleZ = (255.0 -  z) / 255.0;
	
					if(!wasDrawing) {
						turtle.jumpTo(px+last_x,py+last_y);
						wasDrawing=true;
					}
	
					turtle.moveTo(	px + x + dx * pulseSize*pulseFlip,
									py + y + dy * pulseSize*pulseFlip);
					
					flipSum+=scaleZ;
					if(flipSum >= 1) {
						flipSum-=1;
						pulseFlip = -pulseFlip;
						turtle.moveTo(	px + x + dx * pulseSize*pulseFlip,
										py + y + dy * pulseSize*pulseFlip);
					}
				}
				t_dir=-t_dir;
			}
		} catch(Exception e) {
			logger.error("Sandy failed", e);
		} finally {
			turtle.unlock();
			logger.debug("Sandy finished.");
		}

		fireConversionFinished();
	}

	public int getScale() {
		return blockScale;
	}
	public void setScale(int value) {
		if(value<1) value=1;
		blockScale=value;
	}
	public String [] getDirections() {
		return directionChoices;
	}
	public int getDirectionIndex() {
		return direction;
	}
	public void setDirection(int value) {
		if(value<0) value=0;
		if(value>=directionChoices.length) value = directionChoices.length-1;
		direction = value;
	}
}
