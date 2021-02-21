package com.marginallyclever.makelangelo.nodes.deprecated;


import java.util.Arrays;
import java.util.Comparator;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodes.ImageConverter;

/**
 * Attempt to generate a magic circle weaving pattern as seen in https://github.com/i-make-robots/weaving_algorithm
 * Probably never going to work - the calculation time is insane.
 * @author Dan Royer
 */
@Deprecated
public class Converter_MagicCircle extends ImageConverter {
	private static int numberOfPoints = 188;
	private static int numberToDraw = 5000;

	@Override
	public String getName() {
		return Translator.get("Converter_MagicCircle.name");
	}

	/**
	 * @author Dan Royer
	 */
	private class LineIntensity {
		public int i,j;
		public int intensity=512;
	}

	/**
	 * @author Dan Royer
	 */
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
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());
		
		int numLines = numberOfPoints * numberOfPoints / 2;
		LineIntensity [] intensities = new LineIntensity[numLines*2];
		double [] px = new double[numberOfPoints];
		double [] py = new double[numberOfPoints];
		
		double toolDiameter = 10.0;

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		
		// find the largest circle that still fits in the image.
		double height  = yTop-yBottom;
		double width   = xRight-xLeft;
		double maxr = ( height < width ? height : width );

		
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

		outputTurtle.setValue(turtle);
		return false;
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
