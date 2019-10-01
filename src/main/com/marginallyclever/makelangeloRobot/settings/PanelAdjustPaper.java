package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustPaper
extends JPanel
implements ActionListener, PropertyChangeListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 519519372661103125L;

	protected MakelangeloRobot robot;

	private JComboBox<String> paperSizes;
	private SelectFloat pw, ph;
	private JButton isLandscape;
	private boolean beingModified;
	private SelectColor paperColor;
	
	class PaperSize {
		public String name;
		public int width;
		public int height;
		
		PaperSize(String name,int width,int height) {
			this.name = name;
			this.width = width;
			this.height = height;
		}
		
		public String toDescription() {
			return name+" ("+width+" x "+height+")";
		}
	}
	
	private PaperSize commonPaperSizes [] = {
		new PaperSize("4A0",1682,2378),
		new PaperSize("2A0",1189,1682),
		new PaperSize("A0",841,1189),
		new PaperSize("A1",594,841),
		new PaperSize("A2",420,594),
		new PaperSize("A3",297,420),
		new PaperSize("A4",210,297),
		new PaperSize("A5",148,210),
		new PaperSize("A6",105,148),
		new PaperSize("A7",74,105),
		new PaperSize("US Half Letter",140,216),
		new PaperSize("US Letter",216,279),
		new PaperSize("US Legal",216,356),
		new PaperSize("US Junior Legal",127,203),
		new PaperSize("US Ledger / Tabloid",279,432),
		new PaperSize("ANSI A",216,279),
		new PaperSize("ANSI B",279,432),
		new PaperSize("ANSI C",432,559),
		new PaperSize("ANSI D",559,864),
		new PaperSize("ANSI E",864,1118),
		new PaperSize("Arch A",229,305),
		new PaperSize("Arch B",305,457),
		new PaperSize("Arch C",457,610),
		new PaperSize("Arch D",610,914),
		new PaperSize("Arch E",914,1219),
		new PaperSize("Arch E1",762,1067)
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
		String[] commonPaperNames = new String[commonPaperSizes.length+1];
		commonPaperNames[0]="---";
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			commonPaperNames[i+1] = new String(commonPaperSizes[i].toDescription());
		}
		
		paperSizes = new JComboBox<>(commonPaperNames);
		paperSizes.addActionListener(this);
		c.gridx=0;  c.gridy=y;  p.add(new JLabel(Translator.get("PaperSize")),c);
		d.gridx=1;  d.gridy=y;  d.gridwidth=3;  p.add(paperSizes,d);
		y++;

		
		// landscape checkbox
		//isLandscape = new JCheckBox(Translator.get("isLandscape"), false);
		isLandscape = new JButton("\u21cb");
		isLandscape.addActionListener(this);
		//d.gridx=0;  d.gridy=y;  d.gridwidth=3;  p.add(isLandscape,d);
		//y++;
		//c.fill = GridBagConstraints.HORIZONTAL;
		//c.fill = GridBagConstraints.NONE;

		// manual paper size settings
		d.gridwidth=1;
		pw = new SelectFloat();
		ph = new SelectFloat();
		//c.gridx=0;  c.gridy=y;  p.add(Box.createGlue(),c);
		d.gridx=1;  d.gridy=y;  p.add(pw,d); 
		d.gridx=2;  d.gridy=y;  p.add(isLandscape,d);
		d.gridx=3;  d.gridy=y;  p.add(ph,d);
		d.gridx=4;  d.gridy=y;  p.add(new JLabel(Translator.get("Millimeters")),d);
		y++;
		Dimension s = pw.getPreferredSize();
		s.width = 80;
		pw.setPreferredSize(s);
		ph.setPreferredSize(s);
		pw.addPropertyChangeListener(this);
		ph.addPropertyChangeListener(this);

		
		// paper margin
		JPanel marginPanel = new JPanel(new GridBagLayout());
		GridBagConstraints pm = new GridBagConstraints();
		paperMargin = new JSlider(JSlider.HORIZONTAL, 0, 50, 100 - (int) (robot.getSettings().getPaperMargin() * 100));
		paperMargin.setMajorTickSpacing(10);
		paperMargin.setMinorTickSpacing(5);
		paperMargin.setPaintTicks(false);
		paperMargin.setPaintLabels(true);
		paperMargin.addChangeListener(this);
		
		pm.anchor = GridBagConstraints.WEST;
		JLabel marginLabel = new JLabel(Translator.get("PaperMargin"));
		marginLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		//marginLabel.setBorder(BorderFactory.createLineBorder(new Color(0,0,0), 1));
		marginPanel.add(marginLabel,pm);
		pm.gridy=0;
		pm.gridwidth=2;
		pm.anchor = GridBagConstraints.EAST;
		//paperMargin.setBorder(BorderFactory.createLineBorder(new Color(0,0,0), 1));
		marginPanel.add(paperMargin,pm);

		this.add(marginPanel, c);
		
		JPanel colorPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cm = new GridBagConstraints();
		cm.gridx=0;
		cm.gridy=0;
		cm.fill=GridBagConstraints.HORIZONTAL;
		paperColor = new SelectColor(this,"paper color",robot.getSettings().getPaperColor());
		colorPanel.add(paperColor,cm);
		cm.gridy++;
		
		c.gridy++;
		this.add(colorPanel,c);
		
		updateValues();
	}

	
	public void stateChanged(ChangeEvent e) {}


	/**
	 * Must match commonPaperSizes
	 * @return
	 */
	public int getCurrentPaperSizeChoice(double paperWidth,double paperHeight) {
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			if(paperWidth == commonPaperSizes[i].width && 
				paperHeight == commonPaperSizes[i].height)
				return i+1;
				
		}

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
				PaperSize s = commonPaperSizes[selectedIndex-1];
				float sw = s.width;
				float sh = s.height;
				if(isLandscape.isSelected()) {
					float temp = sw;
					sw = sh;
					sh = temp;
				}
				pw.setValue(sw);
				ph.setValue(sh);
			}
		}
		if( subject == isLandscape ) {
			String sw = pw.getText();
			String sh = ph.getText();
			pw.setValue(Float.parseFloat(sh));
			ph.setValue(Float.parseFloat(sw));
		}
		
		beingModified=false;
	}

	public void updateValues() {
		if(robot==null) return;
		
		double w = robot.getSettings().getPaperWidth();
		double h = robot.getSettings().getPaperHeight();

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
		double pwf = ((Number)pw.getValue()).doubleValue();
		double phf = ((Number)ph.getValue()).doubleValue();
		
		boolean data_is_sane=true;
		if( pwf<=0 ) data_is_sane=false;
		if( phf<=0 ) data_is_sane=false;

		if (data_is_sane) {
			MakelangeloRobotSettings s = robot.getSettings();
			s.setPaperSize(pwf,phf);
			s.setPaperColor(paperColor.getColor());

			double pm = (100 - paperMargin.getValue()) * 0.01;
			s.setPaperMargin(pm);
		}
	}
}
