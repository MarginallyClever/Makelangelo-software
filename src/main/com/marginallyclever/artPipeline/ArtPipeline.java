package com.marginallyclever.artPipeline;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
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
	public class ArtLine {
		public Point2D a,b;
		public ColorRGB c;

		public ArtLine(Point2D a, Point2D b, ColorRGB c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}
	public class ArtSegment {
		public ArrayList<ArtLine> lines;
		boolean isClosed;
		
		public ArtSegment() {
			lines = new ArrayList<ArtLine>();
			isClosed=false;
		}
	}
	/**
	 * Offers to look for a better route through the turtle history that means fewer travel moves.
	 */
	public void checkReorder(Turtle turtle, MakelangeloRobotSettings settings) {
		if(turtle.history.size()==0) return;
		
		int result = JOptionPane.showConfirmDialog(null, "Avoid needless travel?", "Optimize", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {
			System.out.println("checkReorder() begin");
			// history is made of changes, travels, and draws
			// look at the section between two changes.
			//   look at all pen down moves in the section.
   			//     if two pen down moves share a start/end, then they are connected and belong in a single segment.
			
			// build a list of all the pen-down lines while remembering their color.
			ArrayList<ArtLine> originalLines = new ArrayList<ArtLine>();
			Movement previousMovement=null;
			ColorRGB color = new ColorRGB(0,0,0);
			
			for( Movement m : turtle.history ) {
				switch(m.type) {
				case DRAW:
					if(previousMovement!=null) {
						ArtLine line = new ArtLine(
								new Point2D(m.x,m.y),
								new Point2D(previousMovement.x,previousMovement.y),
								color);
						originalLines.add(line);
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

			System.out.println("  Found "+turtle.history.size()+" instructions.");
			int total = originalLines.size();
			System.out.println("  Found "+total+" lines.");

			// now sort the lines into contiguous groups.
			// from any given "active" line, search all remaining lines for a match
			// if a match is found, add it to the sorted list and make the match into the active line.
			// repeat until all lines exhausted.  this is O(n*n) hard and pretty slow.
			/// TODO sort the lines into subgroups for faster searching?
			ArrayList<ArtSegment> segments = new ArrayList<ArtSegment>();
			ArtSegment activeSegment=null;
			boolean found=false;
			ArtLine activeLine=null;
			int sorted=0;

			while(!originalLines.isEmpty()) {
				if(found==false) {
					// either this is the first time OR 
					// ( we found no connecting lines AND there are still originalLines left )
					// start a new segment.
					activeSegment = new ArtSegment();
					segments.add(activeSegment);
					// get a new active line
					activeLine = originalLines.remove(0);
					// put the active line in the active segment.
					activeSegment.lines.add(activeLine);
					// do some metrics
					sorted++;
					System.out.println("  "+StringHelper.formatDouble(100*(double)sorted/(double)total)+"%");
				}
				
				found=false;
				// find any line that starts or ends where this line ends.
				for( ArtLine toSort : originalLines ) {
					// only compare similar color lines
					if(toSort.c.equals(activeLine.c)==false) continue;
					
					// check if there's a match with end A.
					if(thesePointsAreTheSame(activeLine.b,toSort.a)) {
						// found!
						// put it in the sorted lines
						activeSegment.lines.add(toSort);
						originalLines.remove(toSort);
						// make it the new segment head
						activeLine = toSort;
						// do some metrics
						sorted++;
						// do it all again!
						found=true;
						break;
					}
					// check if there's a match with end B.
					else if(thesePointsAreTheSame(activeLine.b,toSort.b)) {
						// found!
						// oldLine follows active but oldLine is backwards.  flip toSort
						Point2D temp = toSort.b;
						toSort.b=toSort.a;
						toSort.a=temp;
						// then put it in the sorted lines
						activeSegment.lines.add(toSort);
						originalLines.remove(toSort);
						// and make it the new segment head
						activeLine = toSort;
						// do some metrics
						sorted++;
						// do it all again!
						found=true;
						break;
					}
					// else toSort does not connect to activeLine.b.
					// our grim search continues.
				}
				// yea tho we searched every originalLine left, there were no matches to be found.
				// do it all again from the top.
			}

			// all original lines are now sorted into segments.
			int closed=0;
			for( ArtSegment seg : segments ) {
				seg.isClosed = thesePointsAreTheSame(
						seg.lines.get(0).a,
						seg.lines.get(seg.lines.size()-1).b);
				if(seg.isClosed) closed++;
			}
			
			System.out.println("  Found "+segments.size()+" segments, "+closed+" closed.");
			
			// try to reorganize closed segments to shorten travels
			for( int i=0;i<segments.size()-1;++i ) {
				ArtSegment a=segments.get(i);
				ArtSegment b=segments.get(i+1);
				if(a.isClosed && b.isClosed) {
					// valid candidate
				}
			}
			
			// rebuild the turtle history.
			Turtle t = new Turtle();
			// I assume the turtle history starts at the home position.
			t.setX(turtle.history.get(0).x);
			t.setY(turtle.history.get(0).y);
			t.penUp();
			
			for( ArtSegment seg : segments ) {
				ArtLine first = seg.lines.get(0);
				// change color if needed
				if(first.c!=t.getColor()) {
					t.setColor(first.c);
				}
				// jump to start of segment
				t.jumpTo(first.a.x, first.a.y);

				// follow the segment to its end.
				for( ArtLine toAdd : seg.lines ) {
					t.moveTo(toAdd.b.x, toAdd.b.y);
				}
			}
			
			System.out.println("  History now "+t.history.size()+" instructions.");
			turtle.history = t.history;
			System.out.println("checkReorder() end");
		}
	}

	public boolean thesePointsAreTheSame(Point2D a,Point2D b) {
		if(a==b) return true;
		
		// close enough ?
		double dx = a.x-b.x;
		if(dx>1) return false;
		double dy = a.y-b.y;
		if(dy>1) return false;
		return (MathHelper.lengthSquared(dx, dy)<1e-6); 
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
		if(turtle.history.isEmpty()) return;
		
		checkResize(turtle,settings);
		checkReorder(turtle,settings);
		checkSimplify(turtle,settings);
		cropTurtleToPageMargin(turtle,settings);
	}
}
