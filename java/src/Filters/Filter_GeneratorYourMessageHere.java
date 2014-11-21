package Filters;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import Makelangelo.MachineConfiguration;
import Makelangelo.Makelangelo;


public class Filter_GeneratorYourMessageHere extends Filter {
	protected float kerning=-0.50f;
	protected float letter_width=2.0f;
	protected float letter_height=2.0f;
	protected float line_spacing=0.5f;
	protected float margin=1.0f;
	static final String alphabetFolder = new String("ALPHABET/");
	protected int chars_per_line=35;
	protected static String lastMessage = "";

	public String GetName() { return "Your message here"; }
	
	
	public void Generate(String dest) {
		final JDialog driver = new JDialog(Makelangelo.getSingleton().getParentFrame(),"Text To GCODE",true);
		driver.setLayout(new GridLayout(0,1));

		final JTextArea text = new JTextArea(lastMessage,60,6);
		final JButton buttonSave = new JButton("Go");
		final JButton buttonCancel = new JButton("Cancel");
		final String dest2 = dest;

		driver.add(new JScrollPane(text));
		
		Box horizontalBox = Box.createHorizontalBox();
	    horizontalBox.add(Box.createGlue());
	    horizontalBox.add(buttonSave);
	    horizontalBox.add(buttonCancel);
	    driver.add(horizontalBox);
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				
				if(subject == buttonSave) {
					lastMessage = text.getText();
					CreateMessage(lastMessage,dest2);
					
					// TODO Move to GUI?
					Makelangelo.getSingleton().Log("<font color='green'>Completed.</font>\n");
					Makelangelo.getSingleton().PlayConversionFinishedSound();
					Makelangelo.getSingleton().LoadGCode(dest2);

					driver.dispose();
				}
				if(subject == buttonCancel) {
					driver.dispose();
				}
			}
		};
		
		buttonSave.addActionListener(driveButtons);
		buttonCancel.addActionListener(driveButtons);
		driver.getRootPane().setDefaultButton(buttonSave);

		driver.setSize(300,100);
		driver.setVisible(true);
	}

	protected void CreateMessage(String str,String dest) {
		//System.out.println("output file = "+outputFile);

		try {
			OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");

			MachineConfiguration mc = MachineConfiguration.getSingleton();
			tool = mc.GetCurrentTool();
			SetupTransform();
			output.write(mc.GetConfigLine()+";\n");
			output.write(mc.GetBobbinLine()+";\n");
			tool.WriteChangeTo(output);
			
			TextSetAlign(Align.CENTER);
			TextSetVAlign(VAlign.MIDDLE);
			TextCreateMessageNow(lastMessage,output);

			TextSetAlign(Align.RIGHT);
			TextSetVAlign(VAlign.TOP);
			TextSetPosition(image_width,image_height);
			TextCreateMessageNow("Makelangelo #"+Long.toString(MachineConfiguration.getSingleton().GetUID()),output);
			
			output.close();
		}
		catch(IOException ex) {}
	}
}
