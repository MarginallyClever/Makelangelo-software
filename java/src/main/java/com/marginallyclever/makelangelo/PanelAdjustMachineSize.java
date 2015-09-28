package com.marginallyclever.makelangelo;

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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class PanelAdjustMachineSize
extends JPanel
implements ActionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84665452555208524L;
	
	protected MainGUI gui;
	protected MultilingualSupport translator;
	protected MakelangeloRobot machineConfiguration;
	
	protected JComboBox<String> paperSizes;
	protected JTextField mw, mh;
	protected JTextField pw, ph;

	protected JTextField mBobbin1,mBobbin2;
   
	
	public PanelAdjustMachineSize(MainGUI _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
		gui = _gui;
		translator = _translator;
		machineConfiguration = _machineConfiguration;

	    this.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
	    //this.setLayout(new GridLayout(0,1,8,8));
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


	    GridBagConstraints c = new GridBagConstraints();
	    GridBagConstraints d = new GridBagConstraints();

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

	    mw = new JTextField(String.valueOf((machineConfiguration.limitRight-machineConfiguration.limitLeft)*10));
	    mh = new JTextField(String.valueOf((machineConfiguration.limitTop-machineConfiguration.limitBottom)*10));
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
	    paperSizes.setSelectedIndex(machineConfiguration.getCurrentPaperSizeChoice( (machineConfiguration.paperRight-machineConfiguration.paperLeft)*10, (machineConfiguration.paperTop-machineConfiguration.paperBottom)*10) );
	    
	    pw = new JTextField(Integer.toString((int)((machineConfiguration.paperRight-machineConfiguration.paperLeft)*10)));
	    ph = new JTextField(Integer.toString((int)((machineConfiguration.paperTop-machineConfiguration.paperBottom)*10)));
	    
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
	    c.gridwidth=3;
	    p.add(new JLabel(translator.get("AdjustPulleySize"),SwingConstants.CENTER),c);
	    c.gridwidth=1;
 
	    mBobbin1 = new JTextField(String.valueOf(machineConfiguration.bobbinDiameterLeft * 10));
	    mBobbin2 = new JTextField(String.valueOf(machineConfiguration.bobbinDiameterRight * 10));
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

          if (bld <= 0) data_is_sane = false;
          if (brd <= 0) data_is_sane = false;

        if (data_is_sane) {
        	machineConfiguration.bobbinDiameterLeft = bld;
        	machineConfiguration.bobbinDiameterRight = brd;
          //startingPositionIndex = startPos.getSelectedIndex();
          /*// relative to machine limits
          switch(startingPositionIndex%3) {
          case 0:
            paper_left=(mwf-pwf)/2.0f;
            paper_right=mwf-paper_left;
            limit_left=0;
            limit_right=mwf;
            break;
          case 1:
            paper_left = -pwf/2.0f;
            paper_right = pwf/2.0f;
            limit_left = -mwf/2.0f;
            limit_right = mwf/2.0f;
            break;
          case 2:
            paper_right=-(mwf-pwf)/2.0f;
            paper_left=-mwf-paper_right;
            limit_left=-mwf;
            limit_right=0;
            break;
          }
          switch(startingPositionIndex/3) {
          case 0:
            paper_top=-(mhf-phf)/2;
            paper_bottom=-mhf-paper_top;
            limit_top=0;
            limit_bottom=-mhf;
            break;
          case 1:
            paper_top=phf/2;
            paper_bottom=-phf/2;
            limit_top=mhf/2;
            limit_bottom=-mhf/2;
            break;
          case 2:
            paper_bottom=(mhf-phf)/2;
            paper_top=mhf-paper_bottom;
            limit_top=mhf;
            limit_bottom=0;
            break;
          }
          */
        	machineConfiguration.startingPositionIndex = 4;
          // relative to paper limits
          switch (machineConfiguration.startingPositionIndex % 3) {
            case 0:
            	machineConfiguration.paperLeft = 0;
            	machineConfiguration.paperRight = pwf;
            	machineConfiguration.limitLeft = -(mwf - pwf) / 2.0f;
            	machineConfiguration.limitRight = (mwf - pwf) / 2.0f + pwf;
              break;
            case 1:
            	machineConfiguration.paperLeft = -pwf / 2.0f;
            	machineConfiguration.paperRight = pwf / 2.0f;
            	machineConfiguration.limitLeft = -mwf / 2.0f;
            	machineConfiguration.limitRight = mwf / 2.0f;
              break;
            case 2:
            	machineConfiguration.paperRight = 0;
              machineConfiguration.paperLeft = -pwf;
              machineConfiguration.limitLeft = -pwf - (mwf - pwf) / 2.0f;
              machineConfiguration.limitRight = (mwf - pwf) / 2.0f;
              break;
          }
          switch (machineConfiguration.startingPositionIndex / 3) {
            case 0:
            	machineConfiguration.paperTop = 0;
	            machineConfiguration.paperBottom = -phf;
	            machineConfiguration.limitTop = (mhf - phf) / 2.0f;
	            machineConfiguration.limitBottom = -phf - (mhf - phf) / 2.0f;
              break;
            case 1:
            	machineConfiguration.paperTop = phf / 2.0f;
            	machineConfiguration.paperBottom = -phf / 2.0f;
            	machineConfiguration.limitTop = mhf / 2.0f;
            	machineConfiguration.limitBottom = -mhf / 2.0f;
              break;
            case 2:
            	machineConfiguration.paperBottom = 0;
            	machineConfiguration.paperTop = phf;
              machineConfiguration.limitTop = phf + (mhf - phf) / 2.0f;
              machineConfiguration.limitBottom = -(mhf - phf) / 2.0f;
              break;
          }

          machineConfiguration.saveConfig();
          gui.sendConfig();
        }
      }
}
