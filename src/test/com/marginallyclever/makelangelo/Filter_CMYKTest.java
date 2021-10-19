package com.marginallyclever.makelangelo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_CMYK;
import com.marginallyclever.util.PreferencesHelper;

public class Filter_CMYKTest {
	@BeforeAll
	public static void beforeAll() {
		Log.start();
		PreferencesHelper.start();
	}
	@AfterAll
	public static void afterAll() {
		Log.end();
	}
	
	//@Test
	public void testConversion() {
		try {
			final String PATH_NAME = "target/classes/bill-murray";
			final String EXT = "jpg";
			File file = new File(PATH_NAME + "." + EXT);
			assert (file.isFile());
			TransformedImage img = new TransformedImage(ImageIO.read(new FileInputStream(file)));
			Filter_CMYK filter = new Filter_CMYK();
			filter.filter(img);

			ImageIO.write(filter.getC().getSourceImage(), EXT, new File(PATH_NAME + "C." + EXT));
			ImageIO.write(filter.getM().getSourceImage(), EXT, new File(PATH_NAME + "M." + EXT));
			ImageIO.write(filter.getY().getSourceImage(), EXT, new File(PATH_NAME + "Y." + EXT));
			ImageIO.write(filter.getK().getSourceImage(), EXT, new File(PATH_NAME + "K." + EXT));
		} catch (IOException e1) {
			e1.printStackTrace();
			assert (false);
		}
	}
}
