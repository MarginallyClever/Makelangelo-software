package Filters;


import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import Makelangelo.MachineConfiguration;
import Makelangelo.MainGUI;
import Makelangelo.MultilingualSupport;


public class Filter_GeneratorYourMessageHere extends Filter {
	protected float kerning=-0.50f;
	protected float letter_width=2.0f;
	protected float letter_height=2.0f;
	protected float line_spacing=0.5f;
	protected float margin=1.0f;
	static final String alphabetFolder = new String("ALPHABET/");
	protected int chars_per_line=35;
	protected static String lastMessage = "";

	public Filter_GeneratorYourMessageHere(MainGUI gui,
			MachineConfiguration mc, MultilingualSupport ms) {
		super(gui, mc, ms);
		// TODO Auto-generated constructor stub
	}

	public String GetName() { return "Your message here"; }
	
	public void Generate(String dest) {
		final JTextArea text = new JTextArea(lastMessage,6,60);
	
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JScrollPane(text));
		
	    int result = JOptionPane.showConfirmDialog(null, panel, GetName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
			lastMessage = text.getText();
			CreateMessage(lastMessage,dest);
			
			// TODO Move to GUI?
			mainGUI.Log("<font color='green'>Completed.</font>\n");
			mainGUI.PlayConversionFinishedSound();
			mainGUI.LoadGCode(dest);
	    }
	}

	protected void CreateMessage(String str,String dest) {
		//System.out.println("output file = "+outputFile);

		try {
			OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");

			tool = machine.GetCurrentTool();
			SetupTransform();
			output.write(machine.GetConfigLine()+";\n");
			output.write(machine.GetBobbinLine()+";\n");
			tool.WriteChangeTo(output);
			
			TextSetAlign(Align.CENTER);
			TextSetVAlign(VAlign.MIDDLE);
			TextCreateMessageNow(lastMessage,output);

			TextSetAlign(Align.RIGHT);
			TextSetVAlign(VAlign.TOP);
			TextSetPosition(image_width,image_height);
			TextCreateMessageNow("Makelangelo #"+Long.toString(machine.GetUID()),output);
			
			output.close();
		}
		catch(IOException ex) {}
	}
}
