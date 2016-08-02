package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettings;

public class PanelAdjustPaper
extends JPanel
implements ActionListener, PropertyChangeListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 519519372661103125L;

	protected MakelangeloRobot robot;

	private JComboBox<String> paperSizes;
	private JFormattedTextField pw, ph;
	private JCheckBox isLandscape;
	private boolean beingModified;
	private String commonPaperSizes [] = {
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
		"A7 (74 x 105)",
		"US Half Letter (140 x 216)",
		"US Letter (216 x 279)",
		"US Legal (216 x 356)",
		"US Junior Legal (127 x 203)",
		"US Ledger / Tabloid (279 x 432)",
		"ANSI A (216 x 279)",
		"ANSI B (279 x 432)",
		"ANSI C (432 x 559)",
		"ANSI D (559 x 864)",
		"ANSI E (864 x 1118)",
		"Arch A (229 x 305)",
		"Arch B (305 x 457)",
		"Arch C (457 x 610)",
		"Arch D (610 x 914)",
		"Arch E (914 x 1219)",
		"Arch E1 (762 x 1067)",
		};

	private JSlider paperMargin;

	public PanelAdjustPaper(MakelangeloRobot robot) {
		this.robot = robot;
		
		beingModified=false;
		
	    this.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    
	    
		JPanel p = new JPanel(new GridBagLayout());
		this.add(p);
		int y=0;

		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.WEST;
		c.anchor = GridBagConstraints.EAST;
		c.ipadx=5;
		c.ipady=2;

		// common paper sizes
		paperSizes = new JComboBox<>(commonPaperSizes);
		c.gridx=0;  c.gridy=y;  p.add(new JLabel(Translator.get("PaperSize")),c);
		d.gridx=1;  d.gridy=y;  d.gridwidth=2;  p.add(paperSizes,d);
		y++;

		// landscape checkbox
		isLandscape = new JCheckBox(Translator.get("isLandscape"), false);
		c.fill = GridBagConstraints.HORIZONTAL;
		d.gridx=0;  d.gridy=y;  d.gridwidth=3;  p.add(isLandscape,d);
		y++;
		c.fill = GridBagConstraints.NONE;

		// manual paper size settings
		NumberFormat nFloat = NumberFormat.getIntegerInstance();

		d.gridwidth=1;
		pw = new JFormattedTextField(nFloat);
		c.gridx=0;  c.gridy=y;  p.add(Box.createGlue(),c);
		d.gridx=1;  d.gridy=y;  p.add(pw,d); 
		d.gridx=2;  d.gridy=y;  p.add(new JLabel(Translator.get("Millimeters")),d);
		y++;
		
		ph = new JFormattedTextField(nFloat);
		c.gridx=0;  c.gridy=y;  p.add(new JLabel(" x "),c);
		d.gridx=1;  d.gridy=y;  p.add(ph,d);
		d.gridx=2;  d.gridy=y;  p.add(new JLabel(Translator.get("Millimeters")),d);
		y++;

		Dimension s = ph.getPreferredSize();
		s.width = 80;
		pw.setPreferredSize(s);
		ph.setPreferredSize(s);
		
		// paper margin
		JPanel marginPanel = new JPanel(new GridLayout(2, 1));
		paperMargin = new JSlider(JSlider.HORIZONTAL, 0, 50, 100 - (int) (robot.getSettings().getPaperMargin() * 100));
		paperMargin.setMajorTickSpacing(10);
		paperMargin.setMinorTickSpacing(5);
		paperMargin.setPaintTicks(false);
		paperMargin.setPaintLabels(true);
		marginPanel.add(new JLabel(Translator.get("PaperMargin")));
		marginPanel.add(paperMargin);
		c.gridwidth=3;
		c.gridx=0;  c.gridy=y;  p.add(marginPanel, c);
		y++;

		paperSizes.addActionListener(this);
		isLandscape.addActionListener(this);
		pw.addPropertyChangeListener(this);
		ph.addPropertyChangeListener(this);
		paperMargin.addChangeListener(this);
		
		updateValues();
	}

	
	public void stateChanged(ChangeEvent e) {}


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
		if(beingModified) return;

		beingModified=true;
		if( e.getSource() == pw || e.getSource() == ph ) {
			double w=0;
			double h=0;
			try {
				w = ((Number)pw.getValue()).doubleValue();
				h = ((Number)ph.getValue()).doubleValue();
			} catch(Exception err) {
				err.getMessage();
			}

			int i = getCurrentPaperSizeChoice( h, w );
			if(i!=0) {
				isLandscape.setSelected(true);
			} else {
				i = getCurrentPaperSizeChoice( w, h );
				isLandscape.setSelected(false);
			}
			paperSizes.setSelectedIndex(i);
		}
		beingModified=false;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		if(beingModified) return;
		beingModified=true;
		
		if(subject == paperSizes) {
			final int selectedIndex = paperSizes.getSelectedIndex();
			if(selectedIndex!= 0) {
				String str = paperSizes.getItemAt(selectedIndex);
				String sw = str.substring(str.indexOf('(')+1, str.indexOf('x')).trim();
				String sh = str.substring(str.indexOf('x')+1, str.indexOf(')')).trim();
				if(isLandscape.isSelected()) {
					String temp = sw;
					sw = sh;
					sh = temp;
				}
				pw.setText(sw);
				ph.setText(sh);
			}
		}
		if( subject == isLandscape ) {
			String sw = pw.getText();
			String sh = ph.getText();
			pw.setText(sh);
			ph.setText(sw);
		}
		
		beingModified=false;
	}

	public void updateValues() {
		if(robot==null) return;
		
		double w = robot.getSettings().getPaperWidth()*10;
		double h = robot.getSettings().getPaperHeight()*10;

		beingModified=true;
		
		int i = getCurrentPaperSizeChoice( h, w );
		if(i!=0) {
			isLandscape.setSelected(true);
		} else {
			i = getCurrentPaperSizeChoice( w, h );
			isLandscape.setSelected(false);
		}
		paperSizes.setSelectedIndex(i);
		pw.setValue(w);
		ph.setValue(h);

		beingModified=false;
	}
	
	public void save() {
		double pwf = Double.valueOf(pw.getText()) / 10.0;
		double phf = Double.valueOf(ph.getText()) / 10.0;
		
		boolean data_is_sane=true;
		if( pwf<=0 ) data_is_sane=false;
		if( phf<=0 ) data_is_sane=false;

		if (data_is_sane) {
			MakelangeloRobotSettings s = robot.getSettings();
			s.setPaperSize(pwf,phf);

			double pm = (100 - paperMargin.getValue()) * 0.01;
			s.setPaperMargin(pm);
		}
	}
}
