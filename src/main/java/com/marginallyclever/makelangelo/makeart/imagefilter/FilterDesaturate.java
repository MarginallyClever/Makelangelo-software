package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Converts an image to greyscale.
 * @author Dan Royer
 */
public class FilterDesaturate extends ImageFilter {
	private final TransformedImage img;

	public FilterDesaturate(TransformedImage img) {
		super();
		this.img = img;
	}

	@Override
	public TransformedImage filter() {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();

		BufferedImage bi = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();
		var raster = bi.getRaster();
		var afterRaster = afterBI.getRaster();

		var count = bi.getColorModel().getNumComponents();
		// Temporary array to hold pixel components

		IntStream.range(0, h).parallel().forEach(y -> {
			int[] pixel = new int[count];
			for (int x = 0; x < w; ++x) {
				raster.getPixel(x, y, pixel);
				double average = 0;
				for (int i = 0; i < count; ++i) {
					// convert sRGB to linear
					average += pixel[i];
				}
				average /= count;
				int toned = (int)toneControl(average);
                Arrays.fill(pixel, toned);
				afterRaster.setPixel(x,y,pixel);
			}
		});
		return after;
	}

	/**
	 * Convert a single pixel from sRGB to linear.
	 * @param b a number between 0 and 255, inclusive.
	 * @return a number between 0 and 255, inclusive.
	 */
	private double sRGBtoLinear(double b) {
		b /= 255.0;
		if (b <= 0.04045) b /= 12.92;
		else b = Math.pow((b + 0.055) / 1.055, 2.4);
		return b * 255.0;
	}

	/**
	 * Non-linear tone control.  Mostly brightens highlights while leaving midtones and shadows alone.
	 * @param b a number between 0 and 255, inclusive.
	 * @return a number between 0 and 255, inclusive.
 	 */
	private double toneControl(double b) {
		b /= 255.0;
		b = 0.017 * Math.exp(3.29 * b) + 0.005 * Math.exp(7.27 * b);
		return Math.min(1, Math.max(0, b)) * 255.0;
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
		FilterDesaturate f = new FilterDesaturate(src);
		ResizableImagePanel.showImage(f.filter().getSourceImage(), "FilterDesaturate" );
	}
}