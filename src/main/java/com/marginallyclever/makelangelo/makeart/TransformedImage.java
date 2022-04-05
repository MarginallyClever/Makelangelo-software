package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.makeart.imageFilter.ImageFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * TransformedImage is a BufferedImage, scaled, rotated, and translated
 * somewhere on the drawing area (aka paper space). All sampling interactions
 * with TransformedImage are done in paper space coordinates, and
 * TransformedImage takes care of the rest.
 * 
 * @author droyer
 *
 */
public class TransformedImage {
	private BufferedImage sourceImage;
	private float scaleX, scaleY;
	private float translateX, translateY;
	private float rotationDegrees;
	private int colorChannel;

	public TransformedImage(BufferedImage src) {
		sourceImage = src;
		translateX = -src.getWidth() / 2.0f;
		translateY = -src.getHeight() / 2.0f;
		scaleX = 1;
		scaleY = -1;
		rotationDegrees = 0;
		colorChannel = 0;
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
		rotationDegrees = copy.rotationDegrees;
		colorChannel = copy.colorChannel;
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
		rotationDegrees = other.rotationDegrees;
		colorChannel = other.colorChannel;
	}

	public float getRotationDegrees() {
		return rotationDegrees;
	}

	public float getScaleX() {
		return scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public float getTranslateX() {
		return translateX;
	}

	public float getTranslateY() {
		return translateY;
	}
	
	public BufferedImage getSourceImage() {
		return sourceImage;
	}

	public int getTransformedX(double x) {
		return (int) ((x / scaleX) - translateX);
	}

	public int getTransformedY(double y) {
		return (int) ((y / scaleY) - translateY);
	}
	
	public void rotateAbsolute(float degrees) {
		rotationDegrees = degrees;
	}

	public void rotateRelative(float degrees) {
		rotationDegrees += degrees;
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
		float sampleValue = 0;
		float weightedSum = 0;

		int left   = (int)Math.floor(x0);
		int right  = (int)Math.ceil (x1);
		int bottom = (int)Math.floor(y0);
		int top    = (int)Math.ceil (y1);

		// calculate the weight matrix
		int w = right-left;
		int h = top-bottom;
		if(w<=1 && h<=1) {
			if (canSampleAt(left, bottom)) {
				return sample1x1Unchecked(left, bottom);
			} else {
				return 0;
			}
		}
		
		double [] m = new double[w*h];
		for(int i=0;i<m.length;++i) {
			m[i]=1;
		}
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

		Color c = new Color(sourceImage.getRGB(sampleX, sampleY));
		int colorToBlend=0;
		
		switch (colorChannel) {
		case 1: colorToBlend=c.getRed();  break;
		case 2: colorToBlend=c.getGreen();  break;
		case 3: colorToBlend=c.getBlue();  break;
		default: return ImageFilter.decodeColor(c);
		}
		
		double a = 255.0-c.getAlpha();
		int c2 = (int)(  (255.0 - (double)colorToBlend) * (a / 255.0) + (double)colorToBlend);
		if(c2>255) c2=255;
		else if(c2<0) c2=0;
		return c2;
	}

	public int sample3x3(double x, double y) {
		return sample(x,y,1);
	}

	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	public int sampleArea(int x0, int y0, int x1, int y1) {
		// point sampling
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

	public void setColorChannel(int channel) {
		colorChannel = channel;
	}

	public void setScale(float x,float y) {
		scaleX = x;
		scaleY = y;
	}
	
	@Deprecated
	public void translateX(float x) {
		translateX = x;
	}

	@Deprecated
	public void translateY(float y) {
		translateY += y;
	}
}
