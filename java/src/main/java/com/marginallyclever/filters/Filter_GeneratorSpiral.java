package com.marginallyclever.filters;


import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
 * @author Dan
 */
public class Filter_GeneratorSpiral extends Filter {
	
	@Override
	public String getName() { return translator.get("SpiralName"); }
	
	boolean whole_image = false;  // draw the spiral right out to the edges of the square bounds.
	
	
	public Filter_GeneratorSpiral(MainGUI gui,MachineConfiguration mc,MultilingualSupport ms) {
		super(gui,mc,ms);
	}
	
	
	/**
	 * Overrides teh basic MoveTo() because optimizing for spirals is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out,float x,float y,boolean up) throws IOException {
		tool.writeMoveTo(out, TX(x), TY(y));
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
	}
	
	
	/**
	 * The main entry point
	 * @param img the image to convert.
	 */
	@Override
	public void convert(BufferedImage img) throws IOException {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI,machine,translator,255); 
		img = bw.process(img);

        try(
        final OutputStream fileOutputStream = new FileOutputStream(dest);
        final Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        ) {

            imageStart(img, out);

            double toolDiameter = tool.getDiameter() / scale;
            tool.writeChangeTo(out);
            liftPen(out);

            //*
            // create a spiral across the image
            // raise and lower the pen to darken the appropriate areas

            // spiralize
            int x, y, i, j;
            final int steps = 4;
            double leveladd = 255.0 / 5.0f;
            double level;
            int z = 0;

            float maxr;
            if (whole_image) {
                // go right to the corners
                maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
            } else {
                // do the largest circle that still fits in the image.
                maxr = (h2 > w2) ? w2 : h2;
            }
            maxr /= 2;

            float r = maxr, d, f;
            float fx, fy;
            int numRings = 0;
            double[] each_level = new double[steps];
            each_level[0] = leveladd * 1;
            each_level[1] = leveladd * 3;
            each_level[2] = leveladd * 2;
            each_level[3] = leveladd * 4;
            j = 0;
            while (r > toolDiameter) {
                d = r * 2.0f;
                ++j;
                level = each_level[j % steps];
                // find circumference of current circle
                float circumference = (float) Math.floor((2.0f * d - toolDiameter) * Math.PI);
                if (circumference > 360.0f) circumference = 360.0f;

                for (i = 0; i <= circumference; ++i) {
                    f = (float) Math.PI * 2.0f * (i / circumference);
                    fx = w2 + (float) (Math.cos(f) * d);
                    fy = h2 + (float) (Math.sin(f) * d);
                    x = (int) fx;
                    y = (int) fy;
                    // clip to image boundaries
                    if (x >= 0 && x < image_width && y >= 0 && y < image_height) {
                        z = sample3x3(img, x, y);
                        moveTo(out, fx, fy, (z >= level));
                    } else {
                        moveTo(out, fx, fy, true);
                    }
                }
                r -= toolDiameter * 0.5;
                ++numRings;
            }

            mainGUI.log("<font color='yellow'>" + numRings + " rings.</font>\n");

            liftPen(out);
            signName(out);
            tool.writeMoveTo(out, 0, 0);
        }
    }
}


/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */