package com.marginallyclever.converters;


import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Pulse extends ImageConverter {
	private static float blockScale = 6.0f;
	private static int direction = 0;

	@Override
	public String getName() {
		return Translator.get("PulseLineName");
	}


	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		final JTextField field_size = new JTextField(Float.toString(blockScale));

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel(Translator.get("HilbertCurveSize")));
		panel.add(field_size);

		String[] directions = {"horizontal", "vertical"};
		final JComboBox<String> direction_choices = new JComboBox<>(directions);
		panel.add(direction_choices);

		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			blockScale = Float.parseFloat(field_size.getText());
			direction = direction_choices.getSelectedIndex();
			convertNow(img,out);
			return true;
		}
		return false;
	}


	/**
	 * Converts images into zigzags in paper space instead of image space
	 *
	 * @param img the buffered image to convert
	 * @throws IOException couldn't open output file
	 */
	private void convertNow(TransformedImage img,Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);
		
		convertPaperSpace(img, out);

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}


	private void convertPaperSpace(TransformedImage img, Writer out) throws IOException {
		double PULSE_MINIMUM = 0.5;

		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		// figure out how many lines we're going to have on this image.
		float stepSize = tool.getDiameter() * blockScale;
		float halfStep = stepSize / 2.0f;
		float zigZagSpacing = tool.getDiameter();

		// from top to bottom of the image...
		float x, y, z, scale_z, pulse_size, i = 0;
		double n = 1;

		boolean lifted=true;
		if (direction == 0) {
			// horizontal
			for (y = yBottom; y < yTop; y += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move left to right
					//moveTo(file,x,y,pen up?)]
					if(!lifted) {
						lifted=true;
						moveTo(out, xLeft, y + halfStep, true);
					}

					for (x = xLeft; x < xRight; x += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						assert (z >= 0);
						assert (z <= 255.0f);
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						if(pulse_size<PULSE_MINIMUM) {
							if(!lifted) {
								moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
								lifted=true;
							}
						} else {
							lifted=false;
							moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
						}
						n = n > 0 ? -1 : 1;
					}
					if(!lifted) {
						lifted=true;
						moveTo(out, xRight, y + halfStep, true);
					}
				} else {
					// every odd line move right to left
					//moveTo(file,x,y,pen up?)]
					if(!lifted) {
						lifted=true;
						moveTo(out, xRight, y + halfStep, true);
					}

					for (x = xRight; x >= xLeft; x -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						assert (scale_z <= 1.0);
						pulse_size = halfStep * scale_z;
						if(pulse_size<PULSE_MINIMUM) {
							if(!lifted) {
								lifted=true;
								moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
							}
						} else {
							lifted=false;
							moveTo(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
						}
						n = n > 0 ? -1 : 1;
					}
					
					if(!lifted) {
						lifted=true;
						moveTo(out, xLeft, y + halfStep, true);
					}
				}
			}
		} else {
			// vertical
			for (x = xLeft; x < xRight; x += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move top to bottom
					//moveTo(file,x,y,pen up?)]
					moveTo(out, x + halfStep, yBottom, true);

					for (y = yBottom; y < yTop; y += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						moveTo(out, (x + halfStep + pulse_size * n), y, pulse_size < PULSE_MINIMUM);
						n *= -1;
					}
					moveTo(out, x + halfStep, yTop, true);
				} else {
					// every odd line move bottom to top
					//moveTo(file,x,y,pen up?)]
					moveTo(out, x + halfStep, yTop, true);

					for (y = yTop; y >= yBottom; y -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = img.sample( x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						moveTo(out, (x + halfStep + pulse_size * n), y, pulse_size < PULSE_MINIMUM);
						n *= -1;
					}
					moveTo(out, x + halfStep, yBottom, true);
				}
			}
		}
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
