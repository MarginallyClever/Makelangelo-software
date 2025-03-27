package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight.GenerateClockHands;

/**
 * Draws a border around the paper.  Uses current paper myPaper.
 * @author Dan Royer
 *
 */
public class Generator_AnalogClock extends TurtleGenerator {
	@Override
	public String getName() {
		return Translator.get("Generator_AnalogClock.name");
	}

	@Override
	public void generate() {
		double lesser = Math.min(myPaper.getPaperWidth(),myPaper.getPaperHeight());
		double half = lesser/2;
		var turtle = GenerateClockHands.generateClockHands(0,5,half);
		turtle.translate(-half,-half);
		notifyListeners(turtle);
	}
}
