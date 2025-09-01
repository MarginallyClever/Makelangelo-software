package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
import java.awt.*;

/**
 * <p>{@link TurtleRenderFacade} for rendering a {@link com.marginallyclever.makelangelo.turtle.Turtle} using a
 * {@link TurtleRenderer}.</p>
 * <p>The Facade helps to manage the complexity of the rendering process and provides a clean and simple interface
 * for the client code to interact with.</p>
 * @author Dan Royer
 */
public class TurtleRenderFacade implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(TurtleRenderFacade.class);

	private TurtleRenderer myRenderer = TurtleRenderFactory.getTurtleRenderer(TurtleRenderFactory.DEFAULT);
	private Turtle myTurtle = new Turtle();
	private int first=0;
	private int last;
	private Color penDownColor = Color.BLACK;
	private Color penUpColor = Color.GREEN;
	private double penDiameter = 0.8;
	private boolean showTravel;
	private final Graphics2DGL g2gl = new Graphics2DGL();
	private int turtleHash = 0;

	@Override
	public void render(@Nonnull ShaderProgram shader, GL3 gl) {
		if(turtleHash != myTurtle.hashCode()) {
			turtleHash = myTurtle.hashCode();
			g2gl.renderBegin(gl);
			render(g2gl);
		}

		g2gl.renderFinish();
	}

	@Override
	public void dispose() {
		PreviewListener.super.dispose();
		g2gl.dispose();
		turtleHash = -1;  // force a re-render next time
	}

	public void render(@Nonnull Graphics2D g2d) {
		if(myTurtle.isLocked()) return;
		myTurtle.lock();
		try {
			renderLockedTurtle(g2d);
		}
		finally {
			myTurtle.unlock();
		}
	}

	private void renderLockedTurtle(Graphics2D g2d) {
		// where we're at in the drawing (to check if we're between first & last)
		int showCount = 0;
		myRenderer.setPenUpColor(penUpColor);
		myRenderer.setPenDownColor(penDownColor);
		myRenderer.setShowTravel(showTravel);
		myRenderer.start(g2d);
		try {
			Point2d prev = new Point2d(0, 0);

			for (var layer : myTurtle.getLayers()) {
				if (layer.isEmpty() || !layer.isVisible()) continue;
				myRenderer.setPenDownColor(layer.getColor());
				myRenderer.setPenDiameter(layer.getDiameter());
				for (var line : layer.getAllLines()) {
					if (line.isEmpty()) continue;
					boolean start = true;
					for (var next : line.getAllPoints()) {
						if (showCount >= first && showCount < last) {
							if (start) {
								myRenderer.travel(prev, next);
								start = false;
							} else {
								myRenderer.draw(prev, next);
							}
						}
						prev = next;
						showCount++;
					}
				}
			}
		}
		catch(Exception e) {
			logger.error("Failed to render the turtle", e);
		}
		finally {
			myRenderer.end();
		}
	}

	public Turtle getTurtle() {
		return myTurtle;
	}

	public void setTurtle(Turtle turtle) {
		int size=0;
		if(turtle!=null) size = turtle.countPoints();
		myTurtle = turtle;
		if(myRenderer!=null) {
			myRenderer.reset();
		}

		setFirst(0);
		setLast(size);
	}

	public void setRenderer(@Nonnull TurtleRenderer render) {
		myRenderer = render;
	}

	public @Nonnull TurtleRenderer getRenderer() {
		return myRenderer;
	}

	private int getMax() {
		return (myTurtle==null) ? 0 : myTurtle.countPoints();
	}

	public void setFirst(int arg0) {
		first = Math.min(Math.max(arg0, 0), getMax());
		if(last<first) setLast(first);
		turtleHash = 0;
	}
	
	public int getFirst() {
		return first;
	}
	
	public void setLast(int arg0) {
		last = Math.min(Math.max(arg0, 0), getMax());
		if(first>last) setFirst(last);
		turtleHash = 0;
	}

	public int getLast() {
		return last;
	}

	public void setDownColor(Color penDownColor) {
		if(this.penDownColor == penDownColor) return; // no change
		this.penDownColor = penDownColor;
		turtleHash = 0;
	}

	public void setUpColor(Color penUpColor) {
		if(this.penUpColor == penUpColor) return; // no change
		this.penUpColor = penUpColor;
		turtleHash = 0;
	}

	public void setPenDiameter(double penDiameter) {
		if(this.penDiameter == penDiameter) return; // no change
		this.penDiameter = penDiameter;
		turtleHash = 0;
	}

	public void setShowTravel(boolean showTravel) {
		if(this.showTravel == showTravel) return; // no change
		this.showTravel = showTravel;
		turtleHash = 0;
	}
}
