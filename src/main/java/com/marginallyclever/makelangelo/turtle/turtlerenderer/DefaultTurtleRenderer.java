package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makelangelosettingspanel.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;

/**
 * Draws Turtle instructions one line segment at a time.
 * @author Dan Royer
 *
 */
public class DefaultTurtleRenderer implements TurtleRenderer {
	private GL2 gl2;
	private Color colorTravel = Color.GREEN;
	private Color colorDraw = Color.BLACK;
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter =1;
	private boolean isPenUp = true;
	
	@Override
	public void start(GL2 gl2) {
		this.gl2 = gl2;
		showPenUp = GFXPreferences.getShowPenUp();
		isPenUp = true;

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
	}

	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isPenUp) {
			gl2.glColor4d(
					colorDraw.getRed() / 255.0,
					colorDraw.getGreen() / 255.0,
					colorDraw.getBlue() / 255.0,
					colorDraw.getAlpha() / 255.0);
			isPenUp = false;
		}

		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(!isPenUp) {
			isPenUp = true;
			if(showPenUp) {
				gl2.glColor4d(
						colorTravel.getRed() / 255.0,
						colorTravel.getGreen() / 255.0,
						colorTravel.getBlue() / 255.0,
						colorTravel.getAlpha() / 255.0);
			}
		}
		if(!showPenUp) return;

		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
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
	public void reset() {}
}
