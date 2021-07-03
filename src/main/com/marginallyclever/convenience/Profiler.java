package com.marginallyclever.convenience;

import java.util.ArrayList;

/**
 * mark() start and intervals, then report() to get the interval times in a single System.out.println().
 * @author Dan Royer
 *
 */
public class Profiler {
	private ArrayList<Long> times = new ArrayList<>();
	
	public void clear() {
		times.clear();
	}
	public void mark() {
		times.add(System.currentTimeMillis());
	}
	public void report() {
		Long t = times.get(0);
		String s = "profile: ";
		for( Long t1 : times ) {
			s+= (t1-t) +"\t";
			t1=t;
		}		
		System.out.println(s);
	}
}
