package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makelangelosettingspanel.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

/**
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} with a new color every time the pen is lowered.
 * This illustrates each "loop" of the drawing.
 * @author Dan Royer
 */
public class SeparateLoopTurtleRenderer implements TurtleRenderer {
	private GL2 gl2;
	
	private final ColorRGB colorTravel = new ColorRGB(0,255,0);
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter = 1;
	private int moveCounter;
		
	@Override
	public void start(GL2 gl2) {
		this.gl2=gl2;
		showPenUp = GFXPreferences.getShowPenUp();

		// Multiply blend mode
		gl2.glBlendFunc(GL2.GL_DST_COLOR, GL2.GL_ZERO);

		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		gl2.glLineWidth(penDiameter);

		gl2.glBegin(GL2.GL_LINES);
		moveCounter=0;
	}

	@Override
	public void end() {
		gl2.glEnd();
		// restore pen diameter
		gl2.glLineWidth(lineWidthBuf[0]);
		// restore blend mode
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void setDrawColor() {
		switch(moveCounter%7) {
		case 0 -> gl2.glColor3d(1,0,0);
		case 1 -> gl2.glColor3d(0,0.4,0);
		case 2 -> gl2.glColor3d(0,0,1);
		case 3 -> gl2.glColor3d(1,1,0);
		case 4 -> gl2.glColor3d(1,0,1);
		case 5 -> gl2.glColor3d(0,1,1);
		case 6 -> gl2.glColor3d(0,0,0);
		}
		moveCounter++;
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(showPenUp) {		
			gl2.glColor3d(
					colorTravel.getRed() / 255.0,
					colorTravel.getGreen() / 255.0,
					colorTravel.getBlue() / 255.0);
			gl2.glVertex2d(p0.x, p0.y);
			gl2.glVertex2d(p1.x, p1.y);
		}
		setDrawColor();
	}

	@Override
	public void setPenDownColor(ColorRGB color) {}

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
        return Translator.get("SeparateLoopTurtleRenderer.name");
    }

	/**
	 * Reset any internal state to defaults.  This makes sure rendering optimizations cleaned
	 * up when the turtle is changed.
	 */
	@Override
	public void reset() {}
}
