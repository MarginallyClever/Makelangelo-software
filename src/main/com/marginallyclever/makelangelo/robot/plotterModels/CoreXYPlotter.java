package com.marginallyclever.makelangelo.robot.plotterModels;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.robot.Plotter;

public class CoreXYPlotter extends CartesianPlotter {
	@Override
	public String getHello() {
		return "HELLO WORLD! I AM COREXY #";
	}
	
	@Override
	public void render(GL2 gl2, Plotter plotter) {
		// TODO draw me better.
		super.render(gl2, plotter);
	}
}
