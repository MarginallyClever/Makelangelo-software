package com.marginallyclever.makelangelo.turtle;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.preview.PreviewListener;

public class TurtleRenderFacade implements PreviewListener {
	private TurtleRenderer defaultRenderer = new DefaultTurtleRenderer();

	//private TurtleRenderer barberPole = new BarberPoleTurtleRenderer();

	//private MakelangeloFirmwareVisualizer viz = new MakelangeloFirmwareVisualizer(); 
	//viz.render(gl2, turtleToRender, settings);

	private TurtleRenderer tr=defaultRenderer;
	
	private Turtle myTurtle;
	
	@Override
	public void render(GL2 gl2) {
		if(myTurtle.isLocked()) return;
		try {
			myTurtle.lock();
			
			TurtleMove previousMove = null;
			
			// the first and last command to show (in case we want to isolate part of the drawing)
			int first = 0;
			int last = myTurtle.history.size();
			// where we're at in the drawing (to check if we're between first & last)
			int showCount = 0;
			
			try {
				tr.start(gl2);
				showCount++;

				for (TurtleMove m : myTurtle.history) {
					if(m==null) throw new NullPointerException();
					
					boolean inShow = (showCount >= first && showCount < last);
					switch (m.type) {
					case TurtleMove.TRAVEL:
						if (inShow && previousMove != null) {
							tr.travel(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TurtleMove.DRAW:
						if (inShow && previousMove != null) {
							tr.draw(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TurtleMove.TOOL_CHANGE:
						tr.setPenDownColor(m.getColor());
						break;
					}
				}
			}
			catch(Exception e) {
				//Log.error(e.getMessage());
			}
			finally {
				tr.end();
			}
		}
		catch(Exception e) {
			Log.error(e.getMessage());
		}
		finally {
			if(myTurtle.isLocked()) {
				myTurtle.unlock();
			}
		}
	}
}
