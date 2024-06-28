package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import javax.vecmath.Vector3d;
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
	private boolean showPenUp = false;
	private final ArrayList<TurtleMove> points = new ArrayList<>();
	private float penDiameter = 1;

	private final Mesh mesh = new Mesh();
	private boolean isDone = false;
		
	@Override
	public void start(GL3 gl) {
		this.gl = gl;
		points.clear();
		showPenUp = GFXPreferences.getShowPenUp();
		mesh.setRenderStyle(GL3.GL_TRIANGLES);
		if(!isDone) {
			mesh.unload(gl);
			mesh.clear();
		}
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		points.add(p0);
		points.add(p1);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		drawPoints();
		if(!showPenUp) return;
		Line2QuadHelper.thicken(mesh, p0, p1, colorTravel, penDiameter);
	}

	@Override
	public void end() {
		drawPoints();
		isDone=true;
		mesh.render(gl);
	}

	private void drawPoints() {
		if(isDone) return;
		if(points.isEmpty()) return;

		int size = points.size()-1;
		for(int i=0;i<size;i+=2) {
			TurtleMove p0 = points.get(i  );
			TurtleMove p1 = points.get(i+1);
			int b0 = (int)( 255f * (float)(i  ) / (float)size );
			int b1 = (int)( 255f * (float)(i+1) / (float)size );
			Line2QuadHelper.thicken(mesh,
					new Vector3d(p0.x,p0.y,0),
					new Vector3d(p1.x,p1.y,0),
					new Color(b0,0,255 - b0,255),
					new Color(b1,0,255 - b1,255),
					penDiameter);
		}
		points.clear();
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
