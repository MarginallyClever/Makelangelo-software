package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustPaper
extends JPanel
implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 519519372661103125L;

	protected JComboBox<String> paperSizes;
	protected JFormattedTextField pw, ph;
	protected JCheckBox isPortrait;

	MakelangeloRobot robot;

	private final String commonPaperSizes [] = {
		"---",
		"4A0 (1682 x 2378)",
		"2A0 (1189 x 1682)",
		"A0 (841 x 1189)",
		"A1 (594 x 841)",
		"A2 (420 x 594)",
		"A3 (297 x 420)",
		"A4 (210 x 297)",
		"A5 (148 x 210)",
		"A6 (105 x 148)",
		"A7 (74 x 105)"
		};

	public PanelAdjustPaper(MakelangeloRobot robot) {
		this.robot = robot;

	    this.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    
	    
		JPanel p = new JPanel(new GridBagLayout());
		this.add(p);
		int y=0;
		paperSizes = new JComboBox<>(commonPaperSizes);
		paperSizes.setSelectedIndex(getCurrentPaperSizeChoice( robot.getSettings().getPaperWidth()*10, robot.getSettings().getPaperHeight()*10 ));

		isPortrait = new JCheckBox(Translator.get("isPortrait"), robot.getSettings().isPortrait());
		
		NumberFormat nFloat = NumberFormat.getIntegerInstance();
		pw = new JFormattedTextField(nFloat);
		pw.setValue(robot.getSettings().getPaperWidth()*10);
		ph = new JFormattedTextField(nFloat);
		ph.setValue(robot.getSettings().getPaperHeight()*10);

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		d.anchor = GridBagConstraints.WEST;
		
		c.ipadx=5;
		c.ipady=2;

		c.gridx=0;  c.gridy=y;  p.add(new JLabel(Translator.get("PaperSize")),c);
		d.gridx=1;  d.gridy=y;  d.gridwidth=2;  p.add(paperSizes,d);
		y++;

		d.gridx=1;  d.gridy=y;  d.gridwidth=3;  p.add(isPortrait,d);
		y++;


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
		pw.addPropertyChangeListener(this);
		ph.addPropertyChangeListener(this);
		isPortrait.addActionListener(this);
	}


	/**
	 * Must match commonPaperSizes
	 * @return
	 */
	public int getCurrentPaperSizeChoice(double pw,double ph) {
		if( pw == 1682 && ph == 2378 ) return 1;
		if( pw == 1189 && ph == 1682 ) return 2;
		if( pw == 841 && ph == 1189 ) return 3;
		if( pw == 594 && ph == 841 ) return 4;
		if( pw == 420 && ph == 594 ) return 5;
		if( pw == 297 && ph == 420 ) return 6;
		if( pw == 210 && ph == 297 ) return 7;
		if( pw == 148 && ph == 210 ) return 8;
		if( pw == 105 && ph == 148 ) return 9;
		if( pw == 74 && ph == 105 ) return 10;

		return 0;
	}

	public void propertyChange(PropertyChangeEvent  e) {
		double w=0;
		double h=0;
		try {
			w = ((Number)pw.getValue()).doubleValue();
			h = ((Number)ph.getValue()).doubleValue();
		} catch(Exception err) {
			err.getMessage();
		}

		boolean isPortrait = false;
		int i = getCurrentPaperSizeChoice(w,h);
		if( i == 0 ) {
			i = getCurrentPaperSizeChoice(h,w);
			isPortrait = ( i != 0 );
		};
		
		robot.getSettings().setPortrait(isPortrait);
		paperSizes.setSelectedIndex(i);	
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		// changing the drop box value
		if(subject == paperSizes) {
			final int selectedIndex = paperSizes.getSelectedIndex();
			if(selectedIndex!= 0) {
				String str = paperSizes.getItemAt(selectedIndex);
				String sw = str.substring(str.indexOf('(')+1, str.indexOf('x')).trim();
				String sh = str.substring(str.indexOf('x')+1, str.indexOf(')')).trim();
				if(robot.getSettings().isPortrait()) {
					String temp = sw;
					sw = sh;
					sh = temp;
				}
				pw.setText(sw);
				ph.setText(sh);
			}
		}
		if( subject == isPortrait ) {
			boolean wasPortrait = robot.getSettings().isPortrait();
			boolean isNowPortrait = isPortrait.isSelected();
			robot.getSettings().setPortrait(isNowPortrait);
			
			if(wasPortrait != isNowPortrait) {
				String sh = pw.getText();
				String sw = ph.getText();
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
			robot.getSettings().setPaperSize(pwf,phf);
		}
	}
}
