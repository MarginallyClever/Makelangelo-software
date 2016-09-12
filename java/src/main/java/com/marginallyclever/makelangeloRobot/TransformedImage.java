package com.marginallyclever.makelangeloRobot;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.marginallyclever.makelangeloRobot.imageFilters.ImageFilter;

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

	public boolean canSampleAt(float x, float y) {
		int sampleX = getTransformedX(x);
		int sampleY = getTransformedY(y);

		if (sampleX < 0)
			return false;
		if (sampleX >= sourceImage.getWidth())
			return false;
		if (sampleY < 0)
			return false;
		if (sampleY >= sourceImage.getHeight())
			return false;
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

	public int getTransformedX(float x) {
		return (int) ((x / scaleX) - translateX);
	}

	public int getTransformedY(float y) {
		return (int) ((y / scaleY) - translateY);
	}
	
	public void rotateAbsolute(float degrees) {
		rotationDegrees = degrees;
	}

	public void rotateRelative(float degrees) {
		rotationDegrees += degrees;
	}

	/**
	 * sample the image, taking into account fractions of pixels.
	 *
	 * @param x0 top left corner
	 * @param y0 top left corner
	 * @param x1 bottom right corner
	 * @param y1 bottom right corner
	 * @return greyscale intensity in this region. range 0...255 inclusive
	 */
	public int sample(double x0, double y0, double x1, double y1) {
		float sampleValue = 0;
		float sampleSum = 0;

		double xceil = Math.ceil(x0);
		double xweightstart = (x0 != xceil) ? xceil - x0 : 1;

		double xfloor = Math.floor(x1);
		double xweightend = (x1 != xceil) ? xfloor - x1 : 0;

		int left = (int) Math.floor(x0);
		int right = (int) Math.ceil(x1);
		double s;

		// top edge
		double yceil = Math.ceil(y0);
		if (y0 != yceil) {
			double yweightstart = yceil - y0;

			// left edge
			if (canSampleAt((float) x0, (float) y0)) {
				s = xweightstart * yweightstart;
				sampleValue += sample1x1Unchecked((float) x0, (float) y0) * s;
				sampleSum += s;
			}

			for (int i = left; i < right; ++i) {
				if (canSampleAt(i, (float) y0)) {
					sampleValue += sample1x1Unchecked(i, (float) y0) * yweightstart;
					sampleSum += yweightstart;
				}
			}
			// right edge
			if (canSampleAt(right, (float) y0)) {
				s = xweightend * yweightstart;
				sampleValue += sample1x1Unchecked(right, (float) y0) * s;
				sampleSum += s;
			}
		}

		int bottom = (int) Math.floor(y0);
		int top = (int) Math.ceil(y1);
		for (int j = bottom; j < top; ++j) {
			// left edge
			if (canSampleAt((float) x0, j)) {
				sampleValue += sample1x1Unchecked((int) x0, j) * xweightstart;
				sampleSum += xweightstart;
			}

			for (int i = left; i < right; ++i) {
				if (canSampleAt(i, j)) {
					sampleValue += sample1x1Unchecked(i, j);
					sampleSum += 1;
				}
			}
			// right edge
			if (canSampleAt(right, j)) {
				sampleValue += sample1x1Unchecked(right, j) * xweightend;
				sampleSum += xweightend;
			}
		}

		// bottom edge
		double yfloor = Math.floor(y1);
		if (y1 != yfloor) {
			double yweightend = yfloor - y1;

			// left edge
			if (canSampleAt((float) x0, (float) y1)) {
				s = xweightstart * yweightend;
				sampleValue += sample1x1Unchecked((float) x0, (float) y1) * s;
				sampleSum += s;
			}

			for (int i = left; i < right; ++i) {
				if (canSampleAt(i, (float) y1)) {
					sampleValue += sample1x1Unchecked((float) i, (float) y1) * yweightend;
					sampleSum += yweightend;
				}

			}
			// right edge
			if (canSampleAt(right, (float) y1)) {
				s = xweightend * yweightend;
				sampleValue += sample1x1Unchecked(right, (float) y1) * s;
				sampleSum += s;
			}
		}

		if (sampleSum == 0)
			return 255;

		return (int) (sampleValue / sampleSum);
	}

	/**
	 * Attempt to sample a pixel of the source image, if the (x,y) coordinate is within the bounds of the 
	 * @param x paper-space coordinates of the image
	 * @param y paper-space coordinates of the image
	 * @return 255 if the image cannot be sampled.  The intensity of the color channel [0...255]
	 */
	public int sample1x1(float x, float y) {
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
	public int sample1x1Unchecked(float x, float y) {
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
		
		double a = 255-c.getAlpha();
		int c2 = (int)((255 - colorToBlend) * (a / 255.0) + colorToBlend);
		return c2;
	}

	public int sample3x3(float x, float y) {
		int value = 0, weight = 0;

		if (canSampleAt(x - 1, y - 1)) {			value += sample1x1Unchecked(x - 1, y - 1);			weight += 1;		}
		if (canSampleAt(x    , y - 1)) {			value += sample1x1Unchecked(x    , y - 1);			weight += 1;		}
		if (canSampleAt(x + 1, y - 1)) {			value += sample1x1Unchecked(x + 1, y - 1);			weight += 1;		}
		if (canSampleAt(x - 1, y    )) {			value += sample1x1Unchecked(x - 1, y    );			weight += 1;		}
		if (canSampleAt(x    , y    )) {			value += sample1x1Unchecked(x    , y    );			weight += 1;		}
		if (canSampleAt(x + 1, y    )) {			value += sample1x1Unchecked(x + 1, y    );			weight += 1;		}
		if (canSampleAt(x - 1, y + 1)) {			value += sample1x1Unchecked(x - 1, y + 1);			weight += 1;		}
		if (canSampleAt(x    , y + 1)) {			value += sample1x1Unchecked(x    , y + 1);			weight += 1;		}
		if (canSampleAt(x + 1, y + 1)) {			value += sample1x1Unchecked(x + 1, y + 1);			weight += 1;		}

		if (weight == 0)
			return 255;

		return value / weight;
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

	public void setScaleX(float x) {
		scaleX = x;
	}
	
	public void setScaleY(float y) {
		scaleY = y;
	}

	public void translateX(float x) {
		translateX = x;
	}

	public void translateY(float y) {
		translateY += y;
	}
}
