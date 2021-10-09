package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Generators create gcode from user input.  Fractals might be one example.
 * @author dan royer
 */
public abstract class TurtleGenerator {
	protected Paper myPaper;
	
	abstract public String getName();
	
	/**
	 * @return true if generate succeeded.
	 */
	abstract public void generate();
	
	/**
	 * @return the gui panel with options for this manipulator
	 */
	public TurtleGeneratorPanel getPanel() {
		return null;
	}
	
	/**
	 * live preview as the system is generating.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2) {}
	
	// OBSERVER PATTERN

	private ArrayList<TurtleGeneratorListener> listeners = new ArrayList<TurtleGeneratorListener>();
	public void addListener(TurtleGeneratorListener a) {
		listeners.add(a);
	}
	
	public void removeListener(TurtleGeneratorListener a) {
		listeners.remove(a);
	}
	
	protected void notifyListeners(Turtle turtle) {
		for( TurtleGeneratorListener a : listeners ) a.turtleReady(turtle);
	}

	public void setPaper(Paper paper) {
		myPaper = paper;
	}
}
