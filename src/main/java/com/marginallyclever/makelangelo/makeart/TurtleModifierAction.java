package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;

/**
 * {@link TurtleModifierAction} is the base class for all Actions which modify a {@link Turtle}.
 * Some examples might be scale, flip, rotate, reorder, etc.
 * @author Dan Royer
 * @since 7.31.0
 */
public abstract class TurtleModifierAction extends AbstractAction {
	private final EventListenerList listeners = new EventListenerList();
	private Makelangelo myMakelangelo;
	
	public TurtleModifierAction(String string) {
		super(string);
	}
	
	public TurtleModifierAction(String string,Icon icon) {
		super(string,icon);
	}

	public void setSource(Makelangelo m) {
		myMakelangelo = m;
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
		fireModificationEvent( run( myMakelangelo.getTurtle() ) );
	}

	/**
	 * Execute the modification action.  Do not modify the original {@link Turtle}
	 * @param turtle the source material to modify.
	 * @return the results of the modification action.
	 */
	public abstract Turtle run(Turtle turtle); 
}
