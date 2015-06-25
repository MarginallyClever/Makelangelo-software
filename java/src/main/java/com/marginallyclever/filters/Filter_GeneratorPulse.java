package com.marginallyclever.filters;


import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Filter_GeneratorPulse extends Filter {
	float blockScale=4.0f;
	int direction=0;
	
	public Filter_GeneratorPulse(MainGUI gui, MachineConfiguration mc,
			MultilingualSupport ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() { return translator.get("PulseLineName"); }

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out,float x,float y,boolean up) throws IOException {
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
		tool.writeMoveTo(out, TX(x), TY(y));
	}
	
	// sample the pixels from x0,y0 (top left) to x1,y1 (bottom right)
	protected int takeImageSampleBlock(BufferedImage img,int x0,int y0,int x1,int y1) {
		// point sampling
		int value=0;
		int sum=0;
		
		if(x0<0) x0=0;
		if(x1>image_width-1) x1 = image_width-1;
		if(y0<0) y0=0;
		if(y1>image_height-1) y1 = image_height-1;

		for(int y=y0;y<y1;++y) {
			for(int x=x0;x<x1;++x) {
				value += sample1x1(img,x, y);
				++sum;
			}
		}

		if(sum==0) return 255;
		
		return value/sum;
	}
	
	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 * @param img the image to convert.
	 */
	@Override
	public void convert(BufferedImage img) throws IOException {
		final JTextField field_size = new JTextField(Float.toString(blockScale));

		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel(translator.get("HilbertCurveSize")));
		panel.add(field_size);
		
		String [] directions = { "horizontal", "vertical" };
		final JComboBox<String> direction_choices = new JComboBox<String>(directions);
		panel.add(direction_choices);
		
	    int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
	    	blockScale = Float.parseFloat(field_size.getText());
	    	direction = direction_choices.getSelectedIndex();
			convertNow(img);
	    }
	}
	
	private void convertNow(BufferedImage img) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI,machine,translator,255);
		img = bw.process(img);

		// Open the destination file
        try(
        final OutputStream fileOutputStream = new FileOutputStream(dest);
        final Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        ) {
            // Set up the conversion from image space to paper space, select the current tool, etc.
            imageStart(img, out);
            // "please change to tool X and press any key to continue"
            tool.writeChangeTo(out);
            // Make sure the pen is up for the first move
            liftPen(out);


            // figure out how many lines we're going to have on this image.
            int steps = (int) Math.ceil(tool.getDiameter() / (1.75 * scale));
            if (steps < 1) steps = 1;

            int blockSize = (int) (steps * blockScale);
            float halfstep = (float) blockSize / 2.0f;

            // from top to bottom of the image...
            int x, y, z, i = 0, k = 0;

            if (direction == 0) {
                // horizontal
                for (y = 0; y < image_height; y += blockSize) {
                    ++i;

                    if ((i % 2) == 0) {
                        // every even line move left to right
                        //MoveTo(file,x,y,pen up?)]
                        moveTo(out, (float) 0, (float) y + halfstep, true);

                        for (x = 0; x < image_width; x += blockSize) {
                            // read a block of the image and find the average intensity in this block
                            z = takeImageSampleBlock(img, x, (int) (y - halfstep), x + blockSize, (int) (y + halfstep));
                            // scale the intensity value
                            float scale_z = (255.0f - (float) z) / 255.0f;
                            //scale_z *= scale_z;  // quadratic curve
                            float pulse_size = halfstep * scale_z;
                            if (pulse_size < 0.5f) {
                                moveTo(out, x, y + halfstep, true);
                                moveTo(out, x + blockSize, y + halfstep, true);
                            } else {
                                int finalx = x + blockSize;
                                if (finalx >= image_width) finalx = image_width - 1;
                                // fill the same block in the output image with a heartbeat monitor zigzag.
                                // the height of the pulse is relative to the intensity.
                                moveTo(out, (float) (x), (float) (y + halfstep), false);
                                ++k;
                                for (int block_x = x; block_x <= finalx; block_x += steps) {
                                    float n = 1 + (k % 2) * -2;
                                    ++k;
                                    moveTo(out, (float) (block_x), (float) (y + halfstep + pulse_size * n), false);
                                }
                                moveTo(out, (float) (finalx), (float) (y + halfstep), false);
                            }
                        }
                        moveTo(out, (float) image_width, (float) y + halfstep, true);
                    } else {
                        // every odd line move right to left
                        //MoveTo(file,x,y,pen up?)]
                        moveTo(out, (float) image_width, (float) y + halfstep, true);

                        for (x = image_width; x >= 0; x -= blockSize) {
                            // read a block of the image and find the average intensity in this block
                            z = takeImageSampleBlock(img, x - blockSize, (int) (y - halfstep), x, (int) (y + halfstep));
                            // scale the intensity value
                            float scale_z = (255.0f - (float) z) / 255.0f;
                            //scale_z *= scale_z;  // quadratic curve
                            float pulse_size = halfstep * scale_z;
                            if (pulse_size < 0.5f) {
                                moveTo(out, x, y + halfstep, true);
                                moveTo(out, x - blockSize, y + halfstep, true);
                            } else {
                                int finalx = x - blockSize;
                                if (finalx < 0) finalx = 0;
                                // fill the same block in the output image with a heartbeat monitor zigzag.
                                // the height of the pulse is relative to the intensity.
                                moveTo(out, (float) (x), (float) (y + halfstep), false);
                                ++k;
                                for (int block_x = x; block_x >= finalx; block_x -= steps) {
                                    float n = 1 + (k % 2) * -2;
                                    ++k;
                                    moveTo(out, (float) (block_x), (float) (y + halfstep + pulse_size * n), false);
                                }
                                moveTo(out, (float) (finalx), (float) (y + halfstep), false);
                            }
                        }
                        moveTo(out, (float) 0, (float) y + halfstep, true);
                    }
                }
            } else {
                // vertical
                for (x = 0; x < image_width; x += blockSize) {
                    ++i;

                    if ((i % 2) == 0) {
                        // every even line move top to bottom
                        //MoveTo(file,x,y,pen up?)]
                        moveTo(out, (float) x + halfstep, (float) 0, true);

                        for (y = 0; y < image_height; y += blockSize) {
                            // read a block of the image and find the average intensity in this block
                            //z=takeImageSampleBlock(img,x,(int)(y-halfstep),x+blockSize,(int)(y+halfstep));
                            z = takeImageSampleBlock(img, (int) (x - halfstep), y, (int) (x + halfstep), (int) (y + blockSize));
                            // scale the intensity value
                            float scale_z = (255.0f - (float) z) / 255.0f;
                            //scale_z *= scale_z;  // quadratic curve
                            float pulse_size = halfstep * scale_z;
                            if (pulse_size < 0.5f) {
                                moveTo(out, x + halfstep, y, true);
                                moveTo(out, x + halfstep, y + blockSize, true);
                            } else {
                                int finaly = y + blockSize;
                                if (finaly >= image_height) finaly = image_height - 1;
                                // fill the same block in the output image with a heartbeat monitor zigzag.
                                // the height of the pulse is relative to the intensity.
                                moveTo(out, (float) (x + halfstep), (float) (y), false);
                                ++k;
                                for (int block_y = y; block_y <= finaly; block_y += steps) {
                                    float n = 1 + (k % 2) * -2;
                                    ++k;
                                    moveTo(out, (float) (x + halfstep + pulse_size * n), (float) (block_y), false);
                                }
                                moveTo(out, (float) (x + halfstep), (float) (finaly), false);
                            }
                        }
                        moveTo(out, (float) x + halfstep, (float) image_height, true);
                    } else {
                        // every odd line move bottom to top
                        //MoveTo(file,x,y,pen up?)]
                        moveTo(out, (float) x + halfstep, (float) image_height, true);

                        for (y = image_height; y >= 0; y -= blockSize) {
                            // read a block of the image and find the average intensity in this block
                            z = takeImageSampleBlock(img, (int) (x - halfstep), y - blockSize, (int) (x + halfstep), y);
                            // scale the intensity value
                            float scale_z = (255.0f - (float) z) / 255.0f;
                            //scale_z *= scale_z;  // quadratic curve
                            float pulse_size = halfstep * scale_z;
                            if (pulse_size < 0.5f) {
                                moveTo(out, x + halfstep, y, true);
                                moveTo(out, x + halfstep, y - blockSize, true);
                            } else {
                                int finaly = y - blockSize;
                                if (finaly < 0) finaly = 0;
                                // fill the same block in the output image with a heartbeat monitor zigzag.
                                // the height of the pulse is relative to the intensity.
                                moveTo(out, (float) (x + halfstep), (float) (y), false);
                                ++k;
                                for (int block_y = y; block_y >= finaly; block_y -= steps) {
                                    float n = 1 + (k % 2) * -2;
                                    ++k;
                                    moveTo(out, (float) (x + halfstep + pulse_size * n), (float) (block_y), false);
                                }
                                moveTo(out, (float) (x + halfstep), (float) (finaly), false);
                            }
                        }
                        moveTo(out, (float) x + halfstep, (float) 0, true);
                    }
                }
            }

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