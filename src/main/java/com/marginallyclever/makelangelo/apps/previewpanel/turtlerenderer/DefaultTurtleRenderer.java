package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;

/**
 * Draws Turtle instructions one line segment at a time.
 * @author Dan Royer
 *
 */
public class DefaultTurtleRenderer implements TurtleRenderer {
	private GL3 gl;
	private Color colorTravel = Color.GREEN;
	private Color colorDraw = Color.BLACK;
	private boolean showPenUp = false;
	private float penDiameter = 1;

	private final Mesh mesh = new Mesh();
	private boolean isDone = false;
	
	@Override
	public void start(GL3 gl) {
		this.gl = gl;
		showPenUp = GFXPreferences.getShowPenUp();

		mesh.setRenderStyle(GL3.GL_TRIANGLES);
	}

	@Override
	public void end() {
		// end drawing lines
		isDone=true;
		mesh.render(gl);
	}

	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		Line2QuadHelper.thicken(mesh, p0, p1, colorDraw, penDiameter);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		if(!showPenUp) return;
		Line2QuadHelper.thicken(mesh, p0, p1, colorTravel, penDiameter);
	}

	@Override
	public void setPenDownColor(Color color) {
		colorDraw = color;
	}

	@Override
	public void setPenUpColor(Color color) {
		colorTravel = color;
	}
	
	@Override
	public void setPenDiameter(double penDiameter) {
		this.penDiameter = (float)penDiameter;
	}

	@Override
	public String getTranslatedName() {
		return Translator.get("DefaultTurtleRenderer.name");
	}

	/**
	 * Reset any internal state to defaults.  This makes sure rendering optimizations cleaned
	 * up when the turtle is changed.
	 */
	@Override
	public void reset() {
		mesh.clear();
		isDone=false;
	}
}
