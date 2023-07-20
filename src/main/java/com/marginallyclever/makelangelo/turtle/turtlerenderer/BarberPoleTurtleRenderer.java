package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makelangelosettingspanel.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

/**
 * Draws Turtle in red/blue sequence to show line segments.
 * @author Dan Royer
 *
 */
public class BarberPoleTurtleRenderer implements TurtleRenderer {
	private GL2 gl2;
	
	private final ColorRGB colorTravel = new ColorRGB(0,255,0);
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter =1;
	private int moveCounter;
		
	@Override
	public void start(GL2 gl2) {
		this.gl2=gl2;
		showPenUp = GFXPreferences.getShowPenUp();

		// Multiply blend mode
		gl2.glBlendFunc(GL2.GL_DST_COLOR, GL2.GL_ZERO);
		// set pen diameter
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		gl2.glLineWidth(penDiameter);

		gl2.glBegin(GL2.GL_LINES);
		moveCounter=0;
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
	
	private void setDrawColor() {
		if(moveCounter%2==0) gl2.glColor3d(1,0,0);
		//else if(moveCounter%3==1) gl2.glColor3d(1,0,1);
		else gl2.glColor3d(0,0,1);
		moveCounter++;
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		setDrawColor();
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
        return Translator.get("BarberPoleTurtleRenderer.name");
    }
}
