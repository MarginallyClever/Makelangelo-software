package com.marginallyclever.converters;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.filters.ImageFilter;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;

/**
 * Converts a BufferedImage to gcode
 * @author danroyer
 *
 */
public abstract class ImageConverter extends ImageManipulator implements DrawPanelDecorator {
	// image properties
	protected int imageWidth, imageHeight;
	// paper dimensions
	protected double paperWidth, paperHeight;
	// scaled values
	protected double xStart, yStart;
	protected double xEnd, yEnd;
	// sampling helpers
	protected float sampleValue;
	protected float sampleSum;

	protected int colorChannel = 0;


	public ImageConverter(MakelangeloRobotSettings mc) {
		super(mc);
	}

	/**
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @return true if conversion succeeded.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		return false;
	}


	protected void imageStart(BufferedImage img, Writer out) throws IOException {
		tool = machine.getCurrentTool();

		imageSetupTransform(img);

		out.write(machine.getConfigLine() + ";\n");
		out.write(machine.getBobbinLine() + ";\n");

		previousX = 0;
		previousY = 0;

		setAbsoluteMode(out);
	}

	/**
	 * setup transform from source image dimensions to destination paper dimensions.
	 *
	 * @param img source dimensions
	 */
	protected void imageSetupTransform(BufferedImage img) {
		setupTransform(img.getWidth(), img.getHeight());
	}


	/**
	 * Sets up TX() and TY() transform.  The transform automatically fits an image to the paper margins with the correct image aspect ratio. \
	 * TODO This could be improved by allowing the image to be moved, cropped, rotated, flipped, etc.
	 * The variable names make no sense when there is no image from which to transform.
	 * @param width
	 * @param height
	 */
	protected void setupTransform(int width, int height) {
		imageHeight = height;
		imageWidth = width;
		h2 = imageHeight / 2;
		w2 = imageWidth / 2;

		scale = 10f;  // 10mm = 1cm

		double newHeight = imageHeight;

		if (imageWidth > machine.getPaperWidth()) {
			float resize = (float) machine.getPaperWidth() / (float) imageWidth;
			scale *= resize;
			newHeight *= resize;
		}
		if (newHeight > machine.getPaperHeight()) {
			float resize = (float) machine.getPaperHeight() / (float) newHeight;
			scale *= resize;
			newHeight = machine.getPaperHeight();
		}
		scale *= machine.getPaperMargin();
	}


	protected int sample1x1(BufferedImage img, int x, int y) {
		Color c = new Color(img.getRGB(x, y));
		switch (colorChannel) {
		case 1:
			return c.getRed();
		case 2:
			return c.getGreen();
		case 3:
			return c.getBlue();
		default:
			return ImageFilter.decode(c);
		}
	}


	protected int sample3x3(BufferedImage img, int x, int y) {
		int value = 0, weight = 0;

		if (y > 0) {
			if (x > 0) {
				value += sample1x1(img, x - 1, y - 1);
				weight += 1;
			}
			value += sample1x1(img, x, y - 1) * 2;
			weight += 2;

			if (x < imageWidth - 1) {
				value += sample1x1(img, x + 1, y - 1);
				weight += 1;
			}
		}

		if (x > 0) {
			value += sample1x1(img, x - 1, y) * 2;
			weight += 2;
		}
		value += sample1x1(img, x, y) * 4;
		weight += 4;
		if (x < imageWidth - 1) {
			value += sample1x1(img, x + 1, y) * 2;
			weight += 2;
		}

		if (y < imageHeight - 1) {
			if (x > 0) {
				value += sample1x1(img, x - 1, y + 1);
				weight += 1;
			}
			value += sample1x1(img, x, y + 1) * 2;
			weight += 2;

			if (x < imageWidth - 1) {
				value += sample1x1(img, x + 1, y + 1);
				weight += 1;
			}
		}

		return value / weight;
	}


	protected void sample1x1Safe(BufferedImage img, int x, int y, double scale) {
		if (x < 0 || x >= imageWidth) return;
		if (y < 0 || y >= imageHeight) return;

		sampleValue += sample1x1(img, x, y) * scale;
		sampleSum += scale;
	}

	/**
	 * sample the image, taking into account fractions of pixels.
	 *
	 * @param img the image to sample
	 * @param x0  top left corner
	 * @param y0  top left corner
	 * @param x1  bottom right corner
	 * @param y1  bottom right corner
	 * @return greyscale intensity in this region. range 0...255 inclusive
	 */
	protected int sample(BufferedImage img, double x0, double y0, double x1, double y1) {
		sampleValue = 0;
		sampleSum = 0;

		double xceil = Math.ceil(x0);
		double xweightstart = (x0 != xceil) ? xceil - x0 : 1;

		double xfloor = Math.floor(x1);
		double xweightend = (x1 != xceil) ? xfloor - x1 : 0;

		int left = (int) Math.floor(x0);
		int right = (int) Math.ceil(x1);

		// top edge
		double yceil = Math.ceil(y0);
		if (y0 != yceil) {
			double yweightstart = yceil - y0;

			// left edge
			sample1x1Safe(img, (int) x0, (int) y0, xweightstart * yweightstart);

			for (int i = left; i < right; ++i) {
				sample1x1Safe(img, i, (int) y0, yweightstart);
			}
			// right edge
			sample1x1Safe(img, right, (int) y0, xweightend * yweightstart);
		}

		int bottom = (int) Math.floor(y0);
		int top = (int) Math.ceil(y1);
		for (int j = bottom; j < top; ++j) {
			// left edge
			sample1x1Safe(img, (int) x0, j, xweightstart);

			for (int i = left; i < right; ++i) {
				sample1x1Safe(img, i, j, 1);
			}
			// right edge
			sample1x1Safe(img, right, j, xweightend);
		}

		// bottom edge
		double yfloor = Math.floor(y1);
		if (y1 != yfloor) {
			double yweightend = yfloor - y1;

			// left edge
			sample1x1Safe(img, (int) x0, (int) y1, xweightstart * yweightend);

			for (int i = left; i < right; ++i) {
				sample1x1Safe(img, i, (int) y1, yweightend);
			}
			// right edge
			sample1x1Safe(img, right, (int) y1, xweightend * yweightend);
		}

		return (int) (sampleValue / sampleSum);
	}


	protected int sampleScale(BufferedImage img, double x0, double y0, double x1, double y1) {
		return sample(img,
				(x0 - xStart) / (xEnd - xStart) * (double) imageWidth,
				(double) imageHeight - (y1 - yStart) / (yEnd - yStart) * (double) imageHeight,
				(x1 - xStart) / (xEnd - xStart) * (double) imageWidth,
				(double) imageHeight - (y0 - yStart) / (yEnd - yStart) * (double) imageHeight
				);
	}


	protected boolean isInsideLimits(double x,double y) {
		if(x<xStart) return false;
		if(x>=xEnd) return false;
		if(y<yStart) return false;
		if(y>=yEnd) return false;
		return true;
	}

	/**
	 *  if the image were projected on the paper, where would the top left corner of the image be in paper space?
	 *  image(0,0) is (-paperWidth/2,-paperHeight/2)*machine.getPaperMargin()
	 */
	protected void setupPaperImageTransform() {
		paperWidth = machine.getPaperWidth();
		paperHeight = machine.getPaperHeight();

		xStart = -paperWidth / 2.0;
		yStart = xStart * (double) imageHeight / (double) imageWidth;

		if (yStart < -(paperHeight / 2.0)) {
			xStart *= (-(paperHeight / 2.0)) / yStart;
			yStart = -(paperHeight / 2.0);
		}

		xStart *= 10.0 * machine.getPaperMargin();
		yStart *= 10.0 * machine.getPaperMargin();
		xEnd = -xStart;
		yEnd = -yStart;
	}
	
	@Override
	public void render(GL2 gl2,MakelangeloRobotSettings settings) {}
}
