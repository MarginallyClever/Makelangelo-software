package com.marginallyclever.converters;


import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;


public class Converter_Pulse extends ImageConverter {
	private float blockScale = 6.0f;
	private int direction = 0;

	public Converter_Pulse(Makelangelo gui, MakelangeloRobotSettings mc,
			Translator ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() {
		return translator.get("PulseLineName");
	}

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out, float x, float y, boolean up) throws IOException {
		if (lastUp != up) {
			if (up) liftPen(out);
			else lowerPen(out);
			lastUp = up;
		}
		tool.writeMoveTo(out, TX(x), TY(y));
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		final JTextField field_size = new JTextField(Float.toString(blockScale));

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel(translator.get("HilbertCurveSize")));
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
	private void convertNow(BufferedImage img,Writer out) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI, machine, translator, 255);
		img = bw.filter(img);

		imageStart(img, out);

		// set absolute coordinates
		out.write("G00 G90;\n");
		tool.writeChangeTo(out);
		liftPen(out);

		convertPaperSpace(img, out);

		liftPen(out);
	}


	private void convertPaperSpace(BufferedImage img, Writer out) throws IOException {
		setupPaperImageTransform();

		double PULSE_MINIMUM = 0.5;

		// figure out how many lines we're going to have on this image.
		double stepSize = tool.getDiameter() * blockScale;
		double halfStep = stepSize / 2.0;
		double zigZagSpacing = tool.getDiameter();

		// from top to bottom of the image...
		double x, y, z, scale_z, pulse_size, i = 0;
		double n = 1;

		if (direction == 0) {
			// horizontal
			for (y = yStart; y < yEnd; y += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move left to right
					//moveToPaper(file,x,y,pen up?)]
					moveToPaper(out, xStart, y + halfStep, true);

					for (x = xStart; x < xEnd; x += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = sampleScale(img, x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						assert (z >= 0);
						assert (z <= 255.0);
						scale_z = (255.0 - z) / 255.0;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;

						moveToPaper(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
						n = n > 0 ? -1 : 1;
					}
					moveToPaper(out, xEnd, y + halfStep, true);
				} else {
					// every odd line move right to left
					//moveToPaper(file,x,y,pen up?)]
					moveToPaper(out, xEnd, y + halfStep, true);

					for (x = xEnd; x >= xStart; x -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = sampleScale(img, x - zigZagSpacing, y - halfStep, x + zigZagSpacing, y + halfStep);
						// scale the intensity value
						scale_z = (255.0 - z) / 255.0;
						//scale_z *= scale_z;  // quadratic curve
						assert (scale_z <= 1.0);
						pulse_size = halfStep * scale_z;
						moveToPaper(out, x, (y + halfStep + pulse_size * n), pulse_size < PULSE_MINIMUM);
						n = n > 0 ? -1 : 1;
					}
					moveToPaper(out, xStart, y + halfStep, true);
				}
			}
		} else {
			// vertical
			for (x = xStart; x < xEnd; x += stepSize) {
				++i;

				if ((i % 2) == 0) {
					// every even line move top to bottom
					//moveToPaper(file,x,y,pen up?)]
					moveToPaper(out, x + halfStep, yStart, true);

					for (y = yStart; y < yEnd; y += zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = sampleScale(img, x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						moveToPaper(out, (x + halfStep + pulse_size * n), y, pulse_size < PULSE_MINIMUM);
						n *= -1;
					}
					moveToPaper(out, x + halfStep, yEnd, true);
				} else {
					// every odd line move bottom to top
					//moveToPaper(file,x,y,pen up?)]
					moveToPaper(out, x + halfStep, yEnd, true);

					for (y = yEnd; y >= yStart; y -= zigZagSpacing) {
						// read a block of the image and find the average intensity in this block
						z = sampleScale(img, x - halfStep, y - zigZagSpacing, x + halfStep, y + zigZagSpacing);
						// scale the intensity value
						scale_z = (255.0f - z) / 255.0f;
						//scale_z *= scale_z;  // quadratic curve
						pulse_size = halfStep * scale_z;
						moveToPaper(out, (x + halfStep + pulse_size * n), y, pulse_size < PULSE_MINIMUM);
						n *= -1;
					}
					moveToPaper(out, x + halfStep, yStart, true);
				}
			}
		}
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
