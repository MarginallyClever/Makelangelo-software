package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;

public class PanelAdjustPaper
extends JPanel
implements ActionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 519519372661103125L;

	protected JComboBox<String> paperSizes;
	protected JFormattedTextField pw, ph;

	MakelangeloRobot robot;

	public PanelAdjustPaper(Translator translator,MakelangeloRobot robot) {
		this.robot = robot;

	    this.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    
	    
		JPanel p = new JPanel(new GridBagLayout());
		this.add(p);
		int y=0;
		paperSizes = new JComboBox<>(robot.settings.commonPaperSizes);
		paperSizes.setSelectedIndex(robot.settings.getCurrentPaperSizeChoice( robot.settings.getPaperWidth()*10, robot.settings.getPaperHeight()*10 ));

		NumberFormat nFloat = NumberFormat.getIntegerInstance();
		pw = new JFormattedTextField(nFloat);
		pw.setValue(robot.settings.getPaperWidth()*10);
		ph = new JFormattedTextField(nFloat);
		ph.setValue(robot.settings.getPaperHeight()*10);

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		c.ipadx=5;
		c.ipady=2;

		c.gridx=0;  c.gridy=y;  p.add(new JLabel(Translator.get("PaperSize")),c);
		d.gridx=1;  d.gridy=y;  d.gridwidth=2;  p.add(paperSizes,d);
		y=1;
		d.gridwidth=1;

		c.gridx=0;  c.gridy=y;  p.add(Box.createGlue(),c);
		d.gridx=1;  d.gridy=y;  p.add(pw,d); 
		d.gridx=2;  d.gridy=y;  p.add(new JLabel(Translator.get("Millimeters")),d);
		y++;
		c.gridx=0;  c.gridy=y;  p.add(new JLabel(" x "),c);
		d.gridx=1;  d.gridy=y;  p.add(ph,d);
		d.gridx=2;  d.gridy=y;  p.add(new JLabel(Translator.get("Millimeters")),d);
		y++;


		Dimension s = ph.getPreferredSize();
		s.width = 80;
		pw.setPreferredSize(s);
		ph.setPreferredSize(s);

		paperSizes.addActionListener(this);
		pw.addKeyListener(this);
		ph.addKeyListener(this);
	}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) { Event(e); }
	public void keyTyped(KeyEvent e) { Event(e); }

	private void Event(KeyEvent e) {
		double w=0;
		double h=0;
		try {
			w = ((Number)pw.getValue()).doubleValue();
			h = ((Number)ph.getValue()).doubleValue();
		} catch(Exception err) {
			err.getMessage();
		}
		paperSizes.setSelectedIndex(robot.settings.getCurrentPaperSizeChoice(w,h));	
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if(subject == paperSizes) {
			final int selectedIndex = paperSizes.getSelectedIndex();
			if(selectedIndex!= 0) {
				String str = paperSizes.getItemAt(selectedIndex);
				String sw = str.substring(str.indexOf('(')+1, str.indexOf('x')).trim();
				String sh = str.substring(str.indexOf('x')+1, str.indexOf(')')).trim();
				pw.setText(sw);
				ph.setText(sh);
			}
		}
	}
    
   public void save() {
   	double pwf = Double.valueOf(pw.getText()) / 10.0;
   	double phf = Double.valueOf(ph.getText()) / 10.0;
   	boolean data_is_sane=true;
   	if( pwf<=0 ) data_is_sane=false;
   	if( phf<=0 ) data_is_sane=false;
   	
   	if (data_is_sane) {
   		robot.settings.setPaperSize(pwf,phf);
   	}
   }
}
