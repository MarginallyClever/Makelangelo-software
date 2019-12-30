package com.marginallyclever.artPipeline;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.Turtle.MoveType;
import com.marginallyclever.convenience.Turtle.Movement;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * See https://www.marginallyclever.com/2019/12/lets-talk-about-where-makelangelo-software-is-going-in-2020/
 * @author Dan Royer
 * 
 */
public class ArtPipeline {

	/**
	 * Offers to look for a better route through the turtle history that means fewer travel moves.
	 */
	public void checkReorder(Turtle turtle, MakelangeloRobotSettings settings) {
		// TODO finish me
		/*
		int result = JOptionPane.showConfirmDialog(myPanel, "Avoid needless travel?", "Optimize", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {
			// history is made of changes, travels, and draws
			// look at the section between two changes.
			  // look at all pen down moves in the section.
			    // if two pen down moves share a start/end, then they are connected and belong in a single segment.
		}
		*/
	}

	/**
	 * Offers to optimize your gcode by chopping out very short line segments.
	 * It travels the entire path and drops any pen-down segment shorter than 
	 * minimumStepSize.
	 */
	public void checkSimplify(Turtle turtle, MakelangeloRobotSettings settings) {
		int result = JOptionPane.showConfirmDialog(null, "Simplify?", "Optimize", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {
			Log.info("checkSimplify() begin");
			ArrayList<Movement> toKeep = new ArrayList<Movement>();

			double minimumStepSize=1;

			boolean isUp=true;
			double ox=settings.getHomeX();
			double oy=settings.getHomeY();
			double sum=0;
			double dx,dy;
			Movement previous=null;
			
			for( Movement m : turtle.history ) {
				switch(m.type) {
				case DRAW:
					dx=m.x-ox;
					dy=m.y-oy;
					sum+=Math.sqrt(dx*dx+dy*dy);
					if(isUp || sum>minimumStepSize) {
						toKeep.add(m);
						sum=0;
					}isUp=false;
					ox=m.x;
					oy=m.y;
					previous=m;
					break;
				case TRAVEL:
					if(!isUp && sum>0 ) {
						if(previous!=null && previous.type==Turtle.MoveType.DRAW) {
							toKeep.add(previous);
						}
					}
					isUp=true;
					toKeep.add(m);
					ox=m.x;
					oy=m.y;
					sum=0;
					previous=m;
					break;
				default:
					toKeep.add(m);
					previous=m;
					break;
				}
			}
			int os = turtle.history.size();
			int ns = toKeep.size();
			turtle.history = toKeep;
			Log.info("checkSimplify() end (was "+os+" is now "+ns+")");
		}
	}

	
	
	/**
	 * Offers to resize your loaded image to fit the margins perfectly.
	 */
	protected void checkResize(Turtle turtle, MakelangeloRobotSettings settings) {	
		{
			int result = JOptionPane.showConfirmDialog(null, "Resize to fit inside margins?", "Resize", JOptionPane.YES_NO_OPTION);
			if(result == JOptionPane.YES_OPTION) {
				Point2D top = new Point2D();
				Point2D bottom = new Point2D();
				turtle.getBounds(top, bottom);
				
				double tw = top.x-bottom.x;
				double th = top.y-bottom.y;
				double nh=th;
				double nw=tw;
				double w = settings.getMarginWidth();
				double h = settings.getMarginHeight();
				double ratioW=1,ratioH=1;
				ratioH = h/nh;
				ratioW = w/nw;
				// use < to fit in the page.
				double ratio = ratioW<ratioH?ratioW:ratioH;
				turtle.scale(ratio,ratio);
			}
		}
		{
			int result = JOptionPane.showConfirmDialog(null, "Resize to fill margins?", "Resize", JOptionPane.YES_NO_OPTION);
			if(result == JOptionPane.YES_OPTION) {
				Point2D top = new Point2D();
				Point2D bottom = new Point2D();
				turtle.getBounds(top, bottom);
				
				double tw = top.x-bottom.x;
				double th = top.y-bottom.y;
				double nh=th;
				double nw=tw;
				double w = settings.getMarginWidth();
				double h = settings.getMarginHeight();
				double ratioW=1,ratioH=1;
				ratioH = h/nh;
				ratioW = w/nw;
				// use > to fill the page.
				double ratio = ratioW>ratioH?ratioW:ratioH;
				turtle.scale(ratio,ratio);
			}
		}
	}

	protected void cropTurtleToPageMargin(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle==null) return;
		
		Log.info("cropTurtleToPageMargin() start");

		ArrayList<Movement> oldHistory = turtle.history;
		turtle.history = new ArrayList<Movement>();
		
		// limits we will need for rectangle
		Point2D rMax = new Point2D(settings.getMarginRight(),settings.getMarginTop());
		Point2D rMin = new Point2D(settings.getMarginLeft(),settings.getMarginBottom());
		// working space for clipping
		Point2D P0 = new Point2D(); 
		Point2D P1 = new Point2D(); 
		
		Movement prev=null;
		
		for( Movement m : oldHistory ) {
			switch(m.type) {
			case DRAW:
			case TRAVEL:
				if(prev!=null) {
					P0.set(prev.x, prev.y);
					P1.set(m.x, m.y);
					boolean result = Clipper2D.clipLineToRectangle(P0,P1,rMax,rMin);
					// !result means full crop, do nothing.
					if(result) {
						// partial crop.  Which end(s)?
						boolean startCropped=MathHelper.lengthSquared(P0.x-prev.x, P0.y-prev.y)>1e-8;
						boolean   endCropped=MathHelper.lengthSquared(P1.x-   m.x, P1.y-   m.y)>1e-8;
						
						if(startCropped && endCropped) {
							// crosses rectangle, both ends out.
							turtle.history.add(turtle.new Movement(P0.x,P0.y,MoveType.TRAVEL));
							turtle.history.add(m);
							Movement m2=turtle.new Movement(P1.x,P1.y,m.type);
							turtle.history.add(m2);
						} else if(!startCropped && !endCropped) {
							turtle.history.add(m);
						} else if(endCropped) {
							// end cropped, leaving the rectangle
							Movement m2=turtle.new Movement(P1.x,P1.y,m.type);
							turtle.history.add(m2);
						} else {
							// start cropped, coming back into rectangle
							turtle.history.add(turtle.new Movement(P0.x,P0.y,MoveType.TRAVEL));
							turtle.history.add(m);
						}
					}
				}
				prev=m;
				
				break;
			default:
				turtle.history.add(m);
				break;
			}
		}
		
		// There may be some dumb travel moves left. (several travels in a row.)
		
		int oldSize= oldHistory.size();
		int newSize= turtle.history.size();
		Log.info("cropTurtleToPageMargin() end (was "+oldSize+" now "+newSize+")");
	}

	public void makeChecks(Turtle turtle, MakelangeloRobotSettings settings) {
		checkResize(turtle,settings);
		checkReorder(turtle,settings);
		checkSimplify(turtle,settings);
		cropTurtleToPageMargin(turtle,settings);
	}
}
