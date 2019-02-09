package com.marginallyclever.makelangeloRobot.converters;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename,
 * but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Crosshatch extends ImageConverter {
	private double xStart, yStart, xEnd, yEnd;
	private static float intensity=2.0f;
	
	@Override
	public String getName() {
		return Translator.get("Crosshatch");
	}
	
	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_Crosshatch_Panel(this);
	}

	public void setIntensity(float arg0) {
		intensity=arg0;
	}
	
	public float getIntensity() {
		return intensity;
	}
	
	/**
	 * The main entry point
	 *
	 * @param img
	 *            the image to convert.
	 */
	public void finish(Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		imageStart(out);
		liftPen(out);
		convertPaperSpace(img, out);

		liftPen(out);
		moveTo(out, (float) machine.getHomeX(), (float) machine.getHomeY(), true);
	}

	protected void convertPaperSpace(TransformedImage img, Writer out) throws IOException {
		double leveladd = 255.0 / 6.0;
		double level = leveladd;

		// if the image were projected on the paper, where would the top left
		// corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		yStart = (float) machine.getPaperBottom() * (float) machine.getPaperMargin();
		yEnd   = (float) machine.getPaperTop()    * (float) machine.getPaperMargin();
		xStart = (float) machine.getPaperLeft()   * (float) machine.getPaperMargin();
		xEnd   = (float) machine.getPaperRight()  * (float) machine.getPaperMargin();

		double stepSize = machine.getPenDiameter() * intensity;
		double x, y;
		boolean flip = true;

		// vertical
		for (y = yStart; y <= yEnd; y += stepSize) {
			if (flip) {
				convertAlongLine(xStart, y, xEnd, y, stepSize, level, img, out);
			} else {
				convertAlongLine(xEnd, y, xStart, y, stepSize, level, img, out);
			}
			flip = !flip;
		}

		level += leveladd;

		// horizontal
		for (x = xStart; x <= xEnd; x += stepSize) {
			if (flip) {
				convertAlongLine(x, yStart, x, yEnd, stepSize, level, img, out);
			} else {
				convertAlongLine(x, yEnd, x, yStart, stepSize, level, img, out);
			}
			flip = !flip;
		}

		level += leveladd;

		// diagonal 1
		double dy = yEnd - yStart;
		double dx = xEnd - xStart;
		double len = dx > dy ? dx : dy;

		double x1 = -len;
		double y1 = -len;

		double x2 = +len;
		double y2 = +len;

		double len2 = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		double steps;
		if (len2 > 0)
			steps = len2 / stepSize;
		else
			steps = 1;
		double i;

		for (i = 0; i < steps; ++i) {
			double px = x1 + (x2 - x1) * (i / steps);
			double py = y1 + (y2 - y1) * (i / steps);

			double x3 = px - len;
			double y3 = py + len;
			double x4 = px + len;
			double y4 = py - len;

			if (flip) {
				convertAlongLine(x3, y3, x4, y4, stepSize, level, img, out);
			} else {
				convertAlongLine(x4, y4, x3, y3, stepSize, level, img, out);
			}
			flip = !flip;
		}

		level += leveladd;

		// diagonal 2

		x1 = +len;
		y1 = -len;

		x2 = -len;
		y2 = +len;

		for (i = 0; i < steps; ++i) {
			double px = x1 + (x2 - x1) * (i / steps);
			double py = y1 + (y2 - y1) * (i / steps);

			double x3 = px + len;
			double y3 = py + len;
			double x4 = px - len;
			double y4 = py - len;

			if (flip) {
				convertAlongLine(x3, y3, x4, y4, stepSize, level, img, out);
			} else {
				convertAlongLine(x4, y4, x3, y3, stepSize, level, img, out);
			}
			flip = !flip;
		}
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
