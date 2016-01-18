package com.marginallyclever.converters;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Crosshatch extends ImageConverter {
	private double xStart, yStart;
	private double xEnd, yEnd;
	private double paperWidth, paperHeight;

	@Override
	public String getName() {
		return Translator.get("Crosshatch");
	}

	public Converter_Crosshatch(MakelangeloRobotSettings mc) {
		super(mc);
	}


	/**
	 * The main entry point
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(img, out);

		// set absolute coordinates
		out.write("G00 G90;\n");
		tool.writeChangeTo(out);
		liftPen(out);

		convertPaperSpace(img, out);

		liftPen(out);

		return true;
	}

	protected int sampleScale(BufferedImage img, double x0, double y0, double x1, double y1) {
		return sample(img,
				(x0 - xStart) / (xEnd - xStart) * (double) imageWidth,
				(double) imageHeight - (y1 - yStart) / (yEnd - yStart) * (double) imageHeight,
				(x1 - xStart) / (xEnd - xStart) * (double) imageWidth,
				(double) imageHeight - (y0 - yStart) / (yEnd - yStart) * (double) imageHeight
				);
	}

	protected void convertPaperSpace(BufferedImage img, Writer out) throws IOException {
		double leveladd = 255.0 / 6.0;
		double level = leveladd;

		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

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

		previousX = 0;
		previousY = 0;

		double stepSize = tool.getDiameter() * 3.0;
		double halfStep = stepSize / 2.0;
		double x, y;

		// vertical
		for (y = yStart; y < yEnd; y += stepSize) {
			moveToPaper(out, xStart, y, true);
			for (x = xStart; x < xEnd; x += stepSize) {
				int v = sampleScale(img, x - halfStep, y - halfStep, x + halfStep, y + halfStep);
				moveToPaper(out, x, y, v >= level);
			}
			moveToPaper(out, xEnd, y, true);
		}
		// horizontal
		level += leveladd;
		for (x = xStart; x < xEnd; x += stepSize) {
			moveToPaper(out, x, yStart, true);
			for (y = yStart; y < yEnd; y += stepSize) {
				int v = sampleScale(img, x - halfStep, y - halfStep, x + halfStep, y + halfStep);
				moveToPaper(out, x, y, v >= level);
			}
			moveToPaper(out, x, yEnd, true);
		}


		double x2;

		// diagonal 1
		level += leveladd;
		x = xStart;
		do {
			x2 = x;
			moveToPaper(out, x2, yStart, true);
			for (y = yStart; y < yEnd; y += stepSize, x2 -= stepSize) {
				if (x2 < xStart) {
					moveToPaper(out, xStart, y - stepSize, true);
					break;
				}
				if (x2 > xEnd) continue;
				int v = sampleScale(img, x2 - halfStep, y - halfStep, x2 + halfStep, y + halfStep);
				moveToPaper(out, x2, y, v >= level);
			}
			//if(x2>=xStart && x2 <xEnd)
			moveToPaper(out, x2, yEnd, true);

			x += stepSize;
		} while (x2 < xEnd);

		// diagonal 2
		level += leveladd;
		x = xEnd;
		do {
			x2 = x;
			moveToPaper(out, x2, yStart, true);
			for (y = yStart; y < yEnd; y += stepSize, x2 += stepSize) {
				if (x2 < xStart) continue;
				if (x2 > xEnd) {
					moveToPaper(out, xEnd, y -= stepSize, true);
					break;
				}
				int v = sampleScale(img, x2 - halfStep, y - halfStep, x2 + halfStep, y + halfStep);
				moveToPaper(out, x2, y, v >= level);
			}
			//if(x2>=xStart && x2 <xEnd)
			moveToPaper(out, x2, yEnd, true);

			x -= stepSize;
		} while (x2 > xStart);
	}
}


/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
