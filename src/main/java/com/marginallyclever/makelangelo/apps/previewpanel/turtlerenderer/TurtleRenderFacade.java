package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.apps.previewpanel.PreviewListener;
import com.marginallyclever.makelangelo.apps.previewpanel.ShaderProgram;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * Facade for rendering a {@link com.marginallyclever.makelangelo.turtle.Turtle} using a {@link TurtleRenderer}.
 * TODO explain reason for facade?
 * @author Dan Royer
 */
public class TurtleRenderFacade implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(TurtleRenderFacade.class);

	private final TurtleRenderer defaultRenderer = TurtleRenderFactory.DEFAULT.getTurtleRenderer();
	private TurtleRenderer myRenderer = defaultRenderer;
	private Turtle myTurtle = new Turtle();
	private int first=0;
	private int last;
	private final Color penDownColor = Color.BLACK;
	private Color penUpColor = Color.GREEN;
	private double penDiameter=0.8;

	@Override
	public void render(GL3 gl, ShaderProgram shaderProgram) {
		if(myTurtle.isLocked()) return;
		myTurtle.lock();
		try {
			TurtleMove previousMove = new TurtleMove(0,0, MovementType.TRAVEL);
			
			// where we're at in the drawing (to check if we're between first & last)
			int showCount = 0;
			
			try {
				myRenderer.setPenDiameter(penDiameter);
				myRenderer.setPenUpColor(penUpColor);
				myRenderer.setPenDownColor(penDownColor);
				myRenderer.setPenDiameter(penDiameter);
				myRenderer.start(gl);
				showCount++;

				for (TurtleMove m : myTurtle.history) {
					if(m==null) throw new NullPointerException();
					
					boolean inShow = (showCount >= first && showCount < last);
					switch (m.type) {
					case TRAVEL:
						if (inShow) {
							myRenderer.travel(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case DRAW_LINE:
						if (inShow) {
							myRenderer.draw(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TOOL_CHANGE:
						myRenderer.setPenDownColor(m.getColor());
						myRenderer.setPenDiameter(m.getDiameter());
						break;
					}
				}
			}
			catch(Exception e) {
				//Log.error(e.getMessage());
			}
			finally {
				myRenderer.end();
			}
		}
		catch(Exception e) {
			logger.error("Failed to render the turtle", e);
		}
		finally {
			if(myTurtle.isLocked()) {
				myTurtle.unlock();
			}
		}
	}

	public Turtle getTurtle() {
		return myTurtle;
	}

	public void setTurtle(Turtle turtle) {
		int size=0;
		if(turtle!=null) size = turtle.history.size();
		myTurtle = turtle;
		if(myRenderer!=null) {
			myRenderer.reset();
		}

		setFirst(0);
		setLast(size);
	}

	public void setRenderer(TurtleRenderer render) {
		myRenderer = render;
	}

	public TurtleRenderer getRenderer() {
		return myRenderer;
	}
	
	public void setFirst(int arg0) {
		int size = 0;
		if(myTurtle!=null) size = myTurtle.history.size();

		first=(int)Math.min(Math.max(arg0, 0),size);
		if(last<first) setLast(first);
	}
	
	public int getFirst() {
		return first;
	}
	
	public void setLast(int arg0) {
		int size = 0;
		if(myTurtle!=null) size = myTurtle.history.size();

		last = (int)Math.min(Math.max(arg0, 0), size);
		if(first>last) setFirst(last);
	}

	public int getLast() {
		return last;
	}

	public void setUpColor(Color penUpColor) {
		this.penUpColor=penUpColor;
	}

	public void setPenDiameter(double penDiameter) {
		this.penDiameter = penDiameter;
	}
}
