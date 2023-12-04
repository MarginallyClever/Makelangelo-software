package com.marginallyclever.makelangelo.makeart.tools;

import com.marginallyclever.makelangelo.makeart.TurtleModifierAction;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Scale the input {@link Turtle} to fit the margins of the {@link Paper} provided.
 * @author Dan Royer
 */
public class ResizeTurtleToPaperAction extends TurtleModifierAction {
	private final Paper myPaper;
	private final boolean fillPage;
	
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
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double pw = rect.getWidth();
		double ph = rect.getHeight();
		double px = myPaper.getCenterX();
		double py = myPaper.getCenterY();
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
