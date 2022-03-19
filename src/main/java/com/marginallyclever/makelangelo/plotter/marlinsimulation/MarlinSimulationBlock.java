package com.marginallyclever.makelangelo.plotter.marlinSimulation;

import com.marginallyclever.convenience.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;

/**
 * {@link MarlinSimulationBlock} is one block in the queue of blocks inside a {@link MarlinSimulation}.
 * For more details, please see Marlin documentation.
 * @author Dan Royer
 * @since 7.24.0
 */
public class MarlinSimulationBlock {
	private static final Logger logger = LoggerFactory.getLogger(MarlinSimulationBlock.class);

	public static int counter=0;
	public int id;
	
	public Vector3d start = new Vector3d();
	public Vector3d end = new Vector3d();
	public Vector3d delta = new Vector3d();
	public Vector3d normal = new Vector3d();
	
	//public double start_s;
	public double end_s;
	//public double now_s;
	
	public double feedrate;
	
	public double distance;
	public double nominalSpeed;  // top speed in this segment
	public double entrySpeed;  // per second
	public double exitSpeed;  // per second
	public double acceleration;  // per second per second
	
	public double entrySpeedMax;
	public double accelerateUntilD;  // distance
	public double decelerateAfterD;  // distance
	public double plateauD;  // distance

	public double accelerateUntilT;  // seconds
	public double decelerateAfterT;  // seconds
	
	public double allowableSpeed;
	
	// when optimizing, should we recheck the entry + exit v of this segment?
	public boolean recalculate;
	// is this segment 100% full speed, end to end?
	public boolean nominalLength;
	// is the robot moving through this segment right now?
	public boolean busy;
	
	
	// delta is calculated here in the constructor.
	public MarlinSimulationBlock(final Vector3d endPose,final Vector3d deltaPose) {
		end.set(endPose);
		delta.set(deltaPose);
		normal.set(deltaPose);
		normal.normalize();
		start.sub(end,delta);
		
		id=counter++;
		distance = delta.length();
		busy=false;
		recalculate=true;
	}
	
	public void report() {
		String res = "S" + "\t" + id +
				"\t" + start +
				"\t" + end +
				"\t" + delta +
				"\t" + normal +
				"\t" + StringHelper.formatDouble(end_s) +
				"\t" + StringHelper.formatDouble(feedrate) +
				"\t" + StringHelper.formatDouble(distance) +
				"\t" + StringHelper.formatDouble(nominalSpeed) +
				"\t" + StringHelper.formatDouble(entrySpeed) +
				"\t" + StringHelper.formatDouble(exitSpeed) +
				"\t" + StringHelper.formatDouble(entrySpeedMax) +
				"\t" + StringHelper.formatDouble(allowableSpeed) +
				"\t" + StringHelper.formatDouble(acceleration) +
				"\t" + StringHelper.formatDouble(accelerateUntilD) +
				"\t" + StringHelper.formatDouble(plateauD) +
				"\t" + StringHelper.formatDouble(decelerateAfterD) +
				"\t" + StringHelper.formatDouble(accelerateUntilT) +
				"\t" + StringHelper.formatDouble(decelerateAfterT) +
				"\t" + (nominalLength ? 1 : 0);
		logger.debug(res);
	}
}