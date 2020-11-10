package com.marginallyclever.artPipeline.converters;


import java.util.Arrays;
import java.util.Comparator;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * Attempt to generate a magic circle weaving pattern as seen in https://github.com/i-make-robots/weaving_algorithm
 *
 * @author Dan
 */
public class Converter_MagicCircle extends ImageConverter {
	private static int numberOfPoints = 200;
	private static int numberToDraw = 10000;

	@Override
	public String getName() {
		return Translator.get("MagicCircleName");
	}

	private class LineIntensity {
		public int i,j;
		public int intensity=512;
	}

	public class IntensityComparator implements Comparator<LineIntensity> {
		@Override
		public int compare(LineIntensity o1, LineIntensity o2) {
			return (o1.intensity - o2.intensity);
		}
	}
	
	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 */
	@Override
	public void finish() {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		turtle = new Turtle();
		
		int numLines = numberOfPoints * numberOfPoints / 2;
		LineIntensity [] intensities = new LineIntensity[numLines*2];
		double [] px = new double[numberOfPoints];
		double [] py = new double[numberOfPoints];
		
		double toolDiameter = machine.getPenDiameter();

		// find the largest circle that still fits in the image.
		double w = machine.getMarginWidth()/2.0f;
		double h = machine.getMarginHeight()/2.0f;
		double maxr = ( h < w ? h : w );

		
		int i,j,k;
		for(i=0;i<numberOfPoints;++i) {
			double d = Math.PI * 2.0 * (double)i/(double)numberOfPoints;
			px[i] = Math.sin(d) * maxr;
			py[i] = Math.cos(d) * maxr;
		}

		for(i=0;i<numberOfPoints*numberOfPoints;++i) {
			intensities[i] = new LineIntensity();
		}
		
		// go around the circle, calculating intensities
		for(i=0;i<numberOfPoints;++i) {
			for(j=i+1;j<numberOfPoints;++j) {
				int index = i*numberOfPoints + j;
				double dx = px[j] - px[i];
				double dy = py[j] - py[i];
				double len = Math.floor( Math.sqrt(dx*dx+dy*dy) / toolDiameter );
				
				// measure how dark is the image under this line.
				double intensity = 0;
				for(k=0;k<len;++k) {
					double s = (double)k/len; 
					double fx = px[i] + dx * s;
					double fy = py[i] + dy * s;
					intensity += img.sample3x3((float)fx, (float)fy);
				}
				intensities[index].intensity = (int)( intensity / len );
				intensities[index].i=i;
				intensities[index].j=j;
			}
		}
		
		// sort by intensity, descending.
		Arrays.sort(intensities, new IntensityComparator());

		// draw darkest lines first.
		turtle = new Turtle();
		
		for(k=0;k<numberToDraw;++k) {
			i = intensities[k].i;
			j = intensities[k].j;
			Log.message(""+intensities[k].intensity);
			assert(intensities[k].intensity<255);
			turtle.jumpTo(px[i], py[i]);
			turtle.moveTo(px[j],py[j]);
		}
		turtle.penUp();
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
