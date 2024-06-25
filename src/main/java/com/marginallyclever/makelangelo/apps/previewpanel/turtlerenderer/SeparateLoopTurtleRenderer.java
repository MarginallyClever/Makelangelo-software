package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;

/**
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} with a new color every time the pen is lowered.
 * This illustrates each "loop" of the drawing.
 * @author Dan Royer
 */
public class SeparateLoopTurtleRenderer implements TurtleRenderer {
	private GL3 gl;
	
	private Color colorTravel = Color.GREEN;
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter = 1;
	private int moveCounter;

	private final Mesh mesh = new Mesh();
	private boolean isDone = false;
		
	@Override
	public void start(GL3 gl) {
		this.gl=gl;
		showPenUp = GFXPreferences.getShowPenUp();

		mesh.setRenderStyle(GL3.GL_LINES);
		moveCounter=0;
	}

	@Override
	public void end() {
		isDone=true;
		mesh.render(gl);
	}
	
	private void setDrawColor() {
		switch(moveCounter%7) {
		case 0 -> mesh.addColor(1,0,0,1);
		case 1 -> mesh.addColor(0,0.4f,0,1);
		case 2 -> mesh.addColor(0,0,1,1);
		case 3 -> mesh.addColor(1,1,0,1);
		case 4 -> mesh.addColor(1,0,1,1);
		case 5 -> mesh.addColor(0,1,1,1);
		case 6 -> mesh.addColor(0,0,0,1);
		}
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		setDrawColor();  mesh.addVertex((float)p0.x, (float)p0.y,0);
		setDrawColor();  mesh.addVertex((float)p1.x, (float)p1.y,0);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		if(showPenUp) {
			float r = colorTravel.getRed() / 255.0f;
			float g = colorTravel.getGreen() / 255.0f;
			float b = colorTravel.getBlue() / 255.0f;
			float a = colorTravel.getAlpha() / 255.0f;

			mesh.addColor(r, g, b, a);
			mesh.addVertex((float) p0.x, (float) p0.y, 0);
			mesh.addColor(r, g, b, a);
			mesh.addVertex((float) p1.x, (float) p1.y, 0);
		}
		moveCounter++;
	}

	@Override
	public void setPenDownColor(Color color) {}

	@Override
	public void setPenUpColor(Color color) {
		colorTravel=(color);
	}

	@Override
	public void setPenDiameter(double penDiameter) {
		this.penDiameter =(float)penDiameter;
	}

    @Override
    public String getTranslatedName() {
        return Translator.get("SeparateLoopTurtleRenderer.name");
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
