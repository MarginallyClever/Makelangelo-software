package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGeneratorFactory;

public class MakelangeloRobotPanelTest {

	@Test
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
