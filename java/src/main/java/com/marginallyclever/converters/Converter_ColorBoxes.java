package com.marginallyclever.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.basictypes.C3;
import com.marginallyclever.basictypes.ColorPalette;
import com.marginallyclever.basictypes.TransformedImage;
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
	private float stepsTotal = 0;
	private int direction = 1;


	public Converter_ColorBoxes() {
		palette = new ColorPalette();
		palette.addColor(new C3(0, 0, 0));
		palette.addColor(new C3(255, 0, 0));
		palette.addColor(new C3(0, 255, 0));
		palette.addColor(new C3(0, 0, 255));
	}


	public String getName() {
		return Translator.get("RGBName");
	}


	private void ditherDirection(TransformedImage img, int y, C3[] error, C3[] nexterror, int direction, Writer out) throws IOException {
		C3 oldPixel = new C3(0, 0, 0);
		C3 newPixel = new C3(0, 0, 0);
		C3 quant_error = new C3(0, 0, 0);
		float start, x;

		int xi;
		for (xi = 0; xi < nexterror.length; ++xi) nexterror[xi].set(0, 0, 0);

		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		
		if (direction > 0) {
			start = xLeft;
		} else {
			start = xRight - step4;
		}

		// @TODO: make this a parameter
		boolean draw_filled = false;

		x = start;
		
		for(xi=0;xi<error.length;++xi) {
			// oldpixel := pixel[x][y]
			//oldPixel.set( new C3(img.getRGB(x, y)).add(error[x]) );
			oldPixel.set(new C3(img.sample(x-step2, y-step2, x + step2, y + step2)).add(error[xi]));
			// newpixel := find_closest_palette_color(oldpixel)
			int newIndex = palette.quantizeIndex(oldPixel);
			newPixel = palette.getColor(newIndex);

			// pixel[x][y] := newpixel
			if (newIndex == palette_mask) {
				// draw a circle.  the diameter is relative to the intensity.
				if (draw_filled) {
					moveTo(out, x + step2 - step2, y + step2 - step2, true);
					lowerPen(out);
					moveTo(out, x + step2 + step2, y + step2 - step2, false);
					moveTo(out, x + step2 + step2, y + step2 + step2, false);
					moveTo(out, x + step2 - step2, y + step2 + step2, false);
					moveTo(out, x + step2 - step2, y + step2 - step2, false);
					moveTo(out, x + step2 + step1, y + step2 - step1, false);
					moveTo(out, x + step2 + step1, y + step2 + step1, false);
					moveTo(out, x + step2 - step1, y + step2 + step1, false);
					moveTo(out, x + step2 - step1, y + step2 - step1, false);
					moveTo(out, x + step2, y + step2, false);
					liftPen(out);
				} else {
					moveTo(out, x + step2 - step1, y + step2 - step1, true);
					lowerPen(out);
					moveTo(out, x + step2 + step1, y + step2 - step1, false);
					moveTo(out, x + step2 + step1, y + step2 + step1, false);
					moveTo(out, x + step2 - step1, y + step2 + step1, false);
					moveTo(out, x + step2 - step1, y + step2 - step1, false);
					liftPen(out);
				}
			}

			// quant_error := oldpixel - newpixel
			quant_error.set(oldPixel.sub(newPixel));
			// pixel[x+1][y  ] += 7/16 * quant_error
			// pixel[x-1][y+1] += 3/16 * quant_error
			// pixel[x  ][y+1] += 5/16 * quant_error
			// pixel[x+1][y+1] += 1/16 * quant_error
			nexterror[xi].add(quant_error.mul(5.0 / 16.0));
			if( xi+direction < error.length && xi+direction >= 0) {
				error[xi + direction].add(quant_error.mul(7.0 / 16.0));
				nexterror[xi + direction].add(quant_error.mul(1.0 / 16.0));
			}
			if( xi-direction < error.length && xi-direction >= 0) {
				nexterror[xi - direction].add(quant_error.mul(3.0 / 16.0));
			}
			
			x+=direction*step4;
		}
	}
	

	protected void scan(int tool_index, TransformedImage img, Writer out) throws IOException {
		palette_mask = tool_index;

		// TODO Find a way to swap color pens.
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		int y;

		for (y = 0; y < error.length; ++y) {
			error[y] = new C3(0, 0, 0);
			nexterror[y] = new C3(0, 0, 0);
		}

		int yBottom = (int)( machine.getPaperBottom() * machine.getPaperMargin() * 10.0f );
		int yTop    = (int)( machine.getPaperTop()    * machine.getPaperMargin() * 10.0f );
		
		direction = 1;
		for (y = yBottom; y < yTop; y+= step4) {
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
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(out);

		float pw = (float)(machine.getPaperWidth() * machine.getPaperMargin() * 10.0f);

		// figure out how many boxes we're going to have on this image.
		step4 = ((float)tool.getDiameter() * 10.0f);
		step2 = (step4 / 2.0f);  // half step
		step1 = (step4 / 4.0f);  // quarter step
		stepsTotal = pw / step4;
		if (stepsTotal < 1) stepsTotal = 1;

		// set up the error buffers for floyd/steinberg dithering
		error = new C3[(int) Math.ceil(stepsTotal)];
		nexterror = new C3[(int) Math.ceil(stepsTotal)];

		try {
			scan(0, img, out);  // black
			scan(1, img, out);  // red
			scan(2, img, out);  // green
			scan(3, img, out);  // blue
		} catch (Exception e) {
			e.printStackTrace();
		}

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);

		return true;
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
