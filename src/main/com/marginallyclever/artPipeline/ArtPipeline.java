package com.marginallyclever.artPipeline;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.Turtle.MoveType;
import com.marginallyclever.convenience.Turtle.Movement;
import com.marginallyclever.makelangelo.log.Log;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * See https://www.marginallyclever.com/2019/12/lets-talk-about-where-makelangelo-software-is-going-in-2020/
 * @author Dan Royer
 * 
 */
public class ArtPipeline {
	public class Line2D {
		public Point2D a,b;
		public ColorRGB c;
		public boolean flag;

		public Line2D(Point2D a, Point2D b, ColorRGB c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		public void flip() {
			Point2D temp=b;
			b=a;
			a=temp;
		}
		public String toString() {
			return "("+a.x+","+a.y+")-("+b.x+","+b.y+")";
		}
		public double physicalLengthSquared() {
			double dx=a.x-b.x;
			double dy=a.y-b.y;
			return dx*dx + dy*dy;
		}
	}
	public class Sequence2D {
		public ArrayList<Line2D> lines;
		boolean isClosed;
		
		public Sequence2D() {
			lines = new ArrayList<Line2D>();
			isClosed=false;
		}
		
		public void flip() {
			try {
				Collections.reverse(lines);
			} catch(Exception e) {
				e.printStackTrace();
			}
			for( Line2D line : lines ) {
				line.flip();
			}
		}
	}
	
	protected ArtPipelinePanel myPanel;
	
	/**
	 * Offers to look for a better route through the turtle history that means fewer travel moves.
	 * @param turtle
	 * @param settings
	 */
	public void reorder(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle.history.size()==0) return;
		
		Log.message("checkReorder() begin");
		// history is made of changes, travels, and draws
		// look at the section between two changes.
		//   look at all pen down moves in the section.
		//     if two pen down moves share a start/end, then they are connected in sequence.
		
		// build a list of all the pen-down lines while remembering their color.
		ArrayList<Line2D> originalLines = new ArrayList<Line2D>();
		Movement previousMovement=null;
		ColorRGB color = new ColorRGB(0,0,0);

		Log.message("  Found "+turtle.history.size()+" instructions.");
		
		for( Movement m : turtle.history ) {
			switch(m.type) {
			case DRAW:
				if(previousMovement!=null) {
					Line2D line = new Line2D(
							new Point2D(previousMovement.x,previousMovement.y),
							new Point2D(m.x,m.y),
							color);
					if(line.physicalLengthSquared()>0) {
						originalLines.add(line);
					}
				}
				previousMovement = m;
				break;
			case TRAVEL:
				previousMovement = m;
				break;
			case TOOL_CHANGE:
				color = m.getColor();
				break;
			}
		}

		Log.message("  Converted to "+originalLines.size()+" lines.");

		final double EPSILON = 0.1;
		final double EPSILON2 = EPSILON*EPSILON;

		ArrayList<Line2D> newLines = new ArrayList<Line2D>();
		
		// remove duplicate lines.
		for(int a=0;a<originalLines.size();++a) {
			Line2D aa = originalLines.get(a);
			int b;
			for(b=a+1;b<originalLines.size();++b) {
				Line2D bb = originalLines.get(b);
				if( distanceBetweenPointsSquared(aa.a, bb.a)<EPSILON2 &&
					distanceBetweenPointsSquared(aa.b, bb.b)<EPSILON2 ) {
					break;
				}
				if( distanceBetweenPointsSquared(aa.a, bb.b)<EPSILON2 &&
					distanceBetweenPointsSquared(aa.b, bb.a)<EPSILON2 ) {
					break;
				}
			}
			if(b==originalLines.size()) {
				// aa does not match any line in the list.
				newLines.add(aa);
			}
		}
		
		int duplicates = originalLines.size() - newLines.size();
		originalLines = newLines;
		Log.message("  - "+duplicates+" duplicates = "+originalLines.size()+" lines.");

		// now sort the lines into contiguous groups.
		// from any given "active" line, search all remaining lines for a match
		// if a match is found, add it to the sorted list and make the match into the active line.
		// repeat until all lines exhausted.  this is O(n*n) hard and pretty slow.
		// TODO sort the lines into subgroups for faster searching?
		ArrayList<Sequence2D> segments = new ArrayList<Sequence2D>();
		Sequence2D activeSequence=null;
		Line2D activeLine=null;
		
		// found tracks how many times we flip the active sequence.
		// when it is 2 we have never flipped the sequence - we're still looking for more matches to the tail.
		// when it is 1 we have flipped once - we are now matching to the tail of the flipped sequence (aka the head).
		// when it is 0 we have flipped twice, there can be no more matches, start a new sequence.
		int found=0;
		double EPSILON_CONNECTED=2;  // TODO make this user-tweakable
		
		double d, bestD;
		Point2D p;
		Line2D bestLine;
		boolean bestFlip;
		
		while(!originalLines.isEmpty()) {
			if(found==0) {
				activeSequence = new Sequence2D();
				segments.add(activeSequence);
				activeLine=originalLines.remove(0);
				activeSequence.lines.add(activeLine);
				found=2;
			}

			p = activeLine.b;
			bestD = Double.MAX_VALUE;
			bestLine = null;
			bestFlip=false;
			
			for( Line2D toSort : originalLines ) {
				if(toSort.c.equals(activeLine.c)==false) continue;

				d = distanceBetweenPointsSquared(p, toSort.a);
				if(bestD > d) {
					bestD = d;
					bestLine = toSort;
					bestFlip=false;
				}
				d = distanceBetweenPointsSquared(p, toSort.b);
				if(bestD > d) {
					bestD = d;
					bestLine = toSort;
					bestFlip=true;
				}
			}
			
			// now we have the best line.
			if(bestD<EPSILON_CONNECTED) {
				// match is close enough to be added to this sequence.
				originalLines.remove(bestLine);
				if(bestFlip) bestLine.flip();
				activeSequence.lines.add(activeLine);
				activeLine=bestLine;
			} else {
				// match is too far.  nothing found.
				found--;
				// flip the whole sequence.
				// found==1 I'll match to the other end.
				// found==0 I'll be done with this sequence and return it to the original state.
				activeSequence.flip();
				int s = activeSequence.lines.size();
				activeLine = activeSequence.lines.get(s-1);
			}
		}
		
		Log.message(segments.size() + " sequences.");
		// rebuild the turtle history.
		Turtle t = new Turtle();
		// I assume the turtle history starts at the home position.
		t.setX(turtle.history.get(0).x);
		t.setY(turtle.history.get(0).y);

		//ColorRGB [] test = {new ColorRGB(255,0,0),new ColorRGB(0,255,0),new ColorRGB(0,255,255),new ColorRGB(255,0,255),new ColorRGB(255,255,0)};
		//int testX=0;
		
		for( Sequence2D seg : segments ) {
			int s = seg.lines.size();
			Line2D first = seg.lines.get(0);
			Line2D last = seg.lines.get(s-1);
			double len=distanceBetweenPointsSquared(first.a, last.b);
			boolean closed = s>1 && len<EPSILON_CONNECTED;
			Log.message("Sequence " + seg.lines.size()+(closed?" closed":"")+" lines. "+len);
			if(s==1 && len==0.0) {
				Log.message("ZERO? "+first.toString());
				Log.message("   vs "+last.toString());
			}
			//if(s==1) first.c.set(test[(testX++)%test.length]);
			
			boolean isFirst=true;
			for( Line2D line : seg.lines ) {
				// change color if needed
				if(line.c!=t.getColor()) {
					t.setColor(line.c);
				}
				if(isFirst) {
					isFirst=false;
					t.jumpTo(line.a.x,line.a.y);
				}
				t.moveTo(line.b.x,line.b.y);
			}
		}

		Log.message("  History now "+t.history.size()+" instructions.");
		turtle.history = t.history;
		Log.message("checkReorder() end");
	}

	public double distanceBetweenPointsSquared(Turtle.Movement a,Turtle.Movement b) {
		double dx = a.x-b.x;
		double dy = a.y-b.y;
		return MathHelper.lengthSquared(dx, dy); 
	}

	public double distanceBetweenPointsSquared(Point2D a,Point2D b) {
		double dx = a.x-b.x;
		double dy = a.y-b.y;
		return dx*dx + dy*dy; 
	}
	
	public boolean thesePointsAreTheSame(Point2D a,Point2D b,double epsilon) {
		if(a==b) return true;
		
		// close enough ?
		double dx = a.x-b.x;
		double dy = a.y-b.y;
		//if(dx*dx>epsilon*epsilon) return false;
		//if(dy*dy>epsilon*epsilon) return false;
		return MathHelper.lengthSquared(dx, dy)<=epsilon*epsilon; 
	}
	
	/**
	 * Offers to optimize your gcode by chopping out very short line segments.
	 * It travels the entire path and drops any pen-down segment shorter than 
	 * minimumStepSize.
	 * @param turtle
	 * @param settings
	 */
	public void simplify(Turtle turtle, MakelangeloRobotSettings settings) {
		Log.message("checkSimplify() begin");
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
		Log.message("checkSimplify() end (was "+os+" is now "+ns+")");
	}

	
	
	/**
	 * Offers to resize your loaded image to fit inside the margins.
	 * @param turtle
	 * @param settings
	 */
	protected void resizeFit(Turtle turtle, MakelangeloRobotSettings settings) {	
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		turtle.getBounds(top, bottom);

		// find the scale
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
		
		// and the translation
		double x = (top.x+bottom.x)/2;
		double y = (top.y+bottom.y)/2;
		
		// and apply
		turtle.translate(-x,-y);
		turtle.scale(ratio,ratio);
	}

	/**
	 * 
	 * @param turtle
	 * @param settings
	 */
	protected void flipV(Turtle turtle, MakelangeloRobotSettings settings) {	
		turtle.scale(1,-1);
	}

	/**
	 * 
	 * @param turtle
	 * @param settings
	 */
	protected void flipH(Turtle turtle, MakelangeloRobotSettings settings) {	
		turtle.scale(-1,1);
	}
	
	/**
	 * Offers to resize your loaded image to fill the margins completely.
	 * @param turtle
	 * @param settings
	 */
	protected void resizeFill(Turtle turtle, MakelangeloRobotSettings settings) {	
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		turtle.getBounds(top, bottom);
		
		// find the scale
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
		
		// and the translation
		double x = (top.x+bottom.x)/2;
		double y = (top.y+bottom.y)/2;
		
		// and apply
		turtle.translate(-x,-y);
		turtle.scale(ratio,ratio);
	}

	/**
	 * 
	 * @param turtle
	 * @param settings
	 */
	protected void cropToPageMargin(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle==null) return;
		
		Log.message("cropTurtleToPageMargin() start");

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
		Log.message("cropTurtleToPageMargin() end (was "+oldSize+" now "+newSize+")");
	}

	/**
	 * 
	 * @param turtle
	 * @param settings
	 */
	public void processTurtle(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle.history.isEmpty()) return;
		
		while(turtle.isLocked()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				Log.message("processTurtle wait interrupted.");
				return;
			}
		}
		turtle.lock();
		try {
			if(shouldResizeFill()) resizeFill(turtle,settings);
			if(shouldResizeFit()) resizeFit(turtle,settings);
			if(shouldFlipV()) flipV(turtle,settings);
			if(shouldFlipH()) flipH(turtle,settings);
			if(shouldReorder()) reorder(turtle,settings);
			if(shouldSimplify()) simplify(turtle,settings);
			if(shouldCrop()) cropToPageMargin(turtle,settings);
		}
		finally {
			turtle.unlock();
		}
	}

	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldResizeFill() {
		if(myPanel!=null) return myPanel.shouldResizeFill();
		int result = JOptionPane.showConfirmDialog(null, "Resize to fill margins?", "Resize", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}

	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldResizeFit() {
		if(myPanel!=null) return myPanel.shouldResizeFit();
		int result = JOptionPane.showConfirmDialog(null, "Resize to fit inside margins?", "Resize", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}

	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldReorder() {
		if(myPanel!=null) return myPanel.shouldReorder();
		int result = JOptionPane.showConfirmDialog(null, "Avoid needless travel?", "Optimize", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}
	
	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldFlipV() {
		if(myPanel!=null) return myPanel.shouldFlipV();
		int result = JOptionPane.showConfirmDialog(null, "Flip vertical?", "Flip", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}
	
	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldFlipH() {
		if(myPanel!=null) return myPanel.shouldFlipH();
		int result = JOptionPane.showConfirmDialog(null, "Flip horizonal?", "Flip", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}
	
	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldSimplify() {
		if(myPanel!=null) return myPanel.shouldSimplify();
		int result = JOptionPane.showConfirmDialog(null, "Simplify?", "Optimize", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}
	
	/**
	 * 
	 * @return true or false
	 */
	public boolean shouldCrop() {
		if(myPanel!=null) return myPanel.shouldCrop();
		int result = JOptionPane.showConfirmDialog(null, "Crop to margins?", "Crop", JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.YES_OPTION);
	}
}
