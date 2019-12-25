package com.marginallyclever.makelangeloRobot;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Test;

import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.makelangelo.Translator;

public class LoadAndSaveImageTest {

	@Test
	public void testNoMissingPanels() {
		System.out.println("testNoMissingPanels() begin.");
		try {
			Translator.start();
			ServiceLoader<ImageConverter> converters = ServiceLoader.load(ImageConverter.class);
			Iterator<ImageConverter> ici = converters.iterator();
			while(ici.hasNext()) {
				ImageConverter c = ici.next();
				System.out.println("Creating panel for "+c.getName());
				c.getPanel();
			}
		} catch(Exception e) {
			fail("Missing panel!");
		}

		System.out.println("testNoMissingPanels() complese.");
	}

}
