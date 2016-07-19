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


public class Converter_Sandy extends ImageConverter {
	private static float blockScale=150.0f;
	private static int direction=0;


	@Override
	public String getName() {
		return Translator.get("Sandy Noble Style");
	}

	/**
	 * create horizontal lines across the image.  Raise and lower the pen to darken the appropriate areas
	 * @param img the image to convert.
	 */
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		final JTextField field_size = new JTextField(Float.toString(blockScale));

		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel(Translator.get("HilbertCurveSize")));
		panel.add(field_size);

		String [] directions = { "top right", "top left", "bottom left", "bottom right", "center" };
		final JComboBox<String> direction_choices = new JComboBox<>(directions);
		direction_choices.setSelectedIndex(direction);
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
	 * @param img the buffered image to convert
	 * @throws IOException couldn't open output file
	 */
	private void convertNow(TransformedImage img,Writer out) throws IOException {
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		convertPaperSpace(img,out);

		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}


	private void convertPaperSpace(TransformedImage img,Writer out) throws IOException {
		// if the image were projected on the paper, where would the top left corner of the image be in paper space?
		// image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin

		float yBottom, yTop, xLeft, xRight;

		yBottom = (float)machine.getLimitBottom() * 10;
		yTop    = (float)machine.getLimitTop()    * 10;
		xLeft   = (float)machine.getLimitLeft()   * 10;
		xRight  = (float)machine.getLimitRight()  * 10;
		
		double cx,cy;

		switch(direction) {
		case 0:		cx = xRight;	cy = yTop;		break;
		case 1:		cx = xLeft;		cy = yTop;		break;
		case 2:		cx = xLeft;		cy = yBottom;	break;
		case 3:		cx = xRight;	cy = yBottom;	break;
		default:	cx = 0;			cy = 0;			break;
		}

		double x, y, z, scaleZ;

		double dx = xRight - xLeft; 
		double dy = yTop - yBottom;
		double rMax = Math.sqrt(dx*dx+dy*dy);
		double rMin = 0;

		double rStep = (rMax-rMin)/blockScale;
		double r;
		double t_dir=1;
		double pulseFlip=1;
		double x2,y2,t,t_step;
		double last_x=0,last_y=0;
		boolean wasDrawing=true;
		double flipSum;
		double pulseSize = rStep*0.5;//r_step * 0.6 * scale_z;

		// make concentric circles that get bigger and bigger.
		for(r=rMin;r<rMax;r+=rStep) {
			// go around in a circle
			t=0;
			t_step = tool.getDiameter()/r;
			flipSum=0;
			// go around the circle
			for(t=0;t<Math.PI*2;t+=t_step) {
				dx = Math.cos(t_dir *t);
				dy = Math.sin(t_dir *t);
				x = cx + dx * r;
				y = cy + dy * r;
				if(!isInsidePaperMargins(x,y)) {
					if(wasDrawing) {
						moveTo(out,last_x,last_y,true);
						wasDrawing=false;
					}
					continue;
				}

				last_x=x;
				last_y=y;
				// read a block of the image and find the average intensity in this block
				z = img.sample( x-pulseSize/2.0, y-pulseSize/2.0,x+pulseSize/2.0,y +pulseSize/2.0 );
				// scale the intensity value
				if(z<0) z=0;
				if(z>255) z=255;
				scaleZ = (255.0 -  z) / 255.0;

				if(wasDrawing == false) {
					moveTo(out,last_x,last_y,false);
					wasDrawing=true;
				}

				flipSum+=scaleZ;
				if(flipSum >= 1) {
					flipSum-=1;
					x2 = x + dx * pulseSize*pulseFlip;
					y2 = y + dy * pulseSize*pulseFlip;
					moveTo(out,x2,y2,false);
					pulseFlip = -pulseFlip;
					x2 = x + dx * pulseSize*pulseFlip;
					y2 = y + dy * pulseSize*pulseFlip;
					moveTo(out,x2,y2,false);
				} else {
					x2 = x + dx * pulseSize*pulseFlip;
					y2 = y + dy * pulseSize*pulseFlip;
					moveTo(out,x2,y2,false);
				}
			}
			t_dir=-t_dir;
		}
	}
}


/**
 * This file is part of Makelangelo.
 *
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */