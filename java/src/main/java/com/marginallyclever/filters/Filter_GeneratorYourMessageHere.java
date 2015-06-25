package com.marginallyclever.filters;


import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;


public class Filter_GeneratorYourMessageHere extends Filter {
	protected float kerning=-0.50f;
	protected float letter_width=2.0f;
	protected float letter_height=2.0f;
	protected float line_spacing=0.5f;
	protected float margin=1.0f;
	static final String alphabetFolder = new String("ALPHABET/");
	protected int chars_per_line=35;
	protected static String lastMessage = "";

    private final Logger logger = LoggerFactory.getLogger(Filter_GeneratorYourMessageHere.class);

	public Filter_GeneratorYourMessageHere(MainGUI gui,
			MachineConfiguration mc, MultilingualSupport ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() { return translator.get("YourMsgHereName"); }
	
	public void generate(String dest) {
		final JTextArea text = new JTextArea(lastMessage,6,60);
	
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JScrollPane(text));
		
	    int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
			lastMessage = text.getText();
			createMessage(lastMessage,dest);
			
			// TODO Move to GUI?
			mainGUI.log("<font color='green'>Completed.</font>\n");
	    }
	}

	protected void createMessage(String str,String dest) {

        try (final OutputStream fileOutputStream = new FileOutputStream(dest);
             final Writer output = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

			tool = machine.getCurrentTool();
			setupTransform();
			output.write(machine.getConfigLine()+";\n");
			output.write(machine.getBobbinLine()+";\n");
			tool.writeChangeTo(output);
			
			textSetAlign(Align.CENTER);
			textSetVAlign(VAlign.MIDDLE);
			textCreateMessageNow(lastMessage,output);

			textSetAlign(Align.RIGHT);
			textSetVAlign(VAlign.TOP);
			textSetPosition(image_width,image_height);
			textCreateMessageNow("Makelangelo #" + Long.toString(machine.getUID()), output);
		} catch(IOException e) {
            logger.error("{}", e);
        }
	}
}
