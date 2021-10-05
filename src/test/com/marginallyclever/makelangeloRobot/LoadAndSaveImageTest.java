package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.*;

import org.junit.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverterFactory;

public class LoadAndSaveImageTest {

	@Test
	public void testNoMissingPanels() {
		Log.message("testNoMissingPanels() begin.");
		try {
			Translator.start();
			for( ImageConverter c : ImageConverterFactory.converters ) {
				Log.message("Creating panel for "+c.getName());
				c.getPanel();
			}
		} catch(Exception e) {
			fail("Missing panel! "+e.getLocalizedMessage());
		}

		Log.message("testNoMissingPanels() complete.");
	}

}
