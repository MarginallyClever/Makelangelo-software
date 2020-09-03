package com.marginallyclever.convenience;

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

	public void reportGrey() {
		for( int i=0;i<256;++i ) {
			System.out.println(i+"="+(int)red[i]);
		}
	}

	/**
	 * Split a histogram into numLevels regions of equal weight.  the total weight is the sum of all the histogram values.
	 * @param numLevels must be >0
	 * @return an array filled with the cutoff point between each of the weighted zones.
	 */
	public double[] getLevels(int numLevels) throws InvalidParameterException {
		if(numLevels<1) throw new InvalidParameterException("numLevels must be greater than zero.");
		
		//reportGrey();
		
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
				System.out.println("Level @ " + i);
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
		
		//reportGrey();
		
		// sum the total score of the histogram.
		double totalWeight = 0;
		
		for(int i=0;i<256;++i) {
			totalWeight+=red[i];
		}
		System.out.println("Total weight="+totalWeight);
		
		double[] levels = new double[input.length];

		for(int j=0;j<input.length;++j) {
			double weightScaled = totalWeight*input[j];
			System.out.print("Level "+input[j]+" ("+(input[j]*100.0)+") -> "+weightScaled+" = ");
			
			int i;
			for(i=0;i<256;++i) {
				weightScaled-=red[i];
				if(weightScaled<=0) {
					break;
				}
			}
			i = Math.min(i,255);
			levels[j]=i;
			System.out.println(i);
		}
		
		return levels;
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
