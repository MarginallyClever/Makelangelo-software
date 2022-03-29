package com.marginallyclever.makelangelo.makeart.imageconverter;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.convenience.ColorPalette;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

@Deprecated
public class Converter_ColorBoxes extends ImageConverter {
	private ColorPalette palette;
	private double step1;
	private double step2;
	private double step4;
	private int palette_mask;
	private ColorRGB[] error = null;
	private ColorRGB[] nexterror = null;
	private double stepsTotal = 0;
	private int direction = 1;

	// TODO make this a parameter
	public boolean draw_filled = false;

	public Converter_ColorBoxes() {
		palette = new ColorPalette();
		palette.addColor(new ColorRGB(0, 0, 0));
		palette.addColor(new ColorRGB(255, 0, 0));
		palette.addColor(new ColorRGB(0, 255, 0));
		palette.addColor(new ColorRGB(0, 0, 255));
	}

	public String getName() {
		return Translator.get("RGBName");
	}

	private void ditherDirection(TransformedImage img, int y, ColorRGB[] error, ColorRGB[] nexterror, int direction) {
		ColorRGB oldPixel = new ColorRGB(0, 0, 0);
		ColorRGB newPixel = new ColorRGB(0, 0, 0);
		ColorRGB quant_error = new ColorRGB(0, 0, 0);
		double start, x;

		int xi;
		for (xi = 0; xi < nexterror.length; ++xi) nexterror[xi].set(0, 0, 0);

		double xLeft   = myPaper.getMarginLeft();
		double xRight  = myPaper.getMarginRight();
		
		if (direction > 0) {
			start = xLeft;
		} else {
			start = xRight - step4;
		}

		x = start;
		
		for(xi=0;xi<error.length;++xi) {
			// oldpixel := pixel[x][y]
			//oldPixel.set( new C3(img.getRGB(x, y)).add(error[x]) );
			oldPixel.set(new ColorRGB(img.sample(x-step2, y-step2, x + step2, y + step2)).add(error[xi]));
			// newpixel := find_closest_palette_color(oldpixel)
			int newIndex = palette.quantizeIndex(oldPixel);
			newPixel = palette.getColor(newIndex);

			// pixel[x][y] := newpixel
			if (newIndex == palette_mask) {
				// draw a box.  the size is relative to the intensity.
				if (draw_filled) {
					turtle.jumpTo( x + step2 - step2, y + step2 - step2);
					turtle.setAngle(0);
					turtle.forward(step2*2);	turtle.turn(-90);
					turtle.forward(step2*2);	turtle.turn(-90);
					turtle.forward(step2*2);	turtle.turn(-90);
					turtle.forward(step2*2);	turtle.turn(-90);
					turtle.moveTo( x + step2 - step1, y + step2 - step1);
					turtle.setAngle(0);
					turtle.forward(step1*2);	turtle.turn(-90);
					turtle.forward(step1*2);	turtle.turn(-90);
					turtle.forward(step1*2);	turtle.turn(-90);
					turtle.forward(step1*2);	turtle.turn(-90);
				} else {
					turtle.jumpTo( x + step2 - step1, y + step2 - step1);
					turtle.setAngle(0);
					turtle.forward(step1*2);	turtle.turn(-90);
					turtle.forward(step1*2);	turtle.turn(-90);
					turtle.forward(step1*2);	turtle.turn(-90);
					turtle.forward(step1*2);	turtle.turn(-90);
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

	protected void scan(int tool_index, TransformedImage img, String colorName, ColorRGB newPenColor) {
		palette_mask = tool_index;

		turtle.penUp();
		turtle.setColor(newPenColor);

		int y;

		for (y = 0; y < error.length; ++y) {
			error[y] = new ColorRGB(0, 0, 0);
			nexterror[y] = new ColorRGB(0, 0, 0);
		}

		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop();
		
		direction = 1;
		for (y = (int)yBottom; y < yTop; y+= step4) {
			ditherDirection(img, y, error, nexterror, direction);

			direction = -direction;
			ColorRGB[] tmp = error;
			error = nexterror;
			nexterror = tmp;
		}
	}

	/**
	 * turn the image into a grid of boxes.  box size is affected by source image darkness.
	 * {@link #myImage} the image to convert.
	 */
	@Override
	public void finish() {
		double pw = myPaper.getMarginWidth();

		// figure out how many boxes we're going to have on this image.
		step4 = 1;
		step2 = (step4 / 2.0f);  // half step
		step1 = (step4 / 4.0f);  // quarter step
		stepsTotal = pw / step4;
		if (stepsTotal < 1) stepsTotal = 1;

		// set up the error buffers for floyd/steinberg dithering
		error = new ColorRGB[(int) Math.ceil(stepsTotal)];
		nexterror = new ColorRGB[(int) Math.ceil(stepsTotal)];

		scan(0, myImage, "Black",new ColorRGB(  0,  0,  0));  // black
		scan(1, myImage, "Red"  ,new ColorRGB(255,  0,  0));  // red
		scan(2, myImage, "Green",new ColorRGB(  0,255,  0));  // green
		scan(3, myImage, "Blue" ,new ColorRGB(  0,  0,255));  // blue
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {}
}
