package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makelangelosettingspanel.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

/**
 * Draws Turtle instructions one line segment at a time.
 * @author Dan Royer
 *
 */
public class DefaultTurtleRenderer implements TurtleRenderer {
	private GL2 gl2;
	private final ColorRGB colorTravel = new ColorRGB(0,255,0);
	private final ColorRGB colorDraw = new ColorRGB(0,0,0);
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter =1;
	
	@Override
	public void start(GL2 gl2) {
		this.gl2=gl2;
		showPenUp = GFXPreferences.getShowPenUp();

		// Multiply blend mode
		gl2.glBlendFunc(GL2.GL_DST_COLOR, GL2.GL_ZERO);
		// set pen diameter
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		gl2.glLineWidth(penDiameter);
		// start drawing lines
		gl2.glBegin(GL2.GL_LINES);
	}

	@Override
	public void end() {
		// end drawing lines
		gl2.glEnd();
		// restore pen diameter
		gl2.glLineWidth(lineWidthBuf[0]);
		// restore blend mode
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		gl2.glColor3d(
				colorDraw.getRed() / 255.0,
				colorDraw.getGreen() / 255.0,
				colorDraw.getBlue() / 255.0);
		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(!showPenUp) return;
		
		gl2.glColor3d(
				colorTravel.getRed() / 255.0,
				colorTravel.getGreen() / 255.0,
				colorTravel.getBlue() / 255.0);
		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
	}

	@Override
	public void setPenDownColor(ColorRGB color) {
		colorDraw.set(color);
	}

	@Override
	public void setPenUpColor(ColorRGB color) {
		colorTravel.set(color);
	}
	
	@Override
	public void setPenDiameter(double penDiameter) {
		this.penDiameter =(float)penDiameter;
	}

	@Override
	public String getTranslatedName() {
		return Translator.get("DefaultTurtleRenderer.name");
	}
}
