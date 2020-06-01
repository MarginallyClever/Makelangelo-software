package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Test;

import com.marginallyclever.artPipeline.generators.ImageGenerator;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.log.Log;

public class MakelangeloRobotPanelTest {

	@Test
	public void testNoMissingGeneratorPanels() {
		Log.message("testNoMissingGeneratorPanels() begin.");
		try {
			Log.message("Translator.start()...");
			System.out.flush();
			Translator.start();
			Log.message("loading service...");
			System.out.flush();
			ServiceLoader<ImageGenerator> imageGenerators = ServiceLoader.load(ImageGenerator.class);
			Log.message("iterating...");
			System.out.flush();
			Iterator<ImageGenerator> ici = imageGenerators.iterator();
			while(ici.hasNext()) {
				ImageGenerator c = ici.next();
				Log.message("Creating panel for "+c.getName());
				System.out.flush();
				c.getPanel();
			}
		} catch(Exception e) {
			fail("Missing panel!");
		}
		Log.message("testNoMissingGeneratorPanels() complete.");
	}
}
