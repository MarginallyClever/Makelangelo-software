package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.makeart.imagefilter.ImageFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * TransformedImage is a {@link BufferedImage}, with a transformation matrix on top.
 * All sampling interactions are done in paper space coordinates and {@link TransformedImage} takes care of the rest.
 * The original {@link BufferedImage} is not modified so there is no data loss.  This also means one matrix transform
 * per pixel sample, which is slow.
 * @author Dan Royer
 */
public class TransformedImage {
	private final BufferedImage sourceImage;
	private float scaleX, scaleY;
	private float translateX, translateY;
	private final int componentCount;
	private final double [] pixel;
	private final WritableRaster raster;

	public TransformedImage(BufferedImage src) {
		sourceImage = deepCopy(src);
		translateX = -src.getWidth() / 2.0f;
		translateY = -src.getHeight() / 2.0f;
		scaleX = 1;
		scaleY = 1;

		componentCount = sourceImage.getColorModel().getNumComponents();
		pixel = new double[componentCount];
		raster = sourceImage.getRaster();
	}

	public TransformedImage(TransformedImage copy) {
		sourceImage = deepCopy(copy.sourceImage);
		translateX = copy.translateX;
		translateY = copy.translateY;
		scaleX = copy.scaleX;
		scaleY = copy.scaleY;

		componentCount = sourceImage.getColorModel().getNumComponents();
		pixel = new double[componentCount];
		raster = sourceImage.getRaster();
	}

	// https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
	protected BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	/**
	 * Can the image be sampled at this location?
	 * @param x pre-transform x
	 * @param y pre-transform y
	 * @return true if the image can be sampled at this location
	 */
	public boolean canSampleAt(double x, double y) {
		double sampleX = getTransformedX(x);
		double sampleY = getTransformedY(y);

		if (sampleX < 0 || sampleX >= sourceImage.getWidth ()) return false;
		if (sampleY < 0 || sampleY >= sourceImage.getHeight()) return false;
		return true;
	}

	public float getScaleX() {
		return scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public BufferedImage getSourceImage() {
		return sourceImage;
	}

	private double getTransformedX(double x) {
		return ((x / scaleX) - translateX);
	}

	public double getTransformedY(double y) {
		return ((y / scaleY) - translateY);
	}

	class Box {
		public double left, top, right, bottom;

		public Box(double left, double top, double right, double bottom) {
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}

		public void transform() {
			// transform the corners
			left   = getTransformedX(left);
			bottom = getTransformedY(bottom);
			right  = getTransformedX(right);
			top    = getTransformedY(top);
			// make sure left <= right
			if(left > right) {
				double temp = left;
				left = right;
				right = temp;
			}
			// make sure bottom <= top
			if(bottom > top) {
				double temp = bottom;
				bottom = top;
				top = temp;
			}
			// find the bounds of the image once, instead of inside the loops.
			bottom = Math.max(Math.min(bottom,sourceImage.getHeight()),0);
			top    = Math.max(Math.min(top   ,sourceImage.getHeight()),0);
			left   = Math.max(Math.min(left  ,sourceImage.getWidth()),0);
			right  = Math.max(Math.min(right ,sourceImage.getWidth()),0);
		}
	}

	/**
	 * Returns the greyscale intensity [0...255]
	 * @param cx center of the sample area
	 * @param cy center of the sample area
	 * @param radius radius of the sample area
	 * @return the greyscale intensity [0...255]
	 */
	public int sample(double cx, double cy, double radius) {
		return sample(new Box(cx-radius,cy-radius,cx+radius,cy+radius));
	}

	/**
	 * Sample the image, taking into account fractions of pixels.
	 * @param box the area to sample
	 * @return greyscale intensity in this region. [0...255]
	 */
	private int sample(Box box) {
		box.transform();

		// now sample the entire area to average the intensity
		int bBottom = (int)box.bottom;
		int bTop = (int)box.top;
		int bLeft = (int)box.left;
		int bRight = (int)box.right;
		double count = 0;
		double sum = 0;

		for(int y = bBottom; y < bTop; ++y) {
			// sample whole pixels
			for(int x = bLeft; x < bRight; ++x) {
				raster.getPixel(x, y, pixel);
				sum += averageIntensity(pixel);
				count++;
			}
		}

		if(count==0) return 255;
		// average the intensity
		double result = sum / count;
		return (int)Math.min( Math.max(result, 0), 255 );
	}

	// average intensity of the pixel
	private double averageIntensity(double[] pixel) {
		double sum = 0;
		for(int i=0;i<componentCount;++i) {
			sum += pixel[i];
		}
		return sum / componentCount;
	}

	/**
	 * @param cx center of the sample area
	 * @param cy center of the sample area
	 * @param radius radius of the sample area
	 * @return the average color in this region.
	 */
	public Color sampleColor(double cx, double cy, double radius) {
		return sampleColor(new Box(cx-radius,cy-radius,cx+radius,cy+radius));
	}

	/**
	 * Sample the image, taking into account fractions of pixels.
	 * @param box the area to sample
	 * @return the average color in this region.
	 */
	private Color sampleColor(Box box) {
		box.transform();

		// now sample the entire area to average the intensity
		int count = 0;
		var sum = new double[componentCount];
		for(int y = (int)box.bottom; y <= box.top; ++y) {
			for(int x = (int)box.left; x <= box.right; ++x) {
				raster.getPixel(x, y, pixel);
				for(int i=0;i<componentCount;++i) {
					sum[i] += pixel[i];
				}
				count++;
			}
		}
		if(count==0) return Color.WHITE;

		// average the intensity
		for(int i=0;i<componentCount;++i) {
			int j = (int)(sum[i]/count);
			sum[i] = Math.min(Math.max(j, 0), 255);
		}
		return new Color(
				(int)sum[0], // red
				(int)sum[1], // green
				(int)sum[2], // blue
				componentCount > 3 ? (int)sum[3] : 255 // alpha
		);
	}

	/**
	 * Attempt to sample a pixel of the source image, if the (x,y) coordinate is within the bounds of the 
	 * @param x paper-space coordinates of the image
	 * @param y paper-space coordinates of the image
	 * @return 255 if the image cannot be sampled.  The intensity of the color channel [0...255]
	 */
	public int sample1x1(double x, double y) {
		if (canSampleAt(x, y)) {
			return sample1x1Unchecked(x, y);
		}
		return 255;
	}

	/**
	 * Transforms x,y to the local space and samples the source image without a safety check.
	 * @param x paper-space coordinates of the image
	 * @param y paper-space coordinates of the image
	 * @return 255 if the image cannot be sampled.  The intensity of the color channel [0...255].  the color channel is selected with
	 */
	public int sample1x1Unchecked(double x, double y) {
		int sampleX = (int)getTransformedX(x);
		int sampleY = (int)getTransformedY(y);

		int c2 = sourceImage.getRGB(sampleX, sampleY);
		return ImageFilter.decode32bit(c2) & 0xFF;
	}

	public void setScale(float x,float y) {
		scaleX = x;
		scaleY = y;
	}

	public void setTranslation(float x,float y) {
		translateX = x;
		translateY = y;
	}

	public void setRGB(float x, float y, int c) {
		sourceImage.setRGB((int)getTransformedX(x), (int)getTransformedY(y), c);
	}
}
