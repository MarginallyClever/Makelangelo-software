package com.marginallyclever.makelangelo.plotter.plotterTypes;

public class PlotterFactory {
	public static PlotterType create(String name) throws Exception {
		if(name.contentEquals(Makelangelo5Marlin.class.getName())) return new Makelangelo5Marlin();
		else if(name.contentEquals(Makelangelo2.class.getName())) return new Makelangelo2();
		//else if(name.contentEquals(Makelangelo3.class.getName())) return new Makelangelo3();
		else if(name.contentEquals(Makelangelo3_3.class.getName())) return new Makelangelo3_3();
		else if(name.contentEquals(MakelangeloCustom.class.getName())) return new MakelangeloCustom();
		else if(name.contentEquals(Cartesian.class.getName())) return new Cartesian();
		//else if(name.contentEquals(Zarplotter.class.getName())) return new Zarplotter();
		else throw new Exception("PlotterFactory.create() doesn't recognize '"+name+"'.");
	}
}
