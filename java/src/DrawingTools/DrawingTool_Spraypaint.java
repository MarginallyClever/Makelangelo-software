package DrawingTools;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStreamWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Makelangelo.MachineConfiguration;
import Makelangelo.Makelangelo;


public class DrawingTool_Spraypaint extends DrawingTool {
	boolean is_up;
	float old_x,old_y;
	float overlap=0.3f;
	
	public DrawingTool_Spraypaint() {
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

	public void WriteOn(OutputStreamWriter out) throws IOException {
		is_up=false;
	}

	public void WriteOff(OutputStreamWriter out) throws IOException {
		is_up=true;
	}
		
	public void WriteMoveTo(OutputStreamWriter out,float x,float y) throws IOException {
		if(is_up) {
			out.write("G00 X"+x+" Y"+y+";\n");			
		} else {
			// TODO make this into a set of dots
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
				super.WriteOn(out);
				super.WriteOff(out);	
			}
			d=len-r;
			 px = old_x + dx * d/len;
			 py = old_y + dy * d/len;		
			out.write("G00 X"+px+" Y"+py+" F"+feed_rate+";\n");	
			super.WriteOn(out);
			super.WriteOff(out);
		}
		old_x=x;
		old_y=y;
	}
	
	public void Adjust() {
		final JDialog driver = new JDialog(Makelangelo.getSingleton().getParentFrame(),"Adjust Spraypaint",true);
		driver.setLayout(new GridBagLayout());

		final JTextField penDiameter   = new JTextField(Float.toString(diameter),5);
		final JTextField penFeedRate   = new JTextField(Float.toString(feed_rate),5);
		
		final JTextField penUp   = new JTextField(Float.toString(z_off),5);
		final JTextField penDown = new JTextField(Float.toString(z_on),5);
		final JTextField penz = new JTextField(Float.toString(z_rate),5);
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

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel("Diameter"),c);
		d.gridx=1;	d.gridy=y;	driver.add(penDiameter,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel("Max feed rate"),c);
		d.gridx=1;	d.gridy=y;	driver.add(penFeedRate,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel("Up"),c);
		d.gridx=1;	d.gridy=y;	driver.add(penUp,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel("Down"),c);
		d.gridx=1;	d.gridy=y;	driver.add(penDown,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel("Servo speed"),c);
		d.gridx=1;	d.gridy=y;	driver.add(penz,d);
		++y;

		c.gridx=0;	c.gridy=y;	driver.add(new JLabel("Make a dot"),c);
		d.gridx=1;	d.gridy=y;	driver.add(buttonTestDot,d);
		++y;
	
		c.gridx=1;	c.gridy=y;	driver.add(buttonSave,c);
		c.gridx=2;	c.gridy=y;	driver.add(buttonCancel,c);
		++y;
	
		c.gridwidth=2;
		c.insets=new Insets(0,5,5,5);
		c.anchor=GridBagConstraints.WEST;
		
		c.gridheight=4;
		c.gridx=0;  c.gridy=y;
		driver.add(new JTextArea("Adjust the values sent to the servo to\n" +
								 "raise and lower the pen."),c);
		
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				
				if(subject == buttonTestDot) {
					Makelangelo.getSingleton().SendLineToRobot("G00 Z"+penUp.getText()+" F"+penz.getText());
					Makelangelo.getSingleton().SendLineToRobot("G00 Z"+penDown.getText()+" F"+penz.getText());
				}
				if(subject == buttonSave) {
					diameter = Float.valueOf(penDiameter.getText());
					feed_rate = Float.valueOf(penFeedRate.getText());
					z_off = Float.valueOf(penUp.getText());
					z_on = Float.valueOf(penDown.getText());
					z_rate = Float.valueOf(penz.getText());
					MachineConfiguration.getSingleton().SaveConfig();
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
	
		Makelangelo.getSingleton().SendLineToRobot("M114");
		driver.pack();
		driver.setVisible(true);
	}
}
