package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Test;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodePanel;

public class MakelangeloRobotPanelTest {

	@Test
	public void testNoMissingGeneratorPanels() {
		Log.message("testNoMissingGeneratorPanels() begin.");
		try {
			Log.message("Translator.start()...");
			Translator.start();
			Log.message("loading service...");
			System.out.flush();
			ServiceLoader<Node> imageGenerators = ServiceLoader.load(Node.class);
			Log.message("iterating...");
			System.out.flush();
			Iterator<Node> ici = imageGenerators.iterator();
			while(ici.hasNext()) {
				Node c = ici.next();
				Log.message("Creating panel for "+c.getName());
				new NodePanel(c);
			}
		} catch(Exception e) {
			fail("Missing panel! "+e.getLocalizedMessage());
		}
		Log.message("testNoMissingGeneratorPanels() complete.");
	}
}
