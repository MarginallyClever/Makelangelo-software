package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;
import java.util.ArrayList;

/**
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} such that each time the pen is lowered the color begins
 * with red and fades to blue as the pen is lifted.  This illustrates each "loop" of the drawing and the direction.
 * @author Dan Royer
 * @since 7.48.0
 */
public class DirectionLoopTurtleRenderer implements TurtleRenderer {
	private GL2 gl2;
	private Color colorTravel = Color.GREEN;
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter = 1;
	private final ArrayList<TurtleMove> points = new ArrayList<>();
		
	@Override
	public void start(GL2 gl2) {
		this.gl2=gl2;
		showPenUp = GFXPreferences.getShowPenUp();

		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		gl2.glLineWidth(penDiameter);

		gl2.glBegin(GL2.GL_LINES);
	}

	@Override
	public void end() {
		drawPoints();
		gl2.glEnd();
		// restore pen diameter
		gl2.glLineWidth(lineWidthBuf[0]);
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		points.add(p0);
		points.add(p1);
	}

	private void drawPoints() {
		if(!points.isEmpty()) {
			int size = points.size();

			for(int i=0;i<size;i+=2) {
				TurtleMove p0 = points.get(i);
				TurtleMove p1 = points.get(i+1);
				double r = (double)i/(double)size;
				double b = 1.0 - r;
				gl2.glColor3d(r,0,b);
				gl2.glVertex2d(p0.x, p0.y);
				gl2.glVertex2d(p1.x, p1.y);
			}
			points.clear();
		}
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		drawPoints();
		if(showPenUp) {		
			gl2.glColor3d(
					colorTravel.getRed() / 255.0,
					colorTravel.getGreen() / 255.0,
					colorTravel.getBlue() / 255.0);
			gl2.glVertex2d(p0.x, p0.y);
			gl2.glVertex2d(p1.x, p1.y);
		}
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
        return Translator.get("DirectionLoopTurtleRenderer.name");
    }

	/**
	 * Reset any internal state to defaults.  This makes sure rendering optimizations cleaned
	 * up when the turtle is changed.
	 */
	@Override
	public void reset() {}
}
