package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;

public class PanelAdjustMachineSize
extends JPanel
implements ActionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;
	
	protected Makelangelo gui;
	protected Translator translator;
	protected MakelangeloRobotSettings machineConfiguration;
	
	protected JComboBox<String> paperSizes;
	protected JTextField mw, mh;
	protected JTextField pw, ph;
	protected JTextField acceleration;
	
	protected JCheckBox reverse_h;
	protected JTextField mBobbin1,mBobbin2;
   
	
	public PanelAdjustMachineSize(Makelangelo _gui, Translator _translator, MakelangeloRobotSettings _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

	    this.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
	    //this.setLayout(new GridLayout(0,1,8,8));
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


	    GridBagConstraints c = new GridBagConstraints();
	    GridBagConstraints d = new GridBagConstraints();
	    c.ipadx=5;
	    c.ipady=0;

	    int y = 0;
	/*
	    JLabel picLabel = null;
	    BufferedImage myPicture = null;
	    final String limit_file = "limits.png";
	    try (final InputStream s = getClass().getClassLoader().getResourceAsStream(limit_file)) {
	      myPicture = ImageIO.read(s);
	    }
	    catch(IOException e) {
	      logger.error("{}", e);
	      myPicture=null;
	    }
	    
	    if (myPicture != null) {
	      picLabel = new JLabel(new ImageIcon(myPicture));
	    } else {
	      logger.error("{}", translator.get("CouldNotFind")+limit_file);
	    }*/

	/*
	    if (myPicture != null) {
	      c.weightx = 0.25;
	      c.gridx = 0;
	      c.gridy = y;
	      c.gridwidth = 4;
	      c.gridheight = 4;
	      c.anchor = GridBagConstraints.CENTER;
	      this.add(picLabel, c);
	      y += 5;
	    }
	*/    
	    JPanel p = new JPanel(new GridBagLayout());
	    this.add(p);
	    
	    c.gridwidth=3;
	    p.add(new JLabel("1\" = 25.4mm",SwingConstants.CENTER),c);
	    c.gridwidth=1;
	    
	    y=1;

	    c.anchor=GridBagConstraints.EAST;
	    d.anchor=GridBagConstraints.WEST;

	    double r = machineConfiguration.getLimitRight();
	    double l = machineConfiguration.getLimitLeft(); 
	    double w = (r-l)*10;
	    double h = (machineConfiguration.getLimitTop()-machineConfiguration.getLimitBottom())*10;
	    mw = new JTextField(String.valueOf(w));
	    mh = new JTextField(String.valueOf(h));
	    c.gridx=0;  c.gridy=y;  p.add(new JLabel(translator.get("MachineWidth")),c);
	    d.gridx=1;  d.gridy=y;  p.add(mw,d);
	    d.gridx=2;  d.gridy=y;  p.add(new JLabel("mm"),d);
	    y++;
	    c.gridx=0;  c.gridy=y;  p.add(new JLabel(translator.get("MachineHeight")),c);
	    d.gridx=1;  d.gridy=y;  p.add(mh,d);
	    d.gridx=2;  d.gridy=y;  p.add(new JLabel("mm"),d);
	    y++;

	    this.add(new JSeparator(SwingConstants.HORIZONTAL));
	    p = new JPanel(new GridBagLayout());
	    this.add(p);
	    y=0;
	    paperSizes = new JComboBox<>(machineConfiguration.commonPaperSizes);
	    paperSizes.setSelectedIndex(machineConfiguration.getCurrentPaperSizeChoice( machineConfiguration.getPaperWidth()*10, machineConfiguration.getPaperHeight()*10 ));
	    
	    pw = new JTextField(Double.toString(machineConfiguration.getPaperWidth()*10));
	    ph = new JTextField(Double.toString(machineConfiguration.getPaperHeight()*10));
	    
	    c.gridx=0;  c.gridy=y;  p.add(new JLabel(translator.get("PaperSize")),c);
	    d.gridx=1;  d.gridy=y;  d.gridwidth=2;  p.add(paperSizes,d);
	    y=1;
	    d.gridwidth=1;

	    c.gridx=0;  c.gridy=y;  p.add(Box.createGlue(),c);
	    d.gridx=1;  d.gridy=y;  p.add(pw,d); 
	    d.gridx=2;  d.gridy=y;  p.add(new JLabel(translator.get("Millimeters")),d);
	    y++;
	    c.gridx=0;  c.gridy=y;  p.add(new JLabel(" x "),c);
	    d.gridx=1;  d.gridy=y;  p.add(ph,d);
	    d.gridx=2;  d.gridy=y;  p.add(new JLabel(translator.get("Millimeters")),d);
	    y++;
	    
	    //c.gridx=0; c.gridy=9; c.gridwidth=4; c.gridheight=1;
	    //this.add(new JLabel("For more info see http://bit.ly/fix-this-link."),c);
	    //c.gridx=0; c.gridy=11; c.gridwidth=2; c.gridheight=1;  this.add(new JLabel("Pen starts at paper"),c);
	    //c.anchor=GridBagConstraints.WEST;
	    //c.gridx=2; c.gridy=11; c.gridwidth=2; c.gridheight=1;  this.add(startPos,c);

	    //final JComboBox<String> startPos = new JComboBox<String>(startingStrings);
	    //startPos.setSelectedIndex(startingPositionIndex);

	    
	    this.add(new JSeparator());
	    p = new JPanel(new GridBagLayout());
	    this.add(p);
	    
	    c = new GridBagConstraints();
	    c.ipadx=5;
	    c.ipady=0;
	    c.gridwidth=3;
	    p.add(new JLabel(translator.get("AdjustPulleySize"),SwingConstants.CENTER),c);
	    c.gridwidth=1;
 
	    mBobbin1 = new JTextField(String.valueOf(machineConfiguration.getPulleyDiameterLeft() * 10));
	    mBobbin2 = new JTextField(String.valueOf(machineConfiguration.getPulleyDiameterRight() * 10));
	    y=2;
	    c.weightx = 0;
	    c.anchor=GridBagConstraints.EAST;
	    d.anchor=GridBagConstraints.WEST;
	    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("Left")), c);
	    d.gridx = 1;    d.gridy = y;    p.add(mBobbin1, d);
	    d.gridx = 2;    d.gridy = y;    p.add(new JLabel(translator.get("Millimeters")), d);
	    y++;
	    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("Right")), c);
	    d.gridx = 1;    d.gridy = y;    p.add(mBobbin2, d);
	    d.gridx = 2;    d.gridy = y;    p.add(new JLabel(translator.get("Millimeters")), d);

	    Dimension s = mBobbin1.getPreferredSize();
	    s.width = 80;
	    mBobbin1.setPreferredSize(s);
	    mBobbin2.setPreferredSize(s);

	    this.add(new JSeparator());
	    p = new JPanel(new GridBagLayout());
	    this.add(p);

	    acceleration = new JTextField(Double.toString(machineConfiguration.getAcceleration()));

	    y=0;
	    c.weightx = 0;
	    c.anchor=GridBagConstraints.EAST;
	    d.anchor=GridBagConstraints.WEST;
	    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("AdjustAcceleration")), c);
	    d.gridx = 1;    d.gridy = y;    p.add(acceleration, d);
	    y++;
	    
	    
	    this.add(new JSeparator());
	    c.fill=GridBagConstraints.HORIZONTAL;
	    c.anchor=GridBagConstraints.CENTER;
	    c.gridx=0;
	    c.gridy++;
	    reverse_h = new JCheckBox(translator.get("FlipForGlass"));
	    reverse_h.setSelected(machineConfiguration.isReverseForGlass());
	    this.add(reverse_h,c);

	    s = ph.getPreferredSize();
	    s.width = 80;
	    mw.setPreferredSize(s);
	    mh.setPreferredSize(s);
	    pw.setPreferredSize(s);
	    ph.setPreferredSize(s);

	    paperSizes.addActionListener(this);
	    pw.addKeyListener(this);
	    ph.addKeyListener(this);
	    this.setVisible(true);
	}


	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) { Event(e); }
	public void keyTyped(KeyEvent e) { Event(e); }
	
	private void Event(KeyEvent e) {
    	double w=0;
    	double h=0;
    	try {
    		w = Double.parseDouble(pw.getText());
    		h = Double.parseDouble(ph.getText());
    	} catch(Exception err) {
    		err.getMessage();
    	}
    	paperSizes.setSelectedIndex(machineConfiguration.getCurrentPaperSizeChoice(w,h));	
	}

	
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
    	double mwf = Double.valueOf(mw.getText()) / 10.0;
    	double mhf = Double.valueOf(mh.getText()) / 10.0;
    	boolean data_is_sane=true;
    	if( pwf<=0 ) data_is_sane=false;
    	if( phf<=0 ) data_is_sane=false;
    	if( mwf<=0 ) data_is_sane=false;
    	if( mhf<=0 ) data_is_sane=false;

    	double bld = Double.valueOf(mBobbin1.getText()) / 10.0;
    	double brd = Double.valueOf(mBobbin2.getText()) / 10.0;
    	double accel = Double.valueOf(acceleration.getText());
    	
    	if (bld <= 0) data_is_sane = false;
    	if (brd <= 0) data_is_sane = false;

    	if (data_is_sane) {
    		machineConfiguration.setReverseForGlass(reverse_h.isSelected());
    		machineConfiguration.setPulleyDiameter(bld,brd);
    		machineConfiguration.setPaperSize(pwf,phf);
    		machineConfiguration.setMachineSize(mwf,mhf);
    		machineConfiguration.setAcceleration(accel);
    		machineConfiguration.saveConfig();
    		gui.sendConfig();
    	}
    }
}
