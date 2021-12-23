package com.marginallyclever.makelangelo.makeArt;

import java.awt.geom.Rectangle2D;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class ResizeTurtleToPaper {
	public static Turtle run(Turtle turtle,Paper paper,boolean fillPage) {
		Rectangle2D.Double turtleBounds = turtle.getBounds(); // image bounds

		// find the scale
		double iw = turtleBounds.width; // image width
		double ih = turtleBounds.height; // image height
		double pw = paper.getMarginWidth();
		double ph = paper.getMarginHeight();
		double px = (paper.getMarginRight()+paper.getMarginLeft())*0.5;
		double py = (paper.getMarginTop()+paper.getMarginBottom())*0.5;
		double ratioH = ph/ih;
		double ratioW = pw/iw;
		double ratio = 1;
		if(fillPage == false) ratio=Math.min(ratioW, ratioH);
		else 				  ratio=Math.max(ratioW, ratioH);
		
		double ix = turtleBounds.getCenterX();
		double iy = turtleBounds.getCenterY();
		
		// apply
		turtle.translate(-ix,-iy);
		turtle.scale(ratio,ratio);
		turtle.translate(px,py);
		
		return turtle;
	}
}
