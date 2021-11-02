package com.marginallyclever.makelangeloRobot;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;

public class MakelangeloRobotPanelTest {

	@Test
	@Disabled("for the CI")
	public void testNoMissingGeneratorPanels() {
		Log.start();
		Log.message("testNoMissingGeneratorPanels() begin.");
		Translator.start();
		try {
			for( TurtleGenerator c : TurtleGeneratorFactory.available ) {
				c.getPanel();
			}
		} catch(Exception e) {
			fail("Missing panel! "+e.getLocalizedMessage());
		}
		Log.message("testNoMissingGeneratorPanels() complete.");
	}
}
