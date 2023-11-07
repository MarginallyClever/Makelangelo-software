package com.marginallyclever.makelangelo.makeart.turtlegenerator.grid;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Draws a set of squares that fit inside an even margin.
 * @author Dan Royer
 * @since 2022-04-21
 */
public class Generator_GridFit extends TurtleGenerator {
	private final SelectInteger cellsWide;
	private final SelectInteger cellsHigh;
	private final SelectDouble margin;

	public Generator_GridFit() {
		add(margin = new SelectDouble("margin",Translator.get("Generator_GridFit.margin"),50));
		add(cellsWide = new SelectInteger("cellsWide",Translator.get("Generator_GridFit.cellsWide"),1));
		add(cellsHigh = new SelectInteger("cellsHigh",Translator.get("Generator_GridFit.cellsHigh"),1));

		margin.addSelectListener(evt->{
			if(!checkMargin()) return;
			generate();
		});

		cellsWide.addSelectListener(evt->{
			changeWide();
			generate();
		});

		cellsHigh.addSelectListener(evt->{
			changeHigh();
			generate();
		});
	}

	/**
	 *
	 * @return true if the margin is valid.
	 */
	private boolean checkMargin() {
		double width = myPaper.getPaperWidth();
		double v = margin.getValue();
		if(v > width/2) {
			margin.setValue(width/2);
			return false;
		} else if(v<0) {
			margin.setValue(0);
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return Translator.get("Generator_GridFit.Name");
	}

	private void changeWide() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		int w = cellsWide.getValue();
		int h = (int)Math.floor(w*rect.getHeight()/rect.getWidth());
		cellsHigh.setValue(h);
	}

	private void changeHigh() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		int h = cellsHigh.getValue();
		int w = (int)Math.ceil(h*rect.getWidth()/rect.getHeight());
		cellsWide.setValue(w);
	}

	@Override
	public void generate() {
		double myMargin = margin.getValue();
		double xMin = myPaper.getPaperLeft()+myMargin;
		double xMax = myPaper.getPaperRight()-myMargin;

		Turtle turtle = new Turtle();
		turtle.penUp();

		int h = cellsHigh.getValue();
		int w = cellsWide.getValue();
		double length = (xMax-xMin) / w;

		double halfWidth = (w * length)/2;
		double halfHeight = (h * length)/2;

		for(int y=0;y<=h;++y) {
			double y2 = -halfHeight + y * length;
			turtle.jumpTo(-halfWidth,y2);
			turtle.moveTo( halfWidth,y2);
		}

		for(int x=0;x<=w;++x) {
			double x2 = -halfWidth+x*length;
			turtle.jumpTo(x2,-halfHeight);
			turtle.moveTo(x2, halfHeight);
		}

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
	}
}
