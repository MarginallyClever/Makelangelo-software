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
 * Draw {@link com.marginallyclever.makelangelo.turtle.Turtle} with a new color every time the pen is lowered.
 * This illustrates each "loop" of the drawing.
 * @author Dan Royer
 */
public class SeparateLoopTurtleRenderer implements TurtleRenderer {
	private RenderContext context;
	
	private Color colorTravel = Color.GREEN;
	private boolean showPenUp = false;
	private float penDiameter = 1;
	private int moveCounter;
	private Color colorDraw = Color.WHITE;
	private boolean isPenUp;

	private final Mesh mesh = MeshFactory.createMesh();
	private boolean isDone = false;
		
	@Override
	public void start(RenderContext context) {
		this.context = context;
		showPenUp = GFXPreferences.getShowPenUp();

		mesh.setRenderStyle(GL3.GL_TRIANGLES);
		moveCounter=0;
		isPenUp=true;
		setDrawColor();
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
	
	private void setDrawColor() {
		switch(moveCounter%7) {
			case 0 -> colorDraw = new Color(255,0,0,255);
			case 1 -> colorDraw = new Color(0,127,0,255);
			case 2 -> colorDraw = new Color(0,0,255,255);
			case 3 -> colorDraw = new Color(255,255,0,255);
			case 4 -> colorDraw = new Color(255,0,255,255);
			case 5 -> colorDraw = new Color(0,255,255,255);
			default -> colorDraw = new Color(0,0,0,255);
		}
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		isPenUp = false;
		setDrawColor();
		Line2QuadHelper.thicken(mesh, p0, p1, colorDraw, penDiameter);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(isDone) return;
		if(showPenUp) {
			Line2QuadHelper.thicken(mesh, p0, p1, colorTravel, penDiameter);
		}
		if(!isPenUp) {
			moveCounter++;
		}
		isPenUp = true;
	}

	@Override
	public void setPenDownColor(Color color) {}

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
