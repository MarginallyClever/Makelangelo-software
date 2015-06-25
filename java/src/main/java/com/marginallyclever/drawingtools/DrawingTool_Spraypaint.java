package com.marginallyclever.drawingtools;


import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;


public class DrawingTool_Spraypaint extends DrawingTool {
	boolean is_up;
	float old_x,old_y;
	float overlap=0.3f;
	
	public DrawingTool_Spraypaint(MainGUI gui,MultilingualSupport ms,MachineConfiguration mc) {
		super(gui,ms,mc);
		
		diameter=40;
		z_rate=80;
		z_on=50;
		z_off=90;
		tool_number=2;
		name="Spray paint";
		feed_rate=3000;
		
		old_x=0;
		old_y=0;
	}

	public void writeOn(Writer out) throws IOException {
		is_up=false;
	}

	public void writeOff(Writer out) throws IOException {
		is_up=true;
	}
		
	public void writeMoveTo(Writer out,float x,float y) throws IOException {
		if(is_up) {
			out.write("G00 X"+x+" Y"+y+";\n");			
		} else {
			// Make a set of dots in a row, instead of a single continuous line
			//out.write("G00 X"+x+" Y"+y+";\n");
			float dx=x-old_x;
			float dy=y-old_y;
			float len=(float)Math.sqrt(dx*dx+dy*dy);
			float step=diameter*(1-overlap);
			float r=step/2;
			float d,px,py;
			
			for( d=r;d<len-r;d+=step) {
				 px = old_x + dx * d/len;
				 py = old_y + dy * d/len;		
				out.write("G00 X"+px+" Y"+py+" F"+feed_rate+";\n");	
				super.writeOn(out);
				super.writeOff(out);	
			}
			d=len-r;
			 px = old_x + dx * d/len;
			 py = old_y + dy * d/len;		
			out.write("G00 X"+px+" Y"+py+" F"+feed_rate+";\n");	
			super.writeOn(out);
			super.writeOff(out);
		}
		old_x=x;
		old_y=y;
	}
	
	public void adjust() {
		final JDialog driver = new JDialog(mainGUI.getParentFrame(),translator.get("spraypaintToolAdjust"),true);
		driver.setLayout(new GridBagLayout());

		final JTextField spraypaintDiameter   = new JTextField(Float.toString(diameter),5);
		final JTextField spraypaintFeedRate   = new JTextField(Float.toString(feed_rate),5);
		
		final JTextField spraypaintUp   = new JTextField(Float.toString(z_off),5);
		final JTextField spraypaintDown = new JTextField(Float.toString(z_on),5);
		final JTextField spraypaintZRate = new JTextField(Float.toString(z_rate),5);
		final JButton buttonTestDot = new JButton("Test");
		final JButton buttonSave = new JButton("Save");
		final JButton buttonCancel = new JButton("Cancel");
	
		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		c.anchor=GridBagConstraints.EAST;
		c.fill=GridBagConstraints.HORIZONTAL;
		d.anchor=GridBagConstraints.WEST;
		d.fill=GridBagConstraints.HORIZONTAL;
		d.weightx=50;
		int y=0;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel(translator.get("spraypaintToolDiameter")),c);
		d.gridx=1;	d.gridy=y;	driver.add(spraypaintDiameter,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel(translator.get("spraypaintToolMaxFeedRate")),c);
		d.gridx=1;	d.gridy=y;	driver.add(spraypaintFeedRate,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel(translator.get("spraypaintToolUp")),c);
		d.gridx=1;	d.gridy=y;	driver.add(spraypaintUp,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel(translator.get("spraypaintToolDown")),c);
		d.gridx=1;	d.gridy=y;	driver.add(spraypaintDown,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel(translator.get("spraypaintToolLiftSpeed")),c);
		d.gridx=1;	d.gridy=y;	driver.add(spraypaintZRate,d);
		++y;
		
		c.gridx=0;	c.gridy=y;	driver.add(new JLabel(translator.get("spraypaintToolTest")),c);
		d.gridx=1;	d.gridy=y;	driver.add(buttonTestDot,d);
		++y;
	
		c.gridx=1;	c.gridy=y;	driver.add(buttonSave,c);
		c.gridx=2;	c.gridy=y;	driver.add(buttonCancel,c);
		++y;
	
		c.gridwidth=2;
		c.insets=new Insets(0,5,5,5);
		c.anchor=GridBagConstraints.WEST;
		/*
		c.gridheight=4;
		c.gridx=0;  c.gridy=y;
		driver.add(new JTextArea("Adjust the values sent to the servo to\n" +
								 "raise and lower the pen."),c);
		*/
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				
				if(subject == buttonTestDot) {
					mainGUI.sendLineToRobot("G00 Z"+spraypaintUp.getText()+" F"+spraypaintZRate.getText());
					mainGUI.sendLineToRobot("G00 Z"+spraypaintDown.getText()+" F"+spraypaintZRate.getText());
				}
				if(subject == buttonSave) {
					diameter = Float.valueOf(spraypaintDiameter.getText());
					feed_rate = Float.valueOf(spraypaintFeedRate.getText());
					z_off = Float.valueOf(spraypaintUp.getText());
					z_on = Float.valueOf(spraypaintDown.getText());
					z_rate = Float.valueOf(spraypaintZRate.getText());
					machine.saveConfig();
					driver.dispose();
				}
				if(subject == buttonCancel) {
					driver.dispose();
				}
			}
		};
		
		buttonTestDot.addActionListener(driveButtons);
		
		buttonSave.addActionListener(driveButtons);
		buttonCancel.addActionListener(driveButtons);
		driver.getRootPane().setDefaultButton(buttonSave);
	
		mainGUI.sendLineToRobot("M114");
		driver.pack();
		driver.setVisible(true);
	}
}
