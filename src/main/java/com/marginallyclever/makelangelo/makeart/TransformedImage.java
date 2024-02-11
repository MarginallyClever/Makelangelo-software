package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.makeart.imagefilter.ImageFilter;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

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

	public TransformedImage(BufferedImage src) {
		sourceImage = src;
		translateX = -src.getWidth() / 2.0f;
		translateY = -src.getHeight() / 2.0f;
		scaleX = 1;
		scaleY = -1;
	}

	// https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
	protected BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
  
	public TransformedImage(TransformedImage copy) {
		sourceImage = deepCopy(copy.sourceImage);
		translateX = copy.translateX;
		translateY = copy.translateY;
		scaleX = copy.scaleX;
		scaleY = copy.scaleY;
	}

	public boolean canSampleAt(double x, double y) {
		int sampleX = getTransformedX(x);
		int sampleY = getTransformedY(y);

		if (sampleX < 0 || sampleX >= sourceImage.getWidth ()) return false;
		if (sampleY < 0 || sampleY >= sourceImage.getHeight()) return false;
		return true;
	}

	public void copySettingsFrom(TransformedImage other) {
		scaleX = other.scaleX;
		scaleY = other.scaleY;
		translateX = other.translateX;
		translateY = other.translateY;
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

	private int getTransformedX(double x) {
		return (int) ((x / scaleX) - translateX);
	}

	public int getTransformedY(double y) {
		return (int) ((y / scaleY) - translateY);
	}

	/**
	 * Returns the greyscale intensity [0...255]
	 * @param cx center of the sample area
	 * @param cy center of the sample area
	 * @param radius radius of the sample area
	 * @return the greyscale intensity [0...255]
	 */
	public int sample(double cx, double cy, double radius) {
		return sample(cx-radius,cy-radius,cx+radius,cy+radius);
	}
	
	/**
	 * Sample the image, taking into account fractions of pixels. left must be less than right, bottom must be less than top.
	 * @param x0 left
	 * @param y0 top
	 * @param x1 right
	 * @param y1 bottom
	 * @return greyscale intensity in this region. [0...255]v
	 */
	public int sample(double x0, double y0, double x1, double y1) {
		double sampleValue = 0;
		double weightedSum = 0;

		int left   = (int)Math.floor(x0);
		int right  = (int)Math.ceil (x1);
		int bottom = (int)Math.floor(y0);
		int top    = (int)Math.ceil (y1);

		// calculate the weight matrix
		int w = Math.max(1,right-left);
		int h = Math.max(1,top-bottom);
		if(w==1 && h==1) {
			if (canSampleAt(left, bottom)) {
				return sample1x1Unchecked(left, bottom);
			} else {
				return 0;
			}
		}
		
		double [] m = new double[w*h];
		Arrays.fill(m, 1);

		// bottom edge
		if(bottom<y0) {
			double yWeightStart = y0-bottom;
			for(int i=0;i<w;++i) {
				m[i]*=yWeightStart;
			}
		}
		// top edge
		if(top>y1) {
			double yWeightEnd = top-y1;
			for(int i=0;i<w;++i) {
				m[m.length-w+i]*=yWeightEnd;
			}
		}
		// left edge
		if(left<x0) {
			double xWeightStart = x0-left;
			for(int i=0;i<h;++i) {
				m[i*w]*=xWeightStart;
			}
		}
		// right edge
		if(right>x1) {
			double xWeightEnd = right-x1;
			for(int i=0;i<h;++i) {
				m[(i+1)*w-1]*=xWeightEnd;
			}
		}
		
		int i=0;
		for(int y=bottom;y<top;++y) {
			for(int x=left;x<right;++x) {
				double s = m[i++];
				if (canSampleAt(x, y)) {
					sampleValue += sample1x1Unchecked(x,y) * s;
					weightedSum += s;
				}
			}
		}

		if (weightedSum == 0)
			return 255;

		double result = sampleValue / weightedSum;
		
		return (int)Math.min( Math.max(result, 0), 255 );
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
	 * Sample a pixel of the source image without a safety check.
	 * @param x paper-space coordinates of the image
	 * @param y paper-space coordinates of the image
	 * @return 255 if the image cannot be sampled.  The intensity of the color channel [0...255].  the color channel is selected with
	 */
	public int sample1x1Unchecked(double x, double y) {
		int sampleX = getTransformedX(x);
		int sampleY = getTransformedY(y);

		int c2 = sourceImage.getRGB(sampleX, sampleY);
		return ImageFilter.decode32bit(c2) & 0xFF;
	}

	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	public int sampleArea(int x0, int y0, int x1, int y1) {
		int value = 0;
		int sum = 0;

		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				if (canSampleAt(x, y)) {
					value += sample1x1Unchecked(x, y);
					++sum;
				}
			}
		}

		if (sum == 0)
			return 255;

		return value / sum;
	}	

	public void setScale(float x,float y) {
		scaleX = x;
		scaleY = y;
	}


	public void setRGB(float x, float y, int c) {
		sourceImage.setRGB(getTransformedX(x), getTransformedY(y), c);
	}
}
