package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.security.InvalidParameterException;

/**
 * Histogram of an image with 8 bits red, 8 bits green, and 8 bits blue.
 *
 * @author Dan Royer
 * @since 7.1.4-SNAPSHOT?
 */
public class Histogram {
	private static final Logger logger = LoggerFactory.getLogger(Histogram.class);
	
	public char[] red = new char[256];
	public char[] green = new char[256];
	public char[] blue = new char[256];

	public Histogram() {}

	public void getRGBHistogramOf(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		int x, y;

		for (y = 0; y < 255; ++y) {
			red[y] = 0;
			green[y] = 0;
			blue[y] = 0;
		}
		
		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				Color c = new Color(img.getRGB(x, y));
				red[c.getRed()]++;
				green[c.getGreen()]++;
				blue[c.getBlue()]++;
			}
		}
	}

	/**
	 * calculate the greyscale histogram of the BufferedImage and store it in th red[] channel.
	 * @param img
	 */
	public void getGreyHistogramOf(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		int x, y;

		for (y = 0; y < 255; ++y) {
			red[y]=0;
		}
		
		for (y = 0; y < h; ++y) {
			for (x = 0; x < w; ++x) {
				Color c = new Color(img.getRGB(x, y));
				int v = (c.getRed()+c.getGreen()+c.getBlue())/3;
				v = Math.min(Math.max(0, v), 255);
				red[v]++;
			}
		}
	}

	/**
	 * Split a histogram into numLevels regions of equal weight.  the total weight is the sum of all the histogram values.
	 * @param numLevels must be >0
	 * @return an array filled with the cutoff point between each of the weighted zones.
	 */
	public double[] getLevels(int numLevels) throws InvalidParameterException {
		if(numLevels<1) throw new InvalidParameterException("numLevels must be greater than zero.");
		
		// sum the total score of the histogram.
		long total = 0;
		
		for(int i=0;i<256;++i) {
			total+=red[i];
		}
		
		double costPerLevel = (double)total/(double)numLevels;
		double[] levels = new double[numLevels];
		
		int sum=0;
		int j=0;
		for(int i=0;i<256;++i) {
			sum+=red[i];
			if(sum>=costPerLevel) {
				levels[j++]=i;
				sum-=costPerLevel;
				logger.debug("Level @ {}", i);
			}
		}
		
		return levels;
	}


	/**
	 * Split a histogram into regions.  The total weight is the sum of all the histogram values.  The 
	 * @param input an array of values [0...1] indicating the cutoff desired for each zone.
	 * @return a new array of values with the equivalent histogram value for each cutoff.
	 */
	public double[] getLevelsMapped(double [] input) throws InvalidParameterException {
		if(input==null || input.length<1) throw new InvalidParameterException("input length must be greater than zero.");
		
		// sum the total score of the histogram.
		double totalWeight = 0;
		
		for(int i=0;i<256;++i) {
			totalWeight+=red[i];
		}
		logger.debug("Total weight={}", totalWeight);
		
		double[] levels = new double[input.length];

		for(int j=0;j<input.length;++j) {
			double weightScaled = totalWeight*input[j];
			int i;
			for(i=0;i<256;++i) {
				weightScaled-=red[i];
				if(weightScaled<=0) {
					break;
				}
			}
			i = Math.min(i,255);
			levels[j]=i;
			logger.debug("Level {} ({}) -> {} = {}", input[j], (input[j]*100.0), weightScaled, i);
		}
		
		return levels;
	}
}
