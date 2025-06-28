package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.Select;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;

/**
 * Generators create gcode from user input.  Fractals might be one example.
 * @author dan royer
 */
public abstract class TurtleGenerator {
	protected Paper myPaper;
	protected Turtle myTurtle;

	private final List<Select> panelElements = new ArrayList<>();
	
	abstract public String getName();
	
	/**
	 * generate
	 */
	abstract public void generate();

	public void setPaper(Paper paper) {
		myPaper = paper;
	}

	public void add(Select element) {
		panelElements.add(element);
	}

	public List<Select> getPanelElements() {
		return panelElements;
	}

	/**
	 * live preview as the system is generating.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL3 gl2) {}
	
	// OBSERVER PATTERN

	private final EventListenerList listeners = new EventListenerList();

	public void addListener(TurtleGeneratorListener a) {
		listeners.add(TurtleGeneratorListener.class, a);
	}
	
	public void removeListener(TurtleGeneratorListener a) {
		listeners.remove(TurtleGeneratorListener.class, a);
	}
	
	protected void notifyListeners(Turtle turtle) {
		for( TurtleGeneratorListener a : listeners.getListeners(TurtleGeneratorListener.class) ) {
			a.turtleReady(turtle);
		}
	}

	public void setTurtle(Turtle turtle) {
		myTurtle = turtle;
	}
}
