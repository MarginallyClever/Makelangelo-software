package com.marginallyclever.filters;


import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;


public class Filter_GeneratorSandy extends Filter {
  float blockScale=50.0f;
  int direction=0;
  
  public Filter_GeneratorSandy(MainGUI gui, MakelangeloRobot mc,
      MultilingualSupport ms) {
    super(gui, mc, ms);
  }

  @Override
  public String getName() { return translator.get("SandyNobleName"); }

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
    
    String [] directions = { "top right", "top left", "bottom left", "bottom right", "center" };
    final JComboBox<String> direction_choices = new JComboBox<>(directions);
    panel.add(direction_choices);
    
    int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      blockScale = Float.parseFloat(field_size.getText());
      direction = direction_choices.getSelectedIndex();
      convertNow(img);
    }
  }

  
  double xStart,yStart;
  double xEnd,yEnd;
  double paperWidth,paperHeight;
    
  protected int sampleScale(BufferedImage img,double x0,double y0,double x1,double y1) {
    return sample(img,
        (x0-xStart)/(xEnd-xStart) * (double)image_width,
        (double)image_height - (y1-yStart)/(yEnd-yStart) * (double)image_height,
        (x1-xStart)/(xEnd-xStart) * (double)image_width,
        (double)image_height - (y0-yStart)/(yEnd-yStart) * (double)image_height
        );
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
   * Converts images into zigzags in paper space instead of image space
   * @param img the buffered image to convert
   * @throws IOException couldn't open output file
   */
  private void convertNow(BufferedImage img) throws IOException {
      // make black & white
      Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI, machine, translator, 255);
      img = bw.process(img);
      

	    mainGUI.log("<font color='green'>Converting to gcode and saving "+dest+"</font>\n");
	        try(
	        final OutputStream fileOutputStream = new FileOutputStream(dest);
	        final Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
	        ) {

	            imageStart(img, out);

	            // set absolute coordinates
	            out.write("G00 G90;\n");
	            tool.writeChangeTo(out);
	            liftPen(out);

//	            convertImageSpace(img, out);
	    		convertPaperSpace(img,out);

	            liftPen(out);
	            signName(out);
	            moveTo(out, 0, 0, true);
	        }
  }
  
  
  private boolean isInsideLimits(double x,double y) {
	  if(x<xStart) return false;
	  if(x>=xEnd) return false;
	  if(y<yStart) return false;
	  if(y>=yEnd) return false;
	  return true;
  }
  
  
  private void convertPaperSpace(BufferedImage img,Writer out) throws IOException {
    // if the image were projected on the paper, where would the top left corner of the image be in paper space?
    // image(0,0) is (-paperWidth/2,-paperHeight/2)*paperMargin
    
    paperWidth = machine.getPaperWidth();
    paperHeight = machine.getPaperHeight();
    
    xStart = -paperWidth/2.0;
    yStart = xStart * (double)image_height/(double)image_width;

    if(yStart < -(paperHeight/2.0)) {
      xStart *= (-(paperHeight/2.0)) / yStart;      
      yStart = -(paperHeight/2.0);
    }

    xStart *= 10.0* machine.paperMargin;
    yStart *= 10.0* machine.paperMargin;
    xEnd = -xStart;
    yEnd = -yStart;
    
    double PULSE_MINIMUM=0.5;

    // from top to bottom of the image...
    double x, y, z, scale_z, pulse_size;
    
    
    double dx = xStart - machine.limit_right*10; 
    double dy = yStart - machine.limit_top*10;
    double r_max = Math.sqrt(dx*dx+dy*dy);
    double r_min = 0;
    
	double cx,cy;
	
	switch(direction) {
	case 0:
		cx = machine.limit_right*10;
		cy = machine.limit_top*10;
		break;
	case 1:
		cx = machine.limit_left*10;
		cy = machine.limit_top*10;
		break;
	case 2:
		cx = machine.limit_left*10;
		cy = machine.limit_bottom*10;
		break;
	case 3:
		cx = machine.limit_left*10;
		cy = machine.limit_bottom*10;
		break;
	default:
		cx = 0;
		cy = 0;
		break;
	}

    double r_step = (r_max-r_min)/blockScale;
    double r;
	double t_dir=1;
	double pulse_flip=1;
	double x2,y2,t,t_step, t_step2;
	double last_x=0,last_y=0;
	boolean was_drawing=true;
    
    for(r=r_min;r<r_max;r+=r_step) {
    	// go around in a circle
    	t=0;
    	t_step = tool.getDiameter()/r;
    	for(t=0;t<Math.PI*2;t+=t_step2) {
    		dx = Math.cos(t_dir *t);
    		dy = Math.sin(t_dir *t);
	    	x = cx + dx * r;
	    	y = cy + dy * r;
	    	t_step2=t_step;
		    if(!isInsideLimits(x,y)) {
		    	if(was_drawing) {
		    		moveToPaper(out,last_x,last_y,true);
		    		was_drawing=false;
		    	}
		    	continue;
		    }
		    
		    last_x=x;
		    last_y=y;
            // read a block of the image and find the average intensity in this block
            z = sampleScale( img, x-r_step/2, y-r_step/2,x+r_step/2,y + r_step/2 );
            // scale the intensity value
            assert(z>=0);
            assert(z<=255.0);
            scale_z = (255.0 -  z) / 255.0;
            scale_z = 1-scale_z;
            pulse_size = r_step*0.5;//r_step * 0.6 * scale_z;
	    	t_step2=t_step*pulse_size*scale_z;
	    	if(t_step2<t_step) t_step2=t_step;
            
	    	x2 = x + dx * pulse_size*pulse_flip;
	    	y2 = y + dy * pulse_size*pulse_flip;
	    	pulse_flip=-pulse_flip;
	    	if(was_drawing == false) {
	    		moveToPaper(out,last_x,last_y,pulse_size<PULSE_MINIMUM);
	    		was_drawing=true;
	    	}
    		moveToPaper(out,x2,y2,pulse_size<PULSE_MINIMUM);
	    	x2 = x + dx * pulse_size*pulse_flip;
	    	y2 = y + dy * pulse_size*pulse_flip;
    		moveToPaper(out,x2,y2,pulse_size<PULSE_MINIMUM);
    	}
    	t_dir=-t_dir;
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