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
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter =1;
	private boolean isPenUp = true;

	private final Mesh mesh = new Mesh();
	private boolean isDone = false;
	
	@Override
	public void start(GL3 gl) {
		this.gl = gl;
		showPenUp = GFXPreferences.getShowPenUp();
		isPenUp = true;

		mesh.setRenderStyle(GL3.GL_LINES);
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

		isPenUp = false;

		float r = colorDraw.getRed() / 255.0f;
		float g = colorDraw.getGreen() / 255.0f;
		float b = colorDraw.getBlue() / 255.0f;
		float a = colorDraw.getAlpha() / 255.0f;

		mesh.addColor(r,g,b,a);  mesh.addVertex((float)p0.x, (float)p0.y,0);
		mesh.addColor(r,g,b,a);  mesh.addVertex((float)p1.x, (float)p1.y,0);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		if(!showPenUp) return;

		float r = colorTravel.getRed() / 255.0f;
		float g = colorTravel.getGreen() / 255.0f;
		float b = colorTravel.getBlue() / 255.0f;
		float a = colorTravel.getAlpha() / 255.0f;

		mesh.addColor(r,g,b,a);  mesh.addVertex((float)p0.x, (float)p0.y,0);
		mesh.addColor(r,g,b,a);  mesh.addVertex((float)p1.x, (float)p1.y,0);
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
