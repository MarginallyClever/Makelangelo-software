package com.marginallyclever.makelangelo.turtle.turtleRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleRenderFacade implements PreviewListener {

	private static final Logger logger = LoggerFactory.getLogger(TurtleRenderFacade.class);

	private TurtleRenderer defaultRenderer = TurtleRenderFactory.DEFAULT.getTurtleRenderer();

	//private TurtleRenderer barberPole = new BarberPoleTurtleRenderer();

	//private MakelangeloFirmwareVisualizer viz = new MakelangeloFirmwareVisualizer(); 
	//viz.render(gl2, turtleToRender, settings);

	private TurtleRenderer myRenderer=defaultRenderer;
	private Turtle myTurtle = new Turtle();
	private int first=0;
	private int last;
	
	@Override
	public void render(GL2 gl2) {
		if(myTurtle.isLocked()) return;
		try {
			myTurtle.lock();
			
			TurtleMove previousMove = null;
			
			// where we're at in the drawing (to check if we're between first & last)
			int showCount = 0;
			
			try {
				myRenderer.start(gl2);
				showCount++;

				for (TurtleMove m : myTurtle.history) {
					if(m==null) throw new NullPointerException();
					
					boolean inShow = (showCount >= first && showCount < last);
					switch (m.type) {
					case TurtleMove.TRAVEL:
						if (inShow && previousMove != null) {
							myRenderer.travel(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TurtleMove.DRAW_LINE:
						if (inShow && previousMove != null) {
							myRenderer.draw(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TurtleMove.TOOL_CHANGE:
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
		logger.debug("first={}",first);
		if(last<first) setLast(first);
	}
	
	public int getFirst() {
		return first;
	}
	
	public void setLast(int arg0) {
		int size = 0;
		if(myTurtle!=null) size = myTurtle.history.size();

		last = (int)Math.min(Math.max(arg0, 0), size);
		logger.debug("last={}",last);
		if(first>last) setFirst(last);
	}

	public int getLast() {
		return last;
	}
}
