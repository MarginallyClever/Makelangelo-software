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

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;

public class PanelAdjustMachineSize
extends JPanel
implements ActionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;
	
	protected Translator translator;
	protected MakelangeloRobot robot;
	
	protected JComboBox<String> paperSizes;
	protected JTextField mw, mh;
	protected JTextField pw, ph;
	
	protected JTextField acceleration;
	
	protected JCheckBox flipForGlass;
	protected JTextField pulleyDiameterLeft,pulleyDiameterRight;
   
	
	public PanelAdjustMachineSize(Translator translator, MakelangeloRobot robot) {
		this.translator = translator;
		this.robot = robot;

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

	    double r = robot.settings.getLimitRight();
	    double l = robot.settings.getLimitLeft(); 
	    double w = (r-l)*10;
	    double h = (robot.settings.getLimitTop()-robot.settings.getLimitBottom())*10;
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
	    paperSizes = new JComboBox<>(robot.settings.commonPaperSizes);
	    paperSizes.setSelectedIndex(robot.settings.getCurrentPaperSizeChoice( robot.settings.getPaperWidth()*10, robot.settings.getPaperHeight()*10 ));
	    
	    pw = new JTextField(Double.toString(robot.settings.getPaperWidth()*10));
	    ph = new JTextField(Double.toString(robot.settings.getPaperHeight()*10));
	    
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
 
	    pulleyDiameterLeft = new JTextField(String.valueOf(robot.settings.getPulleyDiameterLeft() * 10));
	    pulleyDiameterRight = new JTextField(String.valueOf(robot.settings.getPulleyDiameterRight() * 10));
	    y=2;
	    c.weightx = 0;
	    c.anchor=GridBagConstraints.EAST;
	    d.anchor=GridBagConstraints.WEST;
	    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("Left")), c);
	    d.gridx = 1;    d.gridy = y;    p.add(pulleyDiameterLeft, d);
	    d.gridx = 2;    d.gridy = y;    p.add(new JLabel(translator.get("Millimeters")), d);
	    y++;
	    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("Right")), c);
	    d.gridx = 1;    d.gridy = y;    p.add(pulleyDiameterRight, d);
	    d.gridx = 2;    d.gridy = y;    p.add(new JLabel(translator.get("Millimeters")), d);

	    Dimension s = pulleyDiameterLeft.getPreferredSize();
	    s.width = 80;
	    pulleyDiameterLeft.setPreferredSize(s);
	    pulleyDiameterRight.setPreferredSize(s);

	    this.add(new JSeparator());
	    p = new JPanel(new GridBagLayout());
	    this.add(p);

	    acceleration = new JTextField(Double.toString(robot.settings.getAcceleration()));

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
	    flipForGlass = new JCheckBox(translator.get("FlipForGlass"));
	    flipForGlass.setSelected(robot.settings.isReverseForGlass());
	    this.add(flipForGlass,c);

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
    	paperSizes.setSelectedIndex(robot.settings.getCurrentPaperSizeChoice(w,h));	
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

    	double bld = Double.valueOf(pulleyDiameterLeft.getText()) / 10.0;
    	double brd = Double.valueOf(pulleyDiameterRight.getText()) / 10.0;
    	double accel = Double.valueOf(acceleration.getText());
    	
    	if (bld <= 0) data_is_sane = false;
    	if (brd <= 0) data_is_sane = false;

    	if (data_is_sane) {
    		robot.settings.setReverseForGlass(flipForGlass.isSelected());
    		robot.settings.setPulleyDiameter(bld,brd);
    		robot.settings.setPaperSize(pwf,phf);
    		robot.settings.setMachineSize(mwf,mhf);
    		robot.settings.setAcceleration(accel);
    		robot.settings.saveConfig();
    		robot.sendConfig();
    	}
    }
}
