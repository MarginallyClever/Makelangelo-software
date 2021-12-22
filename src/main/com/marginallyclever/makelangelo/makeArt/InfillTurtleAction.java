package com.marginallyclever.makelangelo.makeArt;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class InfillTurtleAction extends AbstractAction {
	private static final long serialVersionUID = -8653065260609614796L;

	private Makelangelo myMakelangelo;
	
	public InfillTurtleAction(Makelangelo m) {
		super(Translator.get("InfillTurtleAction.title"));
		myMakelangelo = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		InfillTurtle infill = new InfillTurtle();
		Turtle t = myMakelangelo.getTurtle();
		try {
			t.add(infill.run(t));
		} catch (Exception e1) {
			Log.error(e1.getMessage());
		}
	}

}
