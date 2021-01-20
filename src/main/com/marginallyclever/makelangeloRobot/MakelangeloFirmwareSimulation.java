package com.marginallyclever.makelangeloRobot;

import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;


/**
 * Simulating the firmware inside a Makelangelo to more accurately estimate the time to draw an image.
 * @author Dan Royer
 * @since 7.24.0
 */
public class MakelangeloFirmwareSimulation {
	public static final int MAX_SEGMENTS = 32;
	public static final double MIN_SEGMENT_TIME = 25000.0/1000000.0;
	public static final double MAX_FEEDRATE = 200;   // mm/s
	public static final double MAX_ACCELERATION = 500;  // mm/s/s
	public static final double MINIMUM_PLANNER_SPEED = 0.05;  // mm/s
	public static final int SEGMENTS_PER_SECOND = 8;
	public static final double [] MAX_JERK = { 8, 8, 0.3 };
	
	// poseTo represents the desired destination. It could be null if there is none.  Roughly equivalent to Sixi2Live.poseSent.
	public Vector3d poseTo;
	// poseNow is the current position.  Roughly equivalent to Sixi2Live.poseReceived.
	protected Vector3d poseNow;
	
	protected LinkedList<MakelangeloFirmwareSimulationSegment> queue = new LinkedList<MakelangeloFirmwareSimulationSegment>();
	
	protected boolean readyForCommands;

	protected double [] previousSpeedArray = { 0,0,0 };
	protected double previousSafeSpeed = 0;
	
	public MakelangeloFirmwareSimulation() {
		readyForCommands = true;
	}
	
/*
	public void update(double dt) {
		if(!queue.isEmpty()) {
			MakelangeloFirmwareSimulationSegment seg = queue.getFirst();
			seg.busy=true;
			seg.now_s+=dt;
			double diff = 0;
			if(seg.now_s > seg.end_s) {
				diff = seg.now_s-seg.end_s;
				seg.now_s = seg.end_s;
			}
			
			updatePositions(seg);
			
			if(seg.now_s== seg.end_s) {
				queue.pop();
				// make sure the remainder isn't lost.
				if(!queue.isEmpty()) {
					queue.getFirst().now_s = diff;
				}
			}
			
			readyForCommands=(queue.size()<MAX_SEGMENTS); 
		}
	}*/

	/**
	 * override this to change behavior of joints over time.
	 * @param dt
	 *//*
	protected void updatePositions(MakelangeloFirmwareSimulationSegment seg) {
		if(poseNow==null) return;

		double dt = (seg.now_s - seg.start_s);
		
		// I need to know how much time has been spent accelerating, cruising, and decelerating in this segment.
		// acceleratingT will be in the range 0....seg.accelerateUntilT
		double acceleratingT = Math.min(dt,seg.accelerateUntilT);
		// deceleratingT will be in the range 0....(seg.end_s-seg.decelerateAfterT)
		double deceleratingT = Math.max(dt,seg.decelerateAfterT) - seg.decelerateAfterT;
		// nominalT will be in the range 0....(seg.decelerateAfterT-seg.accelerateUntilT)
		double nominalT = Math.min(Math.max(dt,seg.accelerateUntilT),seg.decelerateAfterT) - seg.accelerateUntilT;
		
		// now find the distance moved in each of those sections.
		double a = (seg.entrySpeed * acceleratingT) + (0.5 * seg.acceleration * acceleratingT*acceleratingT);
		double n = seg.nominalSpeed * nominalT;
		double d = (seg.nominalSpeed * deceleratingT) - (0.5 * seg.acceleration * deceleratingT*deceleratingT);
		double p = a+n+d;
		
		// find the fraction of the total distance travelled
		double fraction = p / seg.distance;
		fraction = Math.min(Math.max(fraction, 0), 1);
		
		boolean verbose=false;
		if(verbose) {
			System.out.print(a+" "+n+" "+d+" -> "+p+" / "+seg.distance + " = ");
			System.out.print(seg.end_s+" / "+seg.now_s+" / "+seg.start_s+" : "+fraction+" = ");
			System.out.print(seg.start+" ");
			System.out.print(seg.delta+" ");
		}
		
		// set pos = start + delta * fraction
		Vector3d temp = new Vector3d();
		temp.scale(fraction,seg.delta);
		poseNow.add(temp,seg.start);
		if(verbose) System.out.println(poseNow+" ");
		if(verbose) System.out.println();
	}*/

	/**
	 * add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param command
	 */
	protected void addDestination2(final Vector3d to, double feedrate, double acceleration) {
		Vector3d delta = new Vector3d();
		delta.sub(to,poseNow);
		double len = delta.length();
		double seconds = len / feedrate;
		int segments = (int)Math.ceil(seconds * SEGMENTS_PER_SECOND);
		if(segments<1) segments=1;
		//double inv_segments = 1.0 / (double)segments;
		//double segment_len_mm = len * inv_segments;
		delta.scale(1.0/segments);
		
		Vector3d temp = new Vector3d(poseNow);
		while(--segments>0) {
			temp.add(delta);
			addDestination(temp,feedrate,acceleration);
		}
		addDestination(to,feedrate,acceleration);
	}
	
	/**
	 * add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param command
	 */
	protected void addDestination(final Vector3d to, double feedrate, double acceleration) {
		poseTo=to;
		
		Vector3d start = (!queue.isEmpty()) ? queue.getLast().end : poseNow;
		
		MakelangeloFirmwareSimulationSegment next = new MakelangeloFirmwareSimulationSegment(start,to);
		poseNow=to;

		// zero distance?  do nothing.
		if(next.distance==0) return;
		
		double timeToEnd = next.distance / feedrate;

		// slow down if the buffer is nearly empty.
		if( queue.size() >= 2 && queue.size() <= (MAX_SEGMENTS/2)-1 ) {
			if( timeToEnd < MIN_SEGMENT_TIME ) {
				timeToEnd += (MIN_SEGMENT_TIME-timeToEnd)*2.0 / queue.size();
			}
		}
		
		next.nominalSpeed = next.distance / timeToEnd;
		
		// find if speed exceeds any joint max speed.
		double [] currentSpeedArray = { 
				next.delta.x / timeToEnd,
				next.delta.y / timeToEnd,
				next.delta.z / timeToEnd
		};
		double speedFactor=1.0;
		double cs;
		for(double v : currentSpeedArray ) {
			cs = Math.abs(v);
			if( cs > MAX_FEEDRATE ) {
				speedFactor = Math.min(speedFactor, MAX_FEEDRATE/cs);
			}
		}

		// apply speed limit
		if(speedFactor<1.0) {
			for(int i=0;i<currentSpeedArray.length;++i) currentSpeedArray[0]*=speedFactor;
			next.nominalSpeed *= speedFactor;
		}
		
		next.acceleration = acceleration;

		// limit jerk between moves
		double safeSpeed = next.nominalSpeed;
		boolean limited=false;
		
		for(int i=0;i<currentSpeedArray.length;++i) {
			double jerk = Math.abs(currentSpeedArray[i]),
					maxj = MAX_JERK[i];
			if( jerk > maxj ) {
				if(limited) {
					double mjerk = maxj * next.nominalSpeed;
					if( jerk * safeSpeed > mjerk ) safeSpeed = mjerk/jerk;
				} else {
					safeSpeed *= maxj / jerk;
					limited=true;
				}
			}
		}
		double vmax_junction = 0;
		
		if(queue.size()>0) { 
			// look at difference between this move and previous move
			MakelangeloFirmwareSimulationSegment prev = queue.getLast();
			if(prev.nominalSpeed > 1e-6) {				
				vmax_junction = Math.min(next.nominalSpeed,prev.nominalSpeed);
				limited=false;

				double vFactor=0;
				double smallerSpeedFactor = vmax_junction / prev.nominalSpeed;

				for(int i=0;i<previousSpeedArray.length;++i) {
					double vExit = previousSpeedArray[i] * smallerSpeedFactor;
					double vEntry = currentSpeedArray[i];
					if(limited) {
						vExit *= vFactor;
						vEntry *= vFactor;
					}
					double jerk = (vExit > vEntry) ? ((vEntry>0 || vExit<0) ? (vExit-vEntry) : Math.max(vExit, -vEntry))
												   : ((vEntry<0 || vExit>0) ? (vEntry-vExit) : Math.max(-vExit, vEntry));
					if( jerk > MAX_JERK[i] ) {
						vFactor = MAX_JERK[i] / jerk;
						limited = true;
					}
				}
				if(limited) {
					vmax_junction *= vFactor;
				}
				
				double vmax_junction_threshold = vmax_junction * 0.99;
				if( previousSafeSpeed > vmax_junction_threshold &&
				    safeSpeed > vmax_junction_threshold ) {
					vmax_junction = safeSpeed;
				}
			}
		} else {
			vmax_junction = safeSpeed;
		}
		
		previousSafeSpeed = safeSpeed;
		
		next.allowableSpeed = maxSpeedAllowed(-next.acceleration,MINIMUM_PLANNER_SPEED,next.distance);
		next.entrySpeedMax = vmax_junction;
		next.entrySpeed = Math.min(vmax_junction, next.allowableSpeed);
		next.nominalLength = ( next.allowableSpeed >= next.nominalSpeed );
		next.recalculate = true;
		//next.now_s = 0;
		//next.start_s = 0;
		//next.end_s = 0;
		
		for(int i=0;i<previousSpeedArray.length;++i) {
			previousSpeedArray[i] = currentSpeedArray[i];
		}
		
		recalculateTrapezoidSegment(next, next.entrySpeed, MINIMUM_PLANNER_SPEED);
		
		queue.add(next);
		
		recalculateAcceleration();
	}
	
	protected void recalculateAcceleration() {
		recalculateBackwards();
		recalculateForwards();
		recalculateTrapezoids();
	}
	
	protected void recalculateBackwards() {
		MakelangeloFirmwareSimulationSegment current;
		MakelangeloFirmwareSimulationSegment next = null;
		Iterator<MakelangeloFirmwareSimulationSegment> ri = queue.descendingIterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateBackwardsBetween(current,next);
			next = current;
		}
	}
	
	protected void recalculateBackwardsBetween(MakelangeloFirmwareSimulationSegment current,MakelangeloFirmwareSimulationSegment next) {
		double top = current.entrySpeedMax;
		if(current.entrySpeed != top || (next!=null && next.recalculate)) {
			double newEntrySpeed = current.nominalLength 
					? top
					: Math.min( top, maxSpeedAllowed( -current.acceleration, (next!=null? next.entrySpeed : 0), current.distance));
			current.entrySpeed = newEntrySpeed;
			current.recalculate = true;
		}
	}
	
	protected void recalculateForwards() {
		MakelangeloFirmwareSimulationSegment current;
		MakelangeloFirmwareSimulationSegment prev = null;
		Iterator<MakelangeloFirmwareSimulationSegment> ri = queue.iterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateForwardsBetween(prev,current);
			prev = current;
		}
	}
	
	protected void recalculateForwardsBetween(MakelangeloFirmwareSimulationSegment prev,MakelangeloFirmwareSimulationSegment current) {
		if(prev==null) return;
		if(!prev.nominalLength && prev.entrySpeed < current.entrySpeed) {
			double newEntrySpeed = maxSpeedAllowed(-prev.acceleration, prev.entrySpeed, prev.distance);
			if(newEntrySpeed < current.entrySpeed) {
				current.recalculate=true;
				current.entrySpeed = newEntrySpeed;
			}
		}
	}
	
	protected void recalculateTrapezoids() {
		MakelangeloFirmwareSimulationSegment current=null;
		
		boolean nextDirty;
		double currentEntrySpeed=0, nextEntrySpeed=0;
		int size = queue.size();
		
		for(int i=0;i<size;++i) {
			current = queue.get(i);
			int j = i+1;
			if(j<size) {
				MakelangeloFirmwareSimulationSegment next = queue.get(j);
				nextEntrySpeed = next.entrySpeed;
				nextDirty = next.recalculate;
			} else {
				nextEntrySpeed=0;
				nextDirty=false;
			}
			if( current.recalculate || nextDirty ) {
				current.recalculate = true;
				if( !current.busy ) {
					recalculateTrapezoidSegment(current, currentEntrySpeed, nextEntrySpeed);
				}
				current.recalculate = false;
			}
			current.recalculate=false;
			currentEntrySpeed = nextEntrySpeed;
		}
	}
	
	protected void recalculateTrapezoidSegment(MakelangeloFirmwareSimulationSegment seg, double entrySpeed, double exitSpeed) {
		if( entrySpeed < MINIMUM_PLANNER_SPEED ) entrySpeed = MINIMUM_PLANNER_SPEED;
		if( exitSpeed  < MINIMUM_PLANNER_SPEED ) exitSpeed  = MINIMUM_PLANNER_SPEED;
		
		double accel = seg.acceleration;
		double accelerateD = Math.ceil( estimateAccelerationDistance(entrySpeed, seg.nominalSpeed, accel));
		double decelerateD = Math.floor( estimateAccelerationDistance(seg.nominalSpeed, exitSpeed, -accel));
		double plateauD = seg.distance - accelerateD - decelerateD;
		if( plateauD < 0 ) {
			double half = Math.ceil(intersectionDistance(entrySpeed, exitSpeed, accel, seg.distance));
			accelerateD = Math.min(Math.max(half, 0), seg.distance);
			plateauD = 0;
		}
		seg.accelerateUntilD = accelerateD;
		seg.plateauD = plateauD;
		seg.decelerateAfterD = accelerateD + plateauD;
		
		double nominalT = plateauD/seg.nominalSpeed;
		
		// d = vt + 0.5att.  it's a quadratic, so t = -v +/- sqrt( v*v -2ad ) / a
		double nA = maxSpeedAllowed(-seg.acceleration,entrySpeed,accelerateD);
		double nD = maxSpeedAllowed(-seg.acceleration,exitSpeed, decelerateD);
		double accelerateT = ( -entrySpeed + nA ) / seg.acceleration;
		double decelerateT = ( -exitSpeed  + nD ) / seg.acceleration;
		
		seg.accelerateUntilT = accelerateT;
		seg.decelerateAfterT = accelerateT + nominalT;
		seg.end_s = accelerateT + nominalT + decelerateT;
		seg.entrySpeed = entrySpeed;
		seg.exitSpeed = exitSpeed;
		
		if(Double.isNaN(seg.end_s)) {
			System.out.println("Uh oh");
		}
	}

	/**
	 * Calculate the maximum allowable speed at this point, in order to reach 'targetVelocity' using 
	 * 'acceleration' within a given 'distance'.
	 * @param acceleration
	 * @param targetVelocity
	 * @param distance
	*/
	protected double maxSpeedAllowed( double acceleration, double targetVelocity, double distance ) {
		return Math.sqrt( (targetVelocity*targetVelocity) - 2 * acceleration * distance );
	}
	
	protected double estimateAccelerationDistance(final double initialRate, final double targetRate, final double accel) {
		if(accel == 0) return 0;
		return ( (targetRate*targetRate) - (initialRate*initialRate) ) / (accel * 2.0);
	}

	protected double intersectionDistance(final double startRate, final double endRate, final double accel, final double distance) {
		if(accel == 0) return 0;
		return ( 2.0 * accel * distance - (startRate*startRate) + (endRate*endRate) ) / (4.0 * accel);
	}

	/**
	 * @return time in seconds to run sequence.
	 */
	public double getTimeEstimate(Turtle t,MakelangeloRobotSettings settings) {
		double fu = settings.getPenUpFeedRate();
		double fd = settings.getPenDownFeedRate();
		double fz = settings.getZRate();
		double a = settings.getAcceleration();
		boolean isUp=true;
		double zu = settings.getPenUpAngle();
		double zd = settings.getPenDownAngle();
		
		poseNow = new Vector3d(settings.getHomeX(),settings.getHomeY(),zu);
		double lx=poseNow.x;
		double ly=poseNow.y;
		double sum=0;
		
		for(TurtleMove m : t.history) {
			switch(m.type) {
			case DRAW:
				if(isUp) {
					isUp=false;
					addDestination2(new Vector3d(lx,ly,isUp?zu:zd),fz,a);
				}
				addDestination2(new Vector3d(m.x,m.y,isUp?zu:zd),isUp?fu:fd,a); 
				lx=m.x;
				ly=m.y;
				break;
			case TRAVEL: 
				if(!isUp) {
					isUp=true;
					addDestination2(new Vector3d(lx,ly,isUp?zu:zd),fz,a);
				}
				addDestination2(new Vector3d(m.x,m.y,isUp?zu:zd),isUp?fu:fd,a); 
				lx=m.x;
				ly=m.y;
				break;
			default:
				break;
			}
			while(queue.size()>MAX_SEGMENTS) {
				MakelangeloFirmwareSimulationSegment s= queue.remove(0);
				s.report();
				sum += s.end_s;// - s.start_s;
			}
		}
		System.out.println("done");
		
		return sum;
	}
}
