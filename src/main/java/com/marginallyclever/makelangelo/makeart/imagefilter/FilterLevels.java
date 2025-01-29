package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Converts an image to N levels of grey
 * @author Dan Royer
 */
public class FilterLevels extends ImageFilter {
	private static final Logger logger = LoggerFactory.getLogger(FilterLevels.class);
	private final TransformedImage img;
	private final double levels;

	public FilterLevels(TransformedImage img, int levels) {
		super();
		this.img = img;
		this.levels = levels;
	}

	@Override
	public TransformedImage filter() {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();

		double max_intensity = -1000;
		double min_intensity = 1000;

		BufferedImage bi = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		double step = 255.0 / (levels - 1); // Step size for quantization

		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				double pixel = decode32bit(bi.getRGB(x, y));
				double d = (int)Math.round((pixel * (levels-1)) / 255.0);
				double c = (int) Math.round(d * step);
				int b = (int) Math.max(Math.min(c, 255), 0);
				afterBI.setRGB(x, y, ImageFilter.encode32bit(b));
			}
		}

		return after;
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
		FilterLevels f = new FilterLevels(src,8);
		ResizableImagePanel.showImage(f.filter().getSourceImage(), "Filter_Greyscale" );
	}
}