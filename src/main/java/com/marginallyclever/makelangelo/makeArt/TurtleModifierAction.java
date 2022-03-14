package com.marginallyclever.makelangelo.makeArt;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * {@link TurtleModifierAction} is the base class for all Actions which modify a {@link Turtle}.
 * Some examples might be scale, flip, rotate, reorder, etc.
 * @author Dan Royer
 * @since 7.31.0
 */
@SuppressWarnings("serial") // Same-version serialization only
public abstract class TurtleModifierAction extends AbstractAction {
	private ArrayList<TurtleModifierListener> listeners = new ArrayList<TurtleModifierListener>();
	private Makelangelo myMakelangelo;
	
	public TurtleModifierAction() {}
	
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
		listeners.add(arg0);
	}
	
	public void removeModifierListener(TurtleModifierListener arg0) {
		listeners.remove(arg0);
	}
	
	protected void fireModificationEvent(Turtle turtle) {
		for(TurtleModifierListener a : listeners) {
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
