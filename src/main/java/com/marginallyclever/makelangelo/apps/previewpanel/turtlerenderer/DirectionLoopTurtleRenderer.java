package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;
import java.util.ArrayList;

/**
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} such that each time the pen is lowered the color begins
 * with red and fades to blue as the pen is lifted.  This illustrates direction and length of each line group.
 * @author Dan Royer
 * @since 7.48.0
 */
public class DirectionLoopTurtleRenderer implements TurtleRenderer {
	private GL3 gl;
	private Color colorTravel = Color.GREEN;
	private final float[] lineWidthBuf = new float[1];
	private boolean showPenUp = false;
	private float penDiameter = 1;
	private final ArrayList<TurtleMove> points = new ArrayList<>();

	private final Mesh mesh = new Mesh();
	private boolean isDone = false;
		
	@Override
	public void start(GL3 gl) {
		this.gl = gl;
		points.clear();
		showPenUp = GFXPreferences.getShowPenUp();
		mesh.setRenderStyle(GL3.GL_LINES);
	}

	@Override
	public void end() {
		drawPoints();
		isDone=true;

		mesh.render(gl);
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		points.add(p0);
		points.add(p1);
	}

	private void drawPoints() {
		if(isDone) return;

		if(!points.isEmpty()) {
			int size = points.size();

			for(int i=0;i<size;i+=2) {
				TurtleMove p0 = points.get(i);
				TurtleMove p1 = points.get(i+1);
				float r = (float)i/(float)size;
				float b = 1.0f - r;
				mesh.addColor(r,0,b,1);
				mesh.addVertex((float)p0.x, (float)p0.y,0);
				mesh.addColor(r,0,b,1);
				mesh.addVertex((float)p1.x, (float)p1.y,0);
			}
			points.clear();
		}
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;

		drawPoints();

		if(!showPenUp) return;

		float r = colorTravel.getRed() / 255.0f;
		float g = colorTravel.getGreen() / 255.0f;
		float b = colorTravel.getBlue() / 255.0f;
		float a = colorTravel.getAlpha() / 255.0f;

		mesh.addColor(r,g,b,a);  mesh.addVertex((float)p0.x, (float)p0.y,0);
		mesh.addColor(r,g,b,a);  mesh.addVertex((float)p1.x, (float)p1.y,0);
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
	public void reset() {
		mesh.clear();
		isDone=false;
	}
}
