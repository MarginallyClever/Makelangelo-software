package com.marginallyclever.makelangelo.makeArt;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class InfillTurtleAction extends AbstractAction {
	private static final Logger logger = LoggerFactory.getLogger(InfillTurtleAction.class);

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
			// PPAC : to indirectly update the slider last max limit (or else max limit is not updated and no view update on infill menu item clic)
			myMakelangelo.setTurtle(t);
		} catch (Exception ex) {
			logger.error("Failed to infill", ex);
		}
	}

}
