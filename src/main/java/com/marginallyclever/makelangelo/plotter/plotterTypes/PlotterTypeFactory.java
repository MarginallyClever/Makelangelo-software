package com.marginallyclever.makelangelo.plotter.plotterTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class PlotterTypeFactory {
	private static PlotterType [] available = {
		new Makelangelo5(),
		new Makelangelo3_3(),
		new MakelangeloCustom(),
		new Cartesian(),
		new Zarplotter()
	};
	
	public static ArrayList<String> getHardwareVersions() {
		ArrayList<String> v = new ArrayList<String>();

		for( PlotterType p : available ) {
			v.add(new String(p.getName()));
		}
		
		return v;
	}
	
	public static ArrayList<String> getNames() {
		ArrayList<String> v = new ArrayList<String>();

		for( PlotterType p : available ) {
			v.add(new String(p.getName()));
		}
		
		return v;
	}

	public static Iterator<PlotterType> iterator() {
		return (Arrays.asList(available)).iterator();
	}
}
