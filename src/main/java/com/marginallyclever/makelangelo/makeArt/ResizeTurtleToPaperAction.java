package com.marginallyclever.makelangelo.makeArt;

import java.awt.geom.Rectangle2D;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Scale the input {@link Turtle} to fit the margins of the {@link Paper} provided.
 * @author Dan Royer
 */
public class ResizeTurtleToPaperAction extends TurtleModifierAction {
	private static final long serialVersionUID = -2481567507951197219L;
	private Paper myPaper;
	private boolean fillPage;
	
	public ResizeTurtleToPaperAction(Paper paper,boolean fillPage,String name) {
		super(name);
		this.fillPage = fillPage;
		this.myPaper = paper;
	}
	
	@Override
	public Turtle run(Turtle turtle) {
		Rectangle2D.Double turtleBounds = turtle.getBounds(); // image bounds

		// find the scale
		double iw = turtleBounds.width; // image width
		double ih = turtleBounds.height; // image height
		double pw = myPaper.getMarginWidth();
		double ph = myPaper.getMarginHeight();
		double px = (myPaper.getMarginRight()+myPaper.getMarginLeft())*0.5;
		double py = (myPaper.getMarginTop()+myPaper.getMarginBottom())*0.5;
		double ratioH = ph/ih;
		double ratioW = pw/iw;
		double ratio = 1;
		if(fillPage == false) ratio=Math.min(ratioW, ratioH);
		else 				  ratio=Math.max(ratioW, ratioH);
		
		double ix = turtleBounds.getCenterX();
		double iy = turtleBounds.getCenterY();
		
		// apply
		Turtle result = new Turtle(turtle);
		result.translate(-ix,-iy);
		result.scale(ratio,ratio);
		result.translate(px,py);
		
		return result;
	}
}
