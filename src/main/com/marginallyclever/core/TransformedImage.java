package com.marginallyclever.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.convenience.imageFilters.ImageFilter;

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
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int BOTTOM = 2;
	public static final int TOP = 3;
	
	private BufferedImage sourceImage;
	protected Texture texture = null;
	private Matrix3d matrix = new Matrix3d();
	private Matrix3d inverseMatrix = new Matrix3d();
	
	private double scaleX, scaleY;
	private double translateX, translateY;
	private double rotationDegrees;
	private int colorChannel;

	public TransformedImage(BufferedImage src) {
		sourceImage = src;
		translateX = 0;
		translateY = 0;
		scaleX = 1;
		scaleY = -1;
		rotationDegrees = 0;
		colorChannel = 0;
		refreshMatrix();
	}
	
	
	public void refreshMatrix() {
		double angle = Math.toRadians(rotationDegrees);
		double sinAngle = Math.sin(angle);
		double cosAngle = Math.cos(angle);

		matrix.m00 = cosAngle * scaleX;
		matrix.m01 = -sinAngle * scaleX;
		matrix.m02 = translateX;
		
		matrix.m10 = sinAngle * scaleY;
		matrix.m11 = cosAngle * scaleY;
		matrix.m12 = translateY;
		
		matrix.m20 = 0.0;
		matrix.m21 = 0.0;
		matrix.m22 = 1.0;
		
		inverseMatrix.invert(matrix);
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
		refreshMatrix();
	}

	public boolean canSampleAt(double x, double y) {
		Point3d p = new Point3d(x,y,1);
		reverseTransform(p);
		
		if (p.x < 0 || p.x >= sourceImage.getWidth()) return false;
		if (p.y < 0 || p.y >= sourceImage.getHeight()) return false;
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

	public double getRotationDegrees() {
		return rotationDegrees;
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public double getTranslateX() {
		return translateX;
	}

	public double getTranslateY() {
		return translateY;
	}
	
	public BufferedImage getSourceImage() {
		return sourceImage;
	}

	public void rotateAbsolute(double degrees) {
		rotationDegrees = degrees;
		refreshMatrix();
	}

	public void rotateRelative(double degrees) {
		rotationDegrees += degrees;
		refreshMatrix();
	}

	public int sample(double cx, double cy, double radius) {
		return sample(cx-radius,cy-radius,cx+radius,cy+radius);
	}
	
	/**
	 * Sample the image, taking into account fractions of pixels. left must be less than right, bottom must be less than top.
	 *
	 * @param x0 left
	 * @param y0 top
	 * @param x1 right
	 * @param y1 bottom
	 * @return greyscale intensity in this region. range 0...255 inclusive
	 */
	public int sample(double x0, double y0, double x1, double y1) {
		double sampleValue = 0;
		double weightedSum = 0;
		
		if(x1<x0) {
			double temp = x1;
			x1=x0;
			x0=temp;
		}
		if(y1<y0) {
			double temp = y1;
			y1=y0;
			y0=temp;
		}

		int left   = (int)Math.floor(x0);
		int right  = (int)Math.ceil (x1);
		int bottom = (int)Math.floor(y0);
		int top    = (int)Math.ceil (y1);

		// calculate the weight matrix
		int w = right-left;
		int h = top-bottom;
		
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
		Point3d p = new Point3d(x,y,1);
		inverseMatrix.transform(p);
		p.x+= sourceImage.getWidth()/2;
		p.y+= sourceImage.getHeight()/2;

		Color color = new Color(sourceImage.getRGB((int)p.x, (int)p.y));
		int colorToBlend=0;
		
		switch (colorChannel) {
		case 1: colorToBlend=color.getRed();  break;
		case 2: colorToBlend=color.getGreen();  break;
		case 3: colorToBlend=color.getBlue();  break;
		default: return ImageFilter.decodeColor(color);
		}
		
		double a = 1.0 - color.getAlpha()/255.0;
		int c = (int)(  (255.0 - (double)colorToBlend) * a + (double)colorToBlend);
		c = c<255 ? c:255;
		c = c>0 ? c:0;
		return c;
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

	public void setScale(double x,double y) {
		scaleX = x;
		scaleY = -y;
		refreshMatrix();
	}
	
	public void setTranslateX(double x) {
		translateX = x;
		refreshMatrix();
	}

	public void setTranslateY(double y) {
		translateY += y;
		refreshMatrix();
	}
	
	/**
	 * find minimum bounds of transformed image - the smallest box to contain the image after transformation
	 * @return { x left, x right, y bottom, y top }
	 */
	public double [] getBounds() {
		double ow=sourceImage.getWidth()/2.0;
		double oh=sourceImage.getHeight()/2.0;

		Point3d [] corners = { 
			new Point3d( ow, oh,1),
			new Point3d(-ow, oh,1),
			new Point3d(-ow,-oh,1),
			new Point3d( ow,-oh,1)
		};
		double xMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;

		for( Point3d p : corners ) {
			matrix.transform(p);
			xMin = Math.min(xMin, p.x);
			xMax = Math.max(xMax, p.x);
			yMin = Math.min(yMin, p.y);
			yMax = Math.max(yMax, p.y);
		}
		return ( new double[] {xMin,xMax,yMin,yMax} );
	}
	
	/**
	 * Load a file from disk.
	 * @param filename
	 * @return
	 */
	static public TransformedImage loadImage(String filename) {
		TransformedImage img = null;
		
		try (final InputStream fileInputStream = new FileInputStream(filename)) {
			img = new TransformedImage( ImageIO.read(fileInputStream) );
		} catch(IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	/**
	 * Draw the image, transformed.
	 */
	public void render(GL2 gl2) {
		double ow=sourceImage.getWidth()/2.0;
		double oh=sourceImage.getHeight()/2.0;
		Point3d [] corners = { 
			new Point3d(ow,oh,1),
			new Point3d(-ow,oh,1),
			new Point3d(-ow,-oh,1),
			new Point3d(ow,-oh,1)
		};
		
		for( Point3d p : corners ) {
			matrix.transform(p);
		}
		
		if( texture==null ) {
			if( sourceImage!=null) {
				texture = AWTTextureIO.newTexture(gl2.getGLProfile(), sourceImage, false);
			}
		}
	
		if(texture!=null) {
			gl2.glEnable(GL2.GL_TEXTURE_2D);
			gl2.glEnable(GL2.GL_BLEND);
			gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			//gl2.glDisable(GL2.GL_COLOR);
			gl2.glColor4d(1, 1, 1, 0.5);
			gl2.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
			texture.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(0, 0);	gl2.glVertex2d(corners[0].x,corners[0].y);
			gl2.glTexCoord2d(1, 0);	gl2.glVertex2d(corners[1].x,corners[1].y);
			gl2.glTexCoord2d(1, 1);	gl2.glVertex2d(corners[2].x,corners[2].y);
			gl2.glTexCoord2d(0, 1);	gl2.glVertex2d(corners[3].x,corners[3].y);
			gl2.glEnd();
			gl2.glDisable(GL2.GL_TEXTURE_2D);
			gl2.glDisable(GL2.GL_BLEND);
			//gl2.glEnable(GL2.GL_COLOR);
		}
		// border
		{
			gl2.glLineWidth(1);
			gl2.glColor4d(1, 1, 1, 1);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glTexCoord2d(0, 0);	gl2.glVertex2d(corners[0].x,corners[0].y);
			gl2.glTexCoord2d(1, 0);	gl2.glVertex2d(corners[1].x,corners[1].y);
			gl2.glTexCoord2d(1, 1);	gl2.glVertex2d(corners[2].x,corners[2].y);
			gl2.glTexCoord2d(0, 1);	gl2.glVertex2d(corners[3].x,corners[3].y);
			gl2.glEnd();
		}
	}

	private void reverseTransform(Point3d p) {
		inverseMatrix.transform(p);
		p.x+= sourceImage.getWidth()/2;
		p.y+= sourceImage.getHeight()/2;
	}

	public void setRGB(float x, float y, int c) {
		Point3d p = new Point3d(x,y,1);
		reverseTransform(p);

		getSourceImage().setRGB((int)p.x, (int)p.y, c);
	}
}
