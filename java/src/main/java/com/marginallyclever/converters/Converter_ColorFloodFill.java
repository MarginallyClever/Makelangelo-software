package com.marginallyclever.converters;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import com.marginallyclever.basictypes.C3;
import com.marginallyclever.basictypes.ColorPalette;
import com.marginallyclever.basictypes.Point2D;
import com.marginallyclever.filters.Filter_GaussianBlur;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;

/**
 * @author danroyer
 * @since at least 7.1.4
 */
public class Converter_ColorFloodFill extends ImageConverter {
	private ColorPalette palette;
	private int diameter = 0;
	private int last_x, last_y;
	private BufferedImage imgChanged;
	private BufferedImage imgMask;


	public Converter_ColorFloodFill(MakelangeloRobotSettings mc) {
		super(mc);

		palette = new ColorPalette();
		palette.addColor(new C3(0, 0, 0));
		palette.addColor(new C3(255, 0, 0));
		palette.addColor(new C3(0, 255, 0));
		palette.addColor(new C3(0, 0, 255));
		palette.addColor(new C3(255, 255, 255));
	}

	@Override
	public String getName() {
		return Translator.get("RGBFloodFillName");
	}


	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	protected void moveTo(float x, float y, boolean up, Writer osw) throws IOException {
		if (lastUp != up) {
			if (up) liftPen(osw);
			else lowerPen(osw);
			lastUp = up;
		}
		tool.writeMoveTo(osw, TX(x), TY(y));
	}

	/**
	 * test the mask from x0,y0 (top left) to x1,y1 (bottom right) to see if this region has already been visited
	 *
	 * @param x0 left
	 * @param y0 top
	 * @return true if all the pixels in this region are zero.
	 */
	protected boolean getMaskTouched(int x0, int y0) {
		int x1 = x0 + diameter;
		int y1 = y0 + diameter;
		if (x0 < 0) x0 = 0;
		if (x1 > imageWidth - 1) x1 = imageWidth - 1;
		if (y0 < 0) y0 = 0;
		if (y1 > imageHeight - 1) y1 = imageHeight - 1;

		Color value;
		int sum = 0;
		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				++sum;
				value = new Color(imgMask.getRGB(x, y));
				if (value.getRed() != 0) {
					return true;
				}
			}
		}

		return (sum == 0);
	}

	protected void setMaskTouched(int x0, int y0, int x1, int y1) {
		if (x0 < 0) x0 = 0;
		if (x1 > imageWidth - 1) x1 = imageWidth - 1;
		if (y0 < 0) y0 = 0;
		if (y1 > imageHeight - 1) y1 = imageHeight - 1;

		int c = (new C3(255, 255, 255)).toInt();
		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				imgMask.setRGB(x, y, c);
			}
		}
		//imgMask.flush();
	}


	/**
	 * sample the pixels from x0,y0 (top left) to x1,y1 (bottom right) and average the color.
	 *
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return the average color in the region.  if nothing is sampled, return white.
	 */
	protected C3 takeImageSampleBlock(int x0, int y0, int x1, int y1) {
		// point sampling
		C3 value = new C3(0, 0, 0);
		int sum = 0;

		if (x0 < 0) x0 = 0;
		if (x1 > imageWidth - 1) x1 = imageWidth - 1;
		if (y0 < 0) y0 = 0;
		if (y1 > imageHeight - 1) y1 = imageHeight - 1;

		for (int y = y0; y < y1; ++y) {
			for (int x = x0; x < x1; ++x) {
				value.add(new C3(imgChanged.getRGB(x, y)));
				++sum;
			}
		}

		if (sum == 0) return new C3(255, 255, 255);

		return value.mul(1.0f / sum);
	}


	protected boolean doesQuantizedBlockMatch(int color_index, float x, float y) {
		C3 original_color = takeImageSampleBlock((int) x, (int) y, (int) (x + diameter), (int) (y + diameter));
		int quantized_color = palette.quantizeIndex(original_color);
		return (quantized_color == color_index);
	}


	/**
	 * queue-based flood fill
	 *
	 * @param color_index
	 * @throws IOException
	 */
	protected void floodFillBlob(int color_index, int x, int y, Writer osw) throws IOException {
		LinkedList<Point2D> points_to_visit = new LinkedList<>();
		points_to_visit.add(new Point2D(x, y));

		Point2D a;

		while (!points_to_visit.isEmpty()) {
			a = points_to_visit.removeLast();

			if (getMaskTouched((int) a.x, (int) a.y)) continue;
			if (!doesQuantizedBlockMatch(color_index, a.x, a.y)) continue;
			// mark this spot as visited.
			setMaskTouched((int) a.x, (int) a.y, (int) (a.x + diameter), (int) (a.y + diameter));

			// if the difference between the last filled pixel and this one is more than diameter*2, pen up, move, pen down.
			float dx = (float) (a.x - last_x);
			float dy = (float) (a.y - last_y);
			if ((dx * dx + dy * dy) > diameter * diameter * 2.0f) {
				//System.out.print("Jump at "+x+", "+y+"\n");
				moveTo(last_x, last_y, true, osw);
				moveTo(a.x, a.y, true, osw);
				moveTo(a.x, a.y, false, osw);
			} else {
				//System.out.print("Move to "+x+", "+y+"\n");
				moveTo(a.x, a.y, false, osw);
			}
			// update the last position.
			last_x = (int) a.x;
			last_y = (int) a.y;

			//      if( !getMaskTouched((int)(a.x+diameter),(int)a.y           ) )
			points_to_visit.add(new Point2D(a.x + diameter, a.y));
			//      if( !getMaskTouched((int)(a.x-diameter),(int)a.y           ) )
			points_to_visit.add(new Point2D(a.x - diameter, a.y));
			//      if( !getMaskTouched((int)a.x           ,(int)(a.y+diameter)) )
			points_to_visit.add(new Point2D(a.x, a.y + diameter));
			//      if( !getMaskTouched((int)a.x           ,(int)(a.y-diameter)) )
			points_to_visit.add(new Point2D(a.x, a.y - diameter));
		}
	}


	/**
	 * find blobs of color in the original image.  Send that to the flood fill system.
	 *
	 * @param color_index index into the list of colors at the top of the class
	 * @throws IOException
	 */
	void scanForContiguousBlocks(int color_index, Writer osw) throws IOException {
		C3 original_color;
		int quantized_color;

		int x, y;
		int z = 0;

		Log.write("orange", "Palette color " + palette.getColor(color_index).toString() );

		for (y = 0; y < imageHeight; y += diameter) {
			for (x = 0; x < imageWidth; x += diameter) {
				if (getMaskTouched(x, y)) continue;

				original_color = takeImageSampleBlock(x, y, x + diameter, y + diameter);
				quantized_color = palette.quantizeIndex(original_color);
				if (quantized_color == color_index) {
					// found blob
					floodFillBlob(color_index, x, y, osw);
					z++;
					//if(z==20)
					//            return;
				}
			}
		}
		System.out.println("Found " + z + " blobs.");
	}

	private void scanColor(int i, Writer osw) throws IOException {
		// "please change to tool X and press any key to continue"
		tool = machine.getTool(i);
		tool.writeChangeTo(osw);
		// Make sure the pen is up for the first move
		liftPen(osw);

		Log.write("green", "Color " + i );

		scanForContiguousBlocks(i, osw);
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		//Filter_DitherFloydSteinbergRGB bw = new Filter_DitherFloydSteinbergRGB(mainGUI,machine,translator);
		//img = bw.process(img);

		Filter_GaussianBlur blur = new Filter_GaussianBlur(1);
		img = blur.filter(img);
		//    Histogram h = new Histogram();
		//    h.getHistogramOf(img);


		// create a color mask so we don't repeat any pixels
		imgMask = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = imgMask.createGraphics();
		g.setPaint(new Color(0, 0, 0));
		g.fillRect(0, 0, imgMask.getWidth(), imgMask.getHeight());



		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(img, out);


		float pw = (float) machine.getPaperWidth();
		float df = tool.getDiameter() * (float) img.getWidth() / (4.0f * pw);
		if (df < 1) df = 1;

		//    float steps = img.getWidth() / df;

		//System.out.println("Diameter = "+df);
		//System.out.println("Steps = "+steps);

		diameter = (int) df;

		imgChanged = img;

		last_x = img.getWidth() / 2;
		last_y = img.getHeight() / 2;

		scanColor(0, out);  // black
		scanColor(1, out);  // red
		scanColor(2, out);  // green
		scanColor(3, out);  // blue

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
