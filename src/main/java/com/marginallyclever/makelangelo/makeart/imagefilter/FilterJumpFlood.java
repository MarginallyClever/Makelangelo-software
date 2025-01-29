package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Converts an image using the jump flood algorithm.  On a white surface, black pixels will "spread out" creating
 * an effect like water caustics.
 * @author Dan Royer
 */
public class FilterJumpFlood extends ImageFilter {
	private final List<Point> points = new ArrayList<>();
	private int scale;
	private final TransformedImage img;

	public FilterJumpFlood(TransformedImage img) {
		super();
		this.img = img;
	}

	@Override
	public TransformedImage filter() {
		BufferedImage src = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage dest = after.getSourceImage();
		dest.setData(src.getRaster());

		fillImage(dest);

		return after;
	}

	public BufferedImage fillImage(BufferedImage image) {
		points.clear();

		var h = image.getHeight();
		var w = image.getWidth();

		// Scan the image to find the initial points (black pixels)
		IntStream.range(0, h).parallel().forEach(y -> {
			for (int x = 0; x < w; x++) {
				Color color = new Color(image.getRGB(x, y));
				if (color.equals(Color.BLACK)) {
					points.add(new Point(x, y));
				}
			}
		});

		scale = Math.min(image.getWidth(), image.getHeight()) /2;

		// Run the algorithm
		IntStream.range(0, h).parallel().forEach(y -> {
			for (int x = 0; x < w; x++) {
				updatePixel(image,points,x, y);
			}
		});

		return image;
	}

	private void updatePixel(BufferedImage image,List<Point> points,int x, int y) {
		int minDistance = Integer.MAX_VALUE;
		for (Point point : points) {
			int dx = point.x - x;
			int dy = point.y - y;
			int distance = (int)Math.sqrt(dx * dx + dy * dy);
			minDistance = Math.min(minDistance, distance);
		}

		int grayLevel = (int) (255.0 * minDistance / scale);
		grayLevel = Math.max(Math.min(255, grayLevel), 0);
		image.setRGB(x, y, new Color(grayLevel, grayLevel, grayLevel).getRGB());
	}

	public static void main(String[] args) throws IOException {
		//*
		BufferedImage image = new BufferedImage(400, 500, BufferedImage.TYPE_INT_RGB);
		var g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());

		// some random black pixels
		for(int i=0;i<25;++i) {
			image.setRGB(
					(int)(Math.random()*image.getWidth()),
					(int)(Math.random()*image.getHeight()),
					Color.BLACK.getRGB());
		}
		TransformedImage src = new TransformedImage( image );

		long start = System.currentTimeMillis();
		FilterJumpFlood f = new FilterJumpFlood(src);
		TransformedImage dest = f.filter();
		long end = System.currentTimeMillis();
		System.out.println("FilterJumpFlood took "+(end-start)+"ms");
		ResizableImagePanel.showImage(dest.getSourceImage(), "Filter_JumpFlood" );
	}
}