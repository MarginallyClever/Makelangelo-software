package com.marginallyclever.artPipeline;

import java.awt.geom.Rectangle2D;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class ResizeTurtleToPaper {
	public static void run(Turtle turtle,MakelangeloRobotSettings settings,boolean fillPage) {
		Rectangle2D.Double turtleBounds = turtle.getBounds(); // image bounds

		
		// find the scale
		double iw = turtleBounds.width; // image width
		double ih = turtleBounds.height; // image height
		double pw = settings.getMarginWidth();
		double ph = settings.getMarginHeight();
		double px = (settings.getMarginRight()+settings.getMarginLeft())*0.5;
		double py = (settings.getMarginTop()+settings.getMarginBottom())*0.5;
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
	}
}
