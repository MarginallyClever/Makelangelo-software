package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.editorcontext.EditorContext;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * {@link TurtleTool} is the base class for all Actions which modify a {@link Turtle}.
 * Some examples might be scale, flip, rotate, reorder, etc.
 * @author Dan Royer
 * @since 7.31.0
 */
public abstract class TurtleTool extends AbstractAction {
	private EditorContext context;
	
	public TurtleTool(String label) {
		super(label);
	}
	
	public TurtleTool(String label, Icon icon) {
		super(label,icon);
	}

	public void setContext(EditorContext context) {
		this.context = context;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// run the action (which doesn't modify the turtle) and then set it (which *does* modify the turtle)
		// this two-step avoids re-entrant problems.
		context.mutate(t-> t.set(run(t)));
	}

	/**
	 * <p>Execute the modification action.  Do not modify the original {@link Turtle} while executing the run
	 * as this might cause re-entrant problems.</p>
	 * @param turtle the source material to modify.
	 * @return the results of the modification action.
	 */
	public abstract Turtle run(Turtle turtle); 
}
