package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Controls related to configuring a makelangelo machine
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

  protected MultilingualSupport translator;
  protected MakelangeloRobot machineConfiguration;
  protected MainGUI gui;

  protected JTabbedPane panes;
  protected JButton save, cancel;

  protected PanelAdjustMachineSize panelAdjustMachineSize;
  protected PanelJogMotors panelJogMotors;
  protected PanelAdjustTools panelAdjustTools;
  protected PanelSelectTool panelSelectTool;
  
  
  public MakelangeloSettingsDialog(MainGUI _gui, MultilingualSupport _translator, MakelangeloRobot _machineConfiguration) {
	super(_gui.getParentFrame(),_translator.get("AdjustMachineSize"),true);

	translator = _translator;
	gui = _gui;
	machineConfiguration = _machineConfiguration;
  }

  
// settings menu
  public void run() {
    panes = new JTabbedPane();
    
    panelAdjustMachineSize = new PanelAdjustMachineSize(gui,translator,machineConfiguration);
    panelJogMotors = new PanelJogMotors(gui,translator,machineConfiguration);
    panelAdjustTools = new PanelAdjustTools(gui,translator,machineConfiguration);
    panelSelectTool = new PanelSelectTool(gui,translator,machineConfiguration);
    
    panes.addTab(translator.get("MenuSettingsMachine"),panelAdjustMachineSize);
    panes.addTab(translator.get("JogMotors"),panelJogMotors);
    panes.addTab(translator.get("MenuAdjustTool"),panelAdjustTools);
    panes.addTab(translator.get("MenuSelectTool"),panelSelectTool);
    
	this.setLayout(new GridBagLayout());
    GridBagConstraints d = new GridBagConstraints();

    	d.fill=GridBagConstraints.BOTH;
    	d.gridx=0;
    	d.gridy=0;
    	d.weightx=1;
    	d.weighty=1;
    	panes.setPreferredSize(new Dimension(600,500));
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

    	d.weightx=1;
    	d.weighty=0;
	    d.gridy=1;
	    d.fill=GridBagConstraints.HORIZONTAL;
	    this.add(p,d);
        
        this.pack();
        this.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
	  Object src = e.getSource();
	  
	  if(src == save) {
		  panelAdjustMachineSize.save();
		  panelAdjustTools.save();
		  panelSelectTool.save();
		  this.dispose();
	  }
	  if(src == cancel) {
		  this.dispose();
		  return;
	  }
  }
}
