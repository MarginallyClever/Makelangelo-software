package com.marginallyclever.makelangelo.plotter;

import com.jogamp.opengl.GL2;

public class CoreXYPlotter extends CartesianPlotter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4469687136782553644L;

	public CoreXYPlotter() {
		super();
	}
	
	@Override
	public String getHello() {
		return "HELLO WORLD! I AM COREXY #";
	}
	
	@Override
	public void render(GL2 gl2) {
		// TODO draw me better.
		super.render(gl2);
	}

	@Override
	public String getName() {
		return "CoreXY";
	}
}
