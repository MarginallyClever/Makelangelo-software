package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Test;

import com.marginallyclever.artPipeline.generators.TurtleGenerator;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;

public class MakelangeloRobotPanelTest {

	@Test
	public void testNoMissingGeneratorPanels() {
		Log.message("testNoMissingGeneratorPanels() begin.");
		try {
			Log.message("Translator.start()...");
			Translator.start();
			Log.message("loading service...");
			System.out.flush();
			ServiceLoader<TurtleGenerator> imageGenerators = ServiceLoader.load(TurtleGenerator.class);
			Log.message("iterating...");
			System.out.flush();
			Iterator<TurtleGenerator> ici = imageGenerators.iterator();
			while(ici.hasNext()) {
				TurtleGenerator c = ici.next();
				Log.message("Creating panel for "+c.getName());
				c.getPanel();
			}
		} catch(Exception e) {
			fail("Missing panel! "+e.getLocalizedMessage());
		}
		Log.message("testNoMissingGeneratorPanels() complete.");
	}
}
