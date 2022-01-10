package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotterRendererFactory {
	private static final Logger logger = LoggerFactory.getLogger(PlotterRendererFactory.class);
	
	private static PlotterRenderer [] available = {
		new Makelangelo5(),
		new Makelangelo3_3(),
		new MakelangeloCustom(),
		new Cartesian(),
		new Zarplotter()
	};
	
	public static ArrayList<String> getNames() {
		ArrayList<String> v = new ArrayList<String>();

		for( PlotterRenderer p : available ) {
			v.add(new String(p.getName()));
		}
		
		return v;
	}

	public static Iterator<PlotterRenderer> iterator() {
		return (Arrays.asList(available)).iterator();
	}
	

	/**
	 * @param name the name to find in the list of {@link PlotterRenderer}s.
	 * @return the {@link PlotterRenderer} with the matching name.
	 */
	public static PlotterRenderer getByName(String name) {
		PlotterRenderer hr=null;
		try {
			// get version numbers
			Iterator<PlotterRenderer> i = PlotterRendererFactory.iterator();
			while (i.hasNext()) {
				PlotterRenderer hw = i.next();
				if (hw.getName().contentEquals(name)) {
					hr = hw.getClass().getDeclaredConstructor().newInstance();
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Hardware version instance failed. Defaulting to v5", e);
			hr = new Makelangelo5();
		}
		
		return hr;
	}
}
