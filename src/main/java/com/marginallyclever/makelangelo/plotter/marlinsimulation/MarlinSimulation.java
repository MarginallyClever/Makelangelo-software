package com.marginallyclever.makelangelo.plotter.marlinsimulation;


import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * {@link MarlinSimulation} is meant to be a 1:1 Java replica of Marlin's 'Planner' and 'Motor' classes. 
 * It is used to estimate the time to draw a set of gcode commands by a robot running Marlin 3D printer firmware.
 * @author Dan Royer
 * @since 7.24.0
 */
public class MarlinSimulation {
	private static final Logger logger = LoggerFactory.getLogger(MarlinSimulation.class);
	public static final double GRAVITYmag = 9800.0;  // mm/s/s
	
	private final Vector3d poseNow = new Vector3d();
	private final PlotterSettings settings;
	private double timeSum;
	private final LinkedList<MarlinSimulationBlock> queue = new LinkedList<>();
	
//	private boolean readyForCommands = true;

	private double [] previousSpeed = { 0,0,0 };
	private double previousSafeSpeed = 0;
	private double XMAX = 325;
	private double XMIN = -325;
	private double YMAX = 500;

	enum JerkType {
		CLASSIC_JERK,
		JUNCTION_DEVIATION,
		DOT_PRODUCT,
		NONE,
	};
	private JerkType jerkType = JerkType.CLASSIC_JERK;

	// Unit vector of previous path line segment
	private Vector3d previousNormal = new Vector3d();
	
	private double previousNominalSpeed=0;
	private double junction_deviation = 0.05;
	private boolean polargraphLimit=false;

	private final double [] maxJerk;
	
	public MarlinSimulation(PlotterSettings settings) {
		this.settings = settings;
		XMAX = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		XMIN = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		YMAX = settings.getDouble(PlotterSettings.LIMIT_TOP);
		maxJerk = settings.getDoubleArray(PlotterSettings.MAX_JERK);
	}
	
	/**
	 * Add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param destination destination (mm)
	 * @param feedrate (mm/s)
	 * @param acceleration (mm/s/s)
	 */
	protected void bufferLine(final Vector3d destination, double feedrate, double acceleration) {
		Vector3d delta = new Vector3d();
		delta.sub(destination,poseNow);
		
		acceleration = Math.min(settings.getDouble(PlotterSettings.MAX_ACCELERATION), acceleration);
		
		double len = delta.length();		
		double seconds = len / feedrate;
		int segments = (int)Math.ceil(seconds * settings.getInteger(PlotterSettings.SEGMENTS_PER_SECOND));
		int maxSeg = (int)Math.ceil(len / settings.getDouble(PlotterSettings.MIN_SEGMENT_LENGTH));
		segments = Math.max(1,Math.min(maxSeg,segments));
		Vector3d deltaSegment = new Vector3d(delta);
		deltaSegment.scale(1.0/segments);
		
		Vector3d temp = new Vector3d(poseNow);
		while(--segments>0) {
			temp.add(deltaSegment);
			bufferSegment(temp,feedrate,acceleration,deltaSegment);
		}
		bufferSegment(destination,feedrate,acceleration,deltaSegment);
	}
	
	/**
	 * add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param to destination position
	 * @param feedrate velocity (mm/s)
	 * @param acceleration (mm/s/s)
	 * @param cartesianDelta move (mm)
	 */
	protected void bufferSegment(final Vector3d to, final double feedrate, final double acceleration,final Vector3d cartesianDelta) {
		MarlinSimulationBlock block = new MarlinSimulationBlock(to,cartesianDelta);
		block.feedrate = feedrate;

		// zero distance?  do nothing.
		if(block.distance<=6.0/80.0) return;
		
		double inverse_secs = feedrate / block.distance;
		
		// slow down if the buffer is nearly empty.
		if( queue.size() >= 2 && queue.size() <= (settings.getInteger(PlotterSettings.BLOCK_BUFFER_SIZE)/2)-1 ) {
			long segment_time_us = Math.round(1000000.0f / inverse_secs);
			long timeDiff = settings.getInteger(PlotterSettings.MIN_SEG_TIME) - segment_time_us;
			if( timeDiff>0 ) {
				double nst = segment_time_us + Math.round(2.0 * timeDiff / queue.size());
				inverse_secs = 1000000.0 / nst;
			}
		}
		
		block.nominalSpeed = block.distance * inverse_secs;
		
		// find if speed exceeds any joint max speed.
		double [] currentSpeed = { 
			block.delta.x * inverse_secs,
			block.delta.y * inverse_secs,
			block.delta.z * inverse_secs
		};
		double speedFactor=1.0;
		double cs;
		for(double v : currentSpeed ) {
			cs = Math.abs(v);
			if( cs > feedrate ) {
				speedFactor = Math.min(speedFactor, feedrate/cs);
			}
		}

		// apply speed limit
		if(speedFactor<1.0) {
			for(int i=0;i<currentSpeed.length;++i) currentSpeed[0] *= speedFactor;
			block.nominalSpeed *= speedFactor;
		}

		if(polargraphLimit) {
			block.acceleration = limitPolargraphAcceleration(to,cartesianDelta,acceleration);
		} else {
			block.acceleration = acceleration;
		}
		
		// limit jerk between moves
		double vmax_junction;
		switch(jerkType) {
			case CLASSIC_JERK:        vmax_junction = classicJerk(block,currentSpeed,block.nominalSpeed);  break;
			case JUNCTION_DEVIATION:  vmax_junction = junctionDeviationJerk(block,block.nominalSpeed);  break;
			case DOT_PRODUCT:         vmax_junction = dotProductJerk(block);  break;
			default:                  vmax_junction = block.nominalSpeed;  break;
		}

		block.allowableSpeed = maxSpeedAllowed(-block.acceleration,settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED),block.distance);
		block.entrySpeedMax = vmax_junction;
		block.entrySpeed = Math.min(vmax_junction, block.allowableSpeed);
		block.nominalLength = ( block.allowableSpeed >= block.nominalSpeed );
		block.recalculate = true;
		
		previousNominalSpeed = block.nominalSpeed;
        System.arraycopy(currentSpeed, 0, previousSpeed, 0, previousSpeed.length);
		
		queue.add(block);
		poseNow.set(to);
		
		recalculateAcceleration();
	}
	
	private double dotProductJerk(MarlinSimulationBlock next) { 
		double vmax_junction = next.nominalSpeed * next.normal.dot(previousNormal) * 1.1;
		vmax_junction = Math.min(vmax_junction, next.nominalSpeed);
		vmax_junction = Math.max(vmax_junction, settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED));
		previousNormal.set(next.normal);
		
		return vmax_junction;
	}

	private double junctionDeviationJerk(MarlinSimulationBlock next,double nominalSpeed) {
		double vmax_junction=nominalSpeed;
		// Skip first block or when previousNominalSpeed is used as a flag for homing and offset cycles.
		if (queue.size() > 0 && previousNominalSpeed > 1e-6) {
			// Compute cosine of angle between previous and current path. (prev_unit_vec is negative)
			// NOTE: Max junction velocity is computed without sin() or acos() by trig half angle identity.
			double junction_cos_theta = (-previousNormal.x * next.normal.x)
									  + (-previousNormal.y * next.normal.y)
									  + (-previousNormal.z * next.normal.z);

			// NOTE: Computed without any expensive trig, sin() or acos(), by trig half angle identity of cos(theta).
			if (junction_cos_theta > 0.999999f) {
				// For a 0 degree acute junction, just set minimum junction speed.
				vmax_junction = settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED);
			} else {
				// Check for numerical round-off to avoid divide by zero.
				junction_cos_theta = Math.max(junction_cos_theta, -0.999999f); 

				// Convert delta vector to unit vector
				Vector3d junction_unit_vec = new Vector3d();
				junction_unit_vec.sub(next.normal, previousNormal);
				junction_unit_vec.normalize();
				if (junction_unit_vec.length() > 0) {
					final double junction_acceleration = limit_value_by_axis_maximum(next.acceleration,junction_unit_vec, settings.getDouble(PlotterSettings.MAX_ACCELERATION));
					// Trig half angle identity. Always positive.
					final double sin_theta_d2 = Math.sqrt(0.5 * (1.0 - junction_cos_theta)); 

					vmax_junction = junction_acceleration * junction_deviation * sin_theta_d2 / (1.0f - sin_theta_d2);

					if (settings.getBoolean(PlotterSettings.HANDLE_SMALL_SEGMENTS)) {
						// For small moves with >135° junction (octagon) find speed for approximate arc
						if (next.distance < 1 && junction_cos_theta < -0.7071067812f) {
							double junction_theta = Math.acos(-junction_cos_theta);
							// NOTE: junction_theta bottoms out at 0.033 which avoids divide by 0.
							double limit = (next.distance * junction_acceleration) / junction_theta;
							vmax_junction = Math.min(vmax_junction, limit);
						}

					}
				}
			}

			// Get the lowest speed
			vmax_junction = Math.min(vmax_junction, next.nominalSpeed);
			vmax_junction = Math.min(vmax_junction, previousNominalSpeed);
		} else {
			// Init entry speed to zero. Assume it starts from rest. Planner will correct
			// this later.
			vmax_junction = 0;
		}

		previousNormal.set(next.normal);

		return vmax_junction;
	}

	private double limit_value_by_axis_maximum(double max_value, Vector3d junction_unit_vec,double maxAcceleration) {
	    double limit_value = max_value;
	    
	    if(junction_unit_vec.x!=0) {
	    	if(limit_value * Math.abs(junction_unit_vec.x) > maxAcceleration) {
	    		limit_value = Math.abs( maxAcceleration / junction_unit_vec.x );
	      	}
	    }
	    if(junction_unit_vec.y!=0) {
	    	if(limit_value * Math.abs(junction_unit_vec.y) > maxAcceleration) {
	    		limit_value = Math.abs( maxAcceleration / junction_unit_vec.y );
	      	}
	    }
	    if(junction_unit_vec.z!=0) {
	    	if(limit_value * Math.abs(junction_unit_vec.z) > maxAcceleration) {
	    		limit_value = Math.abs( maxAcceleration / junction_unit_vec.z );
	      	}
	    }
	
	    return limit_value;
	}

	private double classicJerk(MarlinSimulationBlock next,double[] currentSpeed,double safeSpeed) {
		boolean limited=false;
		
		for(int i=0;i<currentSpeed.length;++i) {
			double jerk = Math.abs(currentSpeed[i]),
					maxj = maxJerk[i];
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
		
		double vmax_junction;
		
		if(!queue.isEmpty()) {
			// look at difference between this move and previous move
			MarlinSimulationBlock prev = queue.getLast();
			if(prev.nominalSpeed > 1e-6) {				
				vmax_junction = Math.min(next.nominalSpeed,prev.nominalSpeed);
				limited=false;

				double vFactor=0;
				double smallerSpeedFactor = vmax_junction / prev.nominalSpeed;

				for(int i=0;i<previousSpeed.length;++i) {
					double vExit = previousSpeed[i] * smallerSpeedFactor;
					double vEntry = currentSpeed[i];
					if(limited) {
						vExit *= vFactor;
						vEntry *= vFactor;
					}
					double jerk = (vExit > vEntry) ? ((vEntry>0 || vExit<0) ? (vExit-vEntry) : Math.max(vExit, -vEntry))
												   : ((vEntry<0 || vExit>0) ? (vEntry-vExit) : Math.max(-vExit, vEntry));
					if( jerk > maxJerk[i] ) {
						vFactor = maxJerk[i] / jerk;
						limited = true;
					}
				}
				if(limited) vmax_junction *= vFactor;
				
				double vmax_junction_threshold = vmax_junction * 0.99;
				if( previousSafeSpeed > vmax_junction_threshold && safeSpeed > vmax_junction_threshold ) {
					vmax_junction = safeSpeed;
				}
			} else {
				vmax_junction = safeSpeed;
			}
		} else {
			vmax_junction = safeSpeed;
		}

		previousSafeSpeed = safeSpeed;
		
		return vmax_junction;
	}

	private double limitPolargraphAcceleration(final Vector3d to, final Vector3d cartesianDelta, final double acceleration) {
		double maxAcceleration = settings.getDouble(PlotterSettings.MAX_ACCELERATION);
		
		// Adjust the maximum acceleration based on the plotter position to reduce
		// wobble at the bottom of the picture.
		// We only consider the XY plane.
		// Special thanks to https://www.reddit.com/user/zebediah49 for his math help.
		double ox = to.x - cartesianDelta.x;
		double oy = to.y - cartesianDelta.y;
		
		// if T is your target direction unit vector,
		double Tx = cartesianDelta.x;
		double Ty = cartesianDelta.y;
		double Rlen = (Tx*Tx) + (Ty*Ty); // always >=0
		if (Rlen > 0) {
			// only affects XY non-zero movement. Servo is not touched.
			Rlen = 1.0 / Math.sqrt(Rlen);
			Tx *= Rlen;
			Ty *= Rlen;
			
			// normal vectors pointing from plotter to motor
			double R1x = XMIN - ox; // to left
			double R1y = YMAX - oy; // to top
			double Rlen1 = 1.0 / Math.sqrt((R1x*R1x) + (R1y*R1y));// old_seg.a[0].step_count * UNITS_PER_STEP;
			R1x *= Rlen1;
			R1y *= Rlen1;

			double R2x = XMAX - ox; // to right
			double R2y = YMAX - oy; // to top
			double Rlen2 = 1.0 / Math.sqrt((R2x*R2x) + (R2y*R2y));// old_seg.a[1].step_count * UNITS_PER_STEP;
			R2x *= Rlen2;
			R2y *= Rlen2;

			// solve cT = -gY + k1 R1 for c [and k1]
			// solve cT = -gY + k2 R2 for c [and k2]
			double c1 = -GRAVITYmag * R1x / (Tx * R1y - Ty * R1x);
			double c2 = -GRAVITYmag * R2x / (Tx * R2y - Ty * R2x);

			// If c is negative, that means that that support rope doesn't limit the
			// acceleration; discard that c.
			double cT = -1;
			if (c1 > 0 && c2 > 0) {
				cT = (c1 < c2) ? c1 : c2;
			} else if (c1 > 0) {
				cT = c1;
			} else if (c2 > 0) {
				cT = c2;
			}

			// The maximum acceleration is given by cT if cT>0
			if (cT > 0) {
				maxAcceleration = Math.max(Math.min(maxAcceleration, cT), (double)settings.getDouble(PlotterSettings.MIN_ACCELERATION));
			}
		}
		return maxAcceleration;
	}

	protected void recalculateAcceleration() {
		recalculateBackwards();
		recalculateForwards();
		recalculateTrapezoids();
	}
	
	protected void recalculateBackwards() {
		MarlinSimulationBlock current;
		MarlinSimulationBlock next = null;
		Iterator<MarlinSimulationBlock> ri = queue.descendingIterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateBackwardsBetween(current,next);
			next = current;
		}
	}
	
	protected void recalculateBackwardsBetween(MarlinSimulationBlock current,MarlinSimulationBlock next) {
		double top = current.entrySpeedMax;
		if(current.entrySpeed != top || (next!=null && next.recalculate)) {
			double newEntrySpeed = current.nominalLength 
					? top
					: Math.min( top, maxSpeedAllowed( -current.acceleration, (next!=null? next.entrySpeed : settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED)), current.distance));
			current.entrySpeed = newEntrySpeed;
			current.recalculate = true;
		}
	}
	
	protected void recalculateForwards() {
		MarlinSimulationBlock current;
		MarlinSimulationBlock prev = null;
		Iterator<MarlinSimulationBlock> ri = queue.iterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateForwardsBetween(prev,current);
			prev = current;
		}
	}
	
	protected void recalculateForwardsBetween(MarlinSimulationBlock prev,MarlinSimulationBlock current) {
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
		MarlinSimulationBlock current=null;
		
		double currentEntrySpeed=0, nextEntrySpeed=0;		
		for( MarlinSimulationBlock next : queue ) {
			nextEntrySpeed = next.entrySpeed;
			if(current!=null) {
				if(current.recalculate || next.recalculate) {
					current.recalculate = true;
					if( !current.busy ) {
						recalculateTrapezoidForBlock(current, currentEntrySpeed, nextEntrySpeed);
					}
					current.recalculate = false;
				}
			}
			current = next;
			currentEntrySpeed = nextEntrySpeed;
		}
		
		if(current!=null) {
			current.recalculate = true;
			if( !current.busy ) {
				recalculateTrapezoidForBlock(current, currentEntrySpeed, settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED));
			}
			current.recalculate = false;
		}
	}
	
	protected void recalculateTrapezoidForBlock(MarlinSimulationBlock block, double entrySpeed, double exitSpeed) {
		if( entrySpeed < settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED) ) entrySpeed = settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED);
		if( exitSpeed  < settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED) ) exitSpeed  = settings.getDouble(PlotterSettings.MINIMUM_PLANNER_SPEED);
		
		double accel = block.acceleration;
		double accelerateD = estimateAccelerationDistance(entrySpeed, block.nominalSpeed, accel);
		double decelerateD = estimateAccelerationDistance(block.nominalSpeed, exitSpeed, -accel);
		double cruiseRate;
		double plateauD = block.distance - accelerateD - decelerateD;
		if( plateauD < 0 ) {
			// never reaches nominal v
			double d = Math.ceil(intersectionDistance(entrySpeed, exitSpeed, accel, block.distance));
			accelerateD = Math.min(Math.max(d, 0), block.distance);
			decelerateD = 0;
			plateauD = 0;
			cruiseRate = finalRate(accel,entrySpeed,accelerateD);
		} else {
			cruiseRate = block.nominalSpeed;
		}
		block.accelerateUntilD = accelerateD;
		block.decelerateAfterD = accelerateD + plateauD;
		block.entrySpeed = entrySpeed;
		block.exitSpeed = exitSpeed;
		block.plateauD = plateauD;
		
		double accelerateT = (cruiseRate - entrySpeed) / accel;
		double decelerateT = (cruiseRate - exitSpeed) / accel;
		double nominalT = plateauD/block.nominalSpeed;

		block.accelerateUntilT = accelerateT;
		block.decelerateAfterT = accelerateT + nominalT;
		block.end_s = accelerateT + nominalT + decelerateT;
		
		if(Double.isNaN(block.end_s)) {
			logger.debug("recalculateTrapezoidSegment() Uh oh");
		}
	}
	
	private double finalRate(double acceleration, double startV, double distance) {
		return Math.sqrt( (startV*startV) + 2.0 * acceleration*distance);
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
	
	// (endV^2 - startV^2) / 2a
	protected double estimateAccelerationDistance(final double initialRate, final double targetRate, final double accel) {
		if(accel == 0) return 0;
		return ( (targetRate*targetRate) - (initialRate*initialRate) ) / (accel * 2.0);
	}

	protected double intersectionDistance(final double startRate, final double endRate, final double accel, final double distance) {
		if(accel == 0) return 0;
		return ( 2.0 * accel * distance - (startRate*startRate) + (endRate*endRate) ) / (4.0 * accel);
	}

	public interface SegmentFunction {
		void run(MarlinSimulationBlock s);
	}
	
	public void historyAction(Turtle t,SegmentFunction consumer) {
		MarlinSimulationBlock.counter=0;

		double perSecond = 1.0/60.0;

		double travelFeedrate = settings.getDouble(PlotterSettings.FEED_RATE_TRAVEL) * perSecond;
		double drawFeedRate = settings.getDouble(PlotterSettings.FEED_RATE_DRAW) * perSecond;
		double penLiftTime = settings.getDouble(PlotterSettings.PEN_ANGLE_UP_TIME);
		double maxAcceleration = settings.getDouble(PlotterSettings.MAX_ACCELERATION);
		double upAngle = settings.getDouble(PlotterSettings.PEN_ANGLE_UP);
		double downAngle = settings.getDouble(PlotterSettings.PEN_ANGLE_DOWN);
		boolean isUp=true;
		
		Point2d home = settings.getHome();
		double lx=home.x;
		double ly=home.y;
		poseNow.set(lx,ly,upAngle);
		queue.clear();
				
		for(TurtleMove m : t.history) {			
			switch(m.type) {
			case DRAW_LINE:
				if(isUp) {
					isUp=false;
					bufferLine(new Vector3d(lx,ly,downAngle),penLiftTime,maxAcceleration);
				}
				bufferLine(new Vector3d(m.x,m.y,downAngle),drawFeedRate,maxAcceleration);
				lx=m.x;
				ly=m.y;
				break;
			case TRAVEL:
				if(!isUp) {
					isUp=true;
					bufferLine(new Vector3d(lx,ly,upAngle),penLiftTime,maxAcceleration);
				}
				bufferLine(new Vector3d(m.x,m.y,upAngle),travelFeedrate,maxAcceleration);
				lx=m.x;
				ly=m.y;
				break;
			default:
				break;
			}
			while(queue.size()>settings.getInteger(PlotterSettings.BLOCK_BUFFER_SIZE)) consumer.run(queue.remove(0));
		}
		while(!queue.isEmpty()) consumer.run(queue.remove(0));
	}
	
	// @return time in seconds to run sequence.
	public double getTimeEstimate(Turtle t) {
		timeSum=0;
		
		historyAction(t, (n)->{ timeSum += n.end_s; });
		
		return timeSum;
	}
}
