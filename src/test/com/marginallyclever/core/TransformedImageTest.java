package com.marginallyclever.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TransformedImageTest {
	@Test
	@Disabled("Not really a test")
	public void ImageIOInformation() {
		System.out.println("Suffixes:");
		for( String s : ImageIO.getReaderFileSuffixes() ) {
			System.out.println("\t"+s);
		}
		System.out.println("Format names:");
		for( String s : ImageIO.getReaderMIMETypes() ) {
			System.out.println("\t"+s);
		}
		System.out.println("MIME types:");
		for( String s : ImageIO.getReaderMIMETypes() ) {
			System.out.println("\t"+s);
		}
		assertEquals(1, 1);
	}
}
