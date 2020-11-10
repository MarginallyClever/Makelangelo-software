package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Test;

import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;

public class LoadAndSaveImageTest {

	@Test
	public void testNoMissingPanels() {
		Log.message("testNoMissingPanels() begin.");
		try {
			Translator.start();
			ServiceLoader<ImageConverter> converters = ServiceLoader.load(ImageConverter.class);
			Iterator<ImageConverter> ici = converters.iterator();
			while(ici.hasNext()) {
				ImageConverter c = ici.next();
				Log.message("Creating panel for "+c.getName());
				c.getPanel();
			}
		} catch(Exception e) {
			fail("Missing panel! "+e.getLocalizedMessage());
		}

		Log.message("testNoMissingPanels() complete.");
	}

}
