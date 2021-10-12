package com.marginallyclever.makelangelo.plotter.marlinSimulation;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;

public class MarlinSimulationBlock {
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
		System.out.print("S");
		System.out.print("\t"+id);
		System.out.print("\t"+start);
		System.out.print("\t"+end);
		System.out.print("\t"+delta);
		System.out.print("\t"+normal);
		System.out.print("\t"+StringHelper.formatDouble(end_s));
		System.out.print("\t"+StringHelper.formatDouble(feedrate));
		System.out.print("\t"+StringHelper.formatDouble(distance));
		System.out.print("\t"+StringHelper.formatDouble(nominalSpeed));
		System.out.print("\t"+StringHelper.formatDouble(entrySpeed));
		System.out.print("\t"+StringHelper.formatDouble(exitSpeed));
		System.out.print("\t"+StringHelper.formatDouble(entrySpeedMax));
		System.out.print("\t"+StringHelper.formatDouble(allowableSpeed));
		System.out.print("\t"+StringHelper.formatDouble(acceleration));
		System.out.print("\t"+StringHelper.formatDouble(accelerateUntilD));
		System.out.print("\t"+StringHelper.formatDouble(plateauD));
		System.out.print("\t"+StringHelper.formatDouble(decelerateAfterD));
		System.out.print("\t"+StringHelper.formatDouble(accelerateUntilT));
		System.out.print("\t"+StringHelper.formatDouble(decelerateAfterT));
		System.out.print("\t"+(nominalLength?1:0));
		System.out.println();
	}
}