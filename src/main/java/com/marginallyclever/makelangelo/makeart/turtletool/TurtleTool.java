package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.makeart.TurtleModifierListener;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;

/**
 * {@link TurtleTool} is the base class for all Actions which modify a {@link Turtle}.
 * Some examples might be scale, flip, rotate, reorder, etc.
 * @author Dan Royer
 * @since 7.31.0
 */
public abstract class TurtleTool extends AbstractAction {
	private final EventListenerList listeners = new EventListenerList();
	private MainFrame frame;
	
	public TurtleTool(String label) {
		super(label);
	}
	
	public TurtleTool(String label, Icon icon) {
		super(label,icon);
	}

	public void setSource(MainFrame frame) {
		this.frame = frame;
	}
	
	public void addModifierListener(TurtleModifierListener arg0) {
		listeners.add(TurtleModifierListener.class,arg0);
	}
	
	public void removeModifierListener(TurtleModifierListener arg0) {
		listeners.remove(TurtleModifierListener.class,arg0);
	}
	
	protected void fireModificationEvent(Turtle turtle) {
		for(TurtleModifierListener a : listeners.getListeners(TurtleModifierListener.class)) {
			a.turtleModifiedEvent(turtle);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		fireModificationEvent( run( frame.getTurtle() ) );
	}

	/**
	 * Execute the modification action.  Do not modify the original {@link Turtle}
	 * @param turtle the source material to modify.
	 * @return the results of the modification action.
	 */
	public abstract Turtle run(Turtle turtle); 
}
