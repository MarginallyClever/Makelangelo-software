package com.marginallyclever.converters;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.C3;
import com.marginallyclever.basictypes.ColorPalette;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;


/**
 * @author Dan
 */
public class Converter_ColorBoxes extends ImageConverter {
	private ColorPalette palette;
	private float step1;
	private float step2;
	private float step4;
	private int palette_mask;
	private C3[] error = null;
	private C3[] nexterror = null;
	private float stepw = 0, steph = 0;
	private int direction = 1;


	public Converter_ColorBoxes(Makelangelo gui, MakelangeloRobotSettings mc,
			Translator ms) {
		super(gui, mc, ms);

		palette = new ColorPalette();
		palette.addColor(new C3(0, 0, 0));
		palette.addColor(new C3(255, 0, 0));
		palette.addColor(new C3(0, 255, 0));
		palette.addColor(new C3(0, 0, 255));
	}


	public String getName() {
		return translator.get("RGBName");
	}

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out1, float x, float y, boolean up) throws IOException {
		if (lastUp != up) {
			if (up) liftPen(out1);
			else lowerPen(out1);
			lastUp = up;
		}
		tool.writeMoveTo(out1, TX(x), TY(y));
	}


	private void ditherDirection(BufferedImage img, int y, C3[] error, C3[] nexterror, int direction, Writer out) throws IOException {
		float w = stepw;
		C3 oldPixel = new C3(0, 0, 0);
		C3 newPixel = new C3(0, 0, 0);
		C3 quant_error = new C3(0, 0, 0);
		int start, end, x;

		for (x = 0; x < w; ++x) nexterror[x].set(0, 0, 0);

		if (direction > 0) {
			start = 0;
			end = (int) w;
		} else {
			start = (int) (w - 1);
			end = -1;
		}

		// @TODO: make this a parameter
		boolean draw_filled = false;

		// for each x from left to right
		for (x = start; x != end; x += direction) {
			// oldpixel := pixel[x][y]
			//oldPixel.set( new C3(img.getRGB(x, y)).add(error[x]) );
			oldPixel.set(new C3(takeImageSampleBlock(img, (int) (x * step4), (int) (y * step4), (int) (x * step4 + step4), (int) (y * step4 + step4))).add(error[x]));
			// newpixel := find_closest_palette_color(oldpixel)
			int newIndex = palette.quantizeIndex(oldPixel);
			newPixel = palette.getColor(newIndex);

			// pixel[x][y] := newpixel
			if (newIndex == palette_mask) {
				// draw a circle.  the diameter is relative to the intensity.
				if (draw_filled) {
					moveTo(out, x * step4 + step2 - step2, y * step4 + step2 - step2, true);
					moveTo(out, x * step4 + step2 + step2, y * step4 + step2 - step2, false);
					moveTo(out, x * step4 + step2 + step2, y * step4 + step2 + step2, false);
					moveTo(out, x * step4 + step2 - step2, y * step4 + step2 + step2, false);
					moveTo(out, x * step4 + step2 - step2, y * step4 + step2 - step2, false);
					moveTo(out, x * step4 + step2 + step1, y * step4 + step2 - step1, false);
					moveTo(out, x * step4 + step2 + step1, y * step4 + step2 + step1, false);
					moveTo(out, x * step4 + step2 - step1, y * step4 + step2 + step1, false);
					moveTo(out, x * step4 + step2 - step1, y * step4 + step2 - step1, false);
					moveTo(out, x * step4 + step2, y * step4 + step2, false);
					moveTo(out, x * step4 + step2, y * step4 + step2, true);
				} else {
					moveTo(out, x * step4 + step2 - step1, y * step4 + step2 - step1, true);
					moveTo(out, x * step4 + step2 + step1, y * step4 + step2 - step1, false);
					moveTo(out, x * step4 + step2 + step1, y * step4 + step2 + step1, false);
					moveTo(out, x * step4 + step2 - step1, y * step4 + step2 + step1, false);
					moveTo(out, x * step4 + step2 - step1, y * step4 + step2 - step1, false);
					moveTo(out, x * step4 + step2 - step1, y * step4 + step2 - step1, true);
				}
			}

			// quant_error := oldpixel - newpixel
			quant_error.set(oldPixel.sub(newPixel));
			// pixel[x+1][y  ] += 7/16 * quant_error
			// pixel[x-1][y+1] += 3/16 * quant_error
			// pixel[x  ][y+1] += 5/16 * quant_error
			// pixel[x+1][y+1] += 1/16 * quant_error
			nexterror[x].add(quant_error.mul(5.0 / 16.0));
			if (x + direction >= 0 && x + direction < w) {
				error[x + direction].add(quant_error.mul(7.0 / 16.0));
				nexterror[x + direction].add(quant_error.mul(1.0 / 16.0));
			}
			if (x - direction >= 0 && x - direction < w) {
				nexterror[x - direction].add(quant_error.mul(3.0 / 16.0));
			}
		}
	}


	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	protected C3 takeImageSampleBlock(BufferedImage img, int x0, int y0, int x1, int y1) {
		// point sampling
		C3 value = new C3(0, 0, 0);
		int sum = 0;

		if (x0 < 0) x0 = 0;
		if (x1 > imageWidth - 1) x1 = imageWidth - 1;
		if (y0 < 0) y0 = 0;
		if (y1 > imageHeight - 1) y1 = imageHeight - 1;

		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				value.add(new C3(img.getRGB(x, y)));
				++sum;
			}
		}

		if (sum == 0) return new C3(255, 255, 255);

		return value.mul(1.0f / sum);
	}


	protected void scan(int tool_index, BufferedImage img, Writer out) throws IOException {
		palette_mask = tool_index;

		// "please change to tool X and press any key to continue"
		tool = machine.getTool(tool_index);
		tool.writeChangeTo(out);
		// Make sure the pen is up for the first move
		liftPen(out);

		int y;

		for (y = 0; y < error.length; ++y) {
			error[y] = new C3(0, 0, 0);
			nexterror[y] = new C3(0, 0, 0);
		}

		direction = 1;
		for (y = 0; y < steph; ++y) {
			ditherDirection(img, y, error, nexterror, direction, out);

			direction = -direction;
			C3[] tmp = error;
			error = nexterror;
			nexterror = tmp;
		}
	}


	/**
	 * turn the image into a grid of boxes.  box size is affected by source image darkness.
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(img, out);

		double pw = machine.getPaperWidth();

		// figure out how many lines we're going to have on this image.
		float steps = (float) (pw / (tool.getDiameter() * 1.75f));

		if (steps < 1) steps = 1;

		step4 = (steps);
		step2 = (step4 / 2.0f);
		step1 = (step4 / 4.0f);

		// set up the error buffers for floyd/steinberg dithering
		stepw = ((float) imageWidth / step4);
		steph = ((float) imageHeight / step4);
		error = new C3[(int) Math.ceil(stepw)];
		nexterror = new C3[(int) Math.ceil(stepw)];

		try {
			scan(0, img, out);  // black
			scan(1, img, out);  // red
			scan(2, img, out);  // green
			scan(3, img, out);  // blue
		} catch (Exception e) {
			e.printStackTrace();
		}

		liftPen(out);

		return true;
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
