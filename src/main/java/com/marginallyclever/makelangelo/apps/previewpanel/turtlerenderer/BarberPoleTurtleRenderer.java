package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.MeshFactory;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;
import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;

/**
 * Draws Turtle in red/blue sequence to show line segments.
 * @author Dan Royer
 *
 */
public class BarberPoleTurtleRenderer implements TurtleRenderer {
	private RenderContext context;
	private Color colorTravel = Color.GREEN;
	private boolean showPenUp = false;
	private float penDiameter = 1;
	private int moveCounter;

	private final Mesh mesh = MeshFactory.createMesh();
	private boolean isDone=false;
	private Color colorDraw;
		
	@Override
	public void start(RenderContext context) {
		this.context = context;
		showPenUp = GFXPreferences.getShowPenUp();

		mesh.setRenderStyle(GL3.GL_TRIANGLES);
		moveCounter = 0;
		if(!isDone) {
			mesh.unload(context.gl);
			mesh.clear();
		}
	}

	@Override
	public void end() {
		isDone=true;
		mesh.render(context.gl);
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		colorDraw = ((moveCounter%2) == 0)
				? new Color(255,0,0,255)
				: new Color(0,0,255,255);
		Line2QuadHelper.thicken(mesh, p0, p1, colorDraw, penDiameter);
		moveCounter++;
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		if(!showPenUp) return;
		Line2QuadHelper.thicken(mesh, p0, p1, colorTravel, penDiameter);
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
        return Translator.get("BarberPoleTurtleRenderer.name");
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
