package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.makeArt.ImageManipulator;
import com.marginallyclever.makelangeloRobot.PlotterDecorator;

/**
 * Generators create gcode from user input.  Fractals might be one example.
 * @author dan royer
 */
public abstract class TurtleGenerator extends ImageManipulator implements PlotterDecorator {
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
}
