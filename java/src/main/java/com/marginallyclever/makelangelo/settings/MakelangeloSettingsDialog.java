package com.marginallyclever.makelangelo.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;

/**
 * Controls related to configuring a Makelangelo machine
 *
 * @author danroyer
 * @since 7.1.4
 */
public class MakelangeloSettingsDialog
extends JDialog
implements ActionListener {
    
  /**
   * @see Serializable
   */
  private static final long serialVersionUID = 1L;

  protected Translator translator;
  protected MakelangeloRobot robot;
  protected Makelangelo gui;

  protected JTabbedPane panes;
  protected JButton save, cancel;
  
  protected PanelAdjustMachineSize panelAdjustMachineSize;
  protected PanelJogMotors panelJogMotors;
  protected DrawingTool panelAdjustPen;
  //protected PanelAdjustTools panelAdjustTools;
  //protected PanelSelectTool panelSelectTool;
  
  protected int dialogWidth = 450;
  protected int dialogHeight = 500;
  
  public MakelangeloSettingsDialog(Makelangelo gui, Translator translator, MakelangeloRobot robot) {
	super(gui.getParentFrame(),translator.get("configureMachine"),true);

	this.translator = translator;
	this.gui = gui;
	this.robot = robot;
  }

  
  // settings menu
  public void run() {
    panes = new JTabbedPane();
    
    panelAdjustMachineSize = new PanelAdjustMachineSize(translator,robot);
    panelJogMotors = new PanelJogMotors(gui,translator,robot);
    panelAdjustPen = robot.settings.getTool(0);
    //panelAdjustTools = new PanelAdjustTools(gui,translator,robot);
    //panelSelectTool = new PanelSelectTool(gui,translator,robot);
    
    panes.addTab(translator.get("MenuSettingsMachine"),panelAdjustMachineSize);
    panes.addTab(translator.get("JogMotors"),panelJogMotors);
    panes.addTab(translator.get("MenuAdjustTool"),panelAdjustPen.getPanel());
    //panes.addTab(translator.get("MenuAdjustTool"),panelAdjustTools);
    //panes.addTab(translator.get("MenuSelectTool"),panelSelectTool);
    
	this.setLayout(new GridBagLayout());
    GridBagConstraints d = new GridBagConstraints();

    	// choice of machine configuration, save, save as, delete.

    	// the panes for the selected machine configuration
    	d.fill=GridBagConstraints.BOTH;
    	d.gridx=0;
    	d.gridy=0;
    	d.weightx=1;
    	d.weighty=1;
    	panes.setPreferredSize(new Dimension(dialogWidth,dialogHeight));
    	this.add(panes,d);
	
        // save and cancel buttons
    	cancel = new JButton(translator.get("Cancel"));
        save = new JButton(translator.get("Save"));

	    JPanel p = new JPanel(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
		    c.anchor=GridBagConstraints.EAST;
		    c.gridy=0;
		    c.weightx=0;
		    c.weighty=1;
		    c.gridx=1; c.gridwidth=1; p.add(save,c);
		    c.gridx=2; c.gridwidth=1; p.add(cancel,c);
		    c.weightx=1;
		    c.gridx=0; c.gridwidth=1; p.add(Box.createGlue(),c);
	        this.getRootPane().setDefaultButton(save);
	        cancel.addActionListener(this);
	        save.addActionListener(this);

    	d.weightx=1;
    	d.weighty=0;
	    d.gridy=1;
	    d.fill=GridBagConstraints.HORIZONTAL;
	    this.add(p,d);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - dialogWidth) / 2, (screenSize.height - dialogHeight) / 2);
        this.pack();
        this.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
	  Object src = e.getSource();
	  
	  if(src == save) {
		  panelAdjustMachineSize.save();
		  panelAdjustPen.save();
		  //panelAdjustTools.save();
		  //panelSelectTool.save();
		  this.dispose();
	  }
	  if(src == cancel) {
		  this.dispose();
		  return;
	  }
  }
}
