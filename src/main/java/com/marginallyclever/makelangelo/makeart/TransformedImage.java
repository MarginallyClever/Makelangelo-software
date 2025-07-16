package com.marginallyclever.makelangelo.makeart;

import com.marginallyclever.makelangelo.makeart.imagefilter.ImageFilter;

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

	public TransformedImage(BufferedImage src) {
		sourceImage = deepCopy(src);
		translateX = -src.getWidth() / 2.0f;
		translateY = -src.getHeight() / 2.0f;
		scaleX = 1;
		scaleY = 1;
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

	/**
	 * Can the image be sampled at this location?
	 * @param x pre-transform x
	 * @param y pre-transform y
	 * @return true if the image can be sampled at this location
	 */
	public boolean canSampleAt(double x, double y) {
		int sampleX = getTransformedX(x);
		int sampleY = getTransformedY(y);

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
	 * Sample the image, taking into account fractions of pixels.
	 * @param x0 left
	 * @param y0 top
	 * @param x1 right
	 * @param y1 bottom
	 * @return greyscale intensity in this region. [0...255]
	 */
	public int sample(double x0, double y0, double x1, double y1) {
		// transform the corners
		int left = getTransformedX(x0);
		int bottom = getTransformedY(y0);
		int right = getTransformedX(x1);
		int top = getTransformedY(y1);
		// in case of flip, make sure the left is less than the right, etc.
		if (left > right) {
			int temp = left;
			left = right;
			right = temp;
		}
		if (bottom > top) {
			int temp = bottom;
			bottom = top;
			top = temp;
		}
		// find the bounds of the image once, instead of inside the loops.
		bottom = Math.max(Math.min(bottom, sourceImage.getHeight()), 0);
		top = Math.max(Math.min(top, sourceImage.getHeight()-1), 0);
		left = Math.max(Math.min(left, sourceImage.getWidth()), 0);
		right = Math.max(Math.min(right, sourceImage.getWidth()-1), 0);

		// now sample the entire area to average the intensity
		int count = 0;
		var componentCount = sourceImage.getColorModel().getNumComponents();
		var pixel = new double[componentCount];

		var raster = sourceImage.getRaster();
		double sampleValue = 0;
		for(int y=bottom;y<=top;++y) {
			for(int x=left;x<=right;++x) {
				raster.getPixel(x, y, pixel);
				double sum = 0;
				for(int i=0;i<componentCount;++i) {
					sum += pixel[i];
				}
				double intensity = sum / componentCount;
				sampleValue += intensity;
				count++;
			}
		}
		if(count==0) return 255;
		// average the intensity
		double result = sampleValue / (double)count;
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
	 * Transforms x,y to the local space and samples the source image without a safety check.
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

	public void setScale(float x,float y) {
		scaleX = x;
		scaleY = y;
	}

	public void setTranslation(float x,float y) {
		translateX = x;
		translateY = y;
	}

	public void setRGB(float x, float y, int c) {
		sourceImage.setRGB(getTransformedX(x), getTransformedY(y), c);
	}
}
