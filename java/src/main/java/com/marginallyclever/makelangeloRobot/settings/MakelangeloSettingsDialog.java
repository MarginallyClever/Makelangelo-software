package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

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

  protected MakelangeloRobot robot;

  protected JTabbedPane panes;
  protected JButton save, cancel;

  // TODO pull these from the class listings in the package?
  private int [] availableHardwareVersions={2,3,5};
  private JComboBox<String> hardwareVersionChoices;
  private String[] hardwareVersionNames={"2+","3+","5+"};
  private int originalHardwareVersion;
  
  private JPanel modelPanel;
  protected PanelAdjustMachine panelAdjustMachine;
  protected PanelAdjustPaper panelAdjustPaper;
  protected DrawingTool panelAdjustPen;
  private JPanel saveAndCancelPanel;
  
  protected int dialogWidth = 450;
  protected int dialogHeight = 500;
  
  public MakelangeloSettingsDialog(Frame parent, MakelangeloRobot robot) {
	super(parent,Translator.get("configureMachine"),true);

	this.robot = robot;
  }

  
  // display settings menu
  public void run() {
	  originalHardwareVersion = robot.getSettings().getHardwareVersion();
	  
	  this.setLayout(new GridBagLayout());
	  GridBagConstraints d = new GridBagConstraints();

	  buildModelPanel();

	  // hardware model settings
	  panes = new JTabbedPane(JTabbedPane.TOP);
	  panes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	  //panes.setPreferredSize(new Dimension(dialogWidth,dialogHeight));

	  rebuildTabbedPanes();

	  buildSaveAndCancel();

	  // now assemble the dialog
	  d.fill=GridBagConstraints.HORIZONTAL;
	  d.gridx=0;
	  d.gridy=0;
	  d.weightx=0;
	  d.weighty=0;
	  d.gridwidth=1;
	  this.add(modelPanel, d);
	  d.fill=GridBagConstraints.BOTH;
	  d.gridy=1;
	  d.weightx=1;
	  d.weighty=1;
	  this.add(panes,d);
	  d.fill=GridBagConstraints.HORIZONTAL;
	  d.gridy=2;
	  d.weighty=0;
	  this.add(saveAndCancelPanel,d);
	  this.getRootPane().setDefaultButton(save);

	  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	  this.setLocation((screenSize.width - dialogWidth) / 2, (screenSize.height - dialogHeight) / 2);
	  this.pack();
	  this.setVisible(true);
  }

  // hardware model choice
  private void buildModelPanel() {
	  modelPanel = new JPanel(new GridBagLayout());
	  modelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

	  GridBagConstraints d = new GridBagConstraints();
	  // the panes for the selected machine configuration
	  d.fill=GridBagConstraints.BOTH;
	  d.gridx=0;
	  d.gridy=0;
	  d.weightx=0;
	  d.weighty=0;
	  
	  JLabel modelLabel = new JLabel(Translator.get("HardwareVersion")); 
	  modelLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
	  modelPanel.add(modelLabel, d);
	  
	  d.gridx=1;
	  d.gridwidth=2;
	  hardwareVersionNames = new String[availableHardwareVersions.length];
	  for(int i=0;i<hardwareVersionNames.length;++i) {
		  hardwareVersionNames[i] = "Makelangelo "+Integer.toString(availableHardwareVersions[i])+"+";
	  }	  
	  hardwareVersionChoices = new JComboBox<>(hardwareVersionNames);
	  // set the default
	  int hv = robot.getSettings().getHardwareVersion();
	  String hvn = Integer.toString(hv);
	  for(int i=0;i<hardwareVersionNames.length;++i) {
		  if(hardwareVersionNames[i].startsWith("Makelangelo "+hvn)) {
			  hardwareVersionChoices.setSelectedIndex(i);
			  break;
		  }
	  }
	  modelPanel.add(hardwareVersionChoices, d);
	  hardwareVersionChoices.addActionListener(this);
  }

  // save and cancel buttons
  private void buildSaveAndCancel() {
	  cancel = new JButton(Translator.get("Cancel"));
	  save = new JButton(Translator.get("Save"));

	  saveAndCancelPanel = new JPanel(new GridBagLayout());
	  GridBagConstraints c = new GridBagConstraints();
	  c.anchor=GridBagConstraints.EAST;
	  c.gridx=0;
	  c.gridy=0;
	  c.weightx=0;
	  c.weighty=1;
	  c.gridx=1; c.gridwidth=1; saveAndCancelPanel.add(save,c);
	  c.gridx=2; c.gridwidth=1; saveAndCancelPanel.add(cancel,c);
	  c.weightx=1;
	  c.gridx=0; c.gridwidth=1; saveAndCancelPanel.add(Box.createGlue(),c);
	  cancel.addActionListener(this);
	  save.addActionListener(this);

	  saveAndCancelPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
  }
  
  
  private void rebuildTabbedPanes() {
	  panes.removeAll();
	  
	  panelAdjustMachine = new PanelAdjustMachine(robot);
	  panes.addTab(Translator.get("MenuSettingsMachine"),panelAdjustMachine);

	  panelAdjustPaper = new PanelAdjustPaper(robot);
	  panes.addTab(Translator.get("MenuAdjustPaper"),panelAdjustPaper);

	  panelAdjustPen = robot.getSettings().getTool(0);
	  panes.addTab(Translator.get("MenuAdjustTool"),panelAdjustPen.getPanel());

	  //panelAdjustTools = new PanelAdjustTools(translator,robot);
	  //panes.addTab(translator.get("MenuAdjustTool"),panelAdjustTools);

	  //panelSelectTool = new PanelSelectTool(translator,robot);
	  //panes.addTab(translator.get("MenuSelectTool"),panelSelectTool);
  }
  
  public void actionPerformed(ActionEvent e) {
	  Object src = e.getSource();
	  
	  if(src == hardwareVersionChoices) {
		  int newChoice=availableHardwareVersions[hardwareVersionChoices.getSelectedIndex()];
		  robot.getSettings().setHardwareVersion(newChoice);
		  rebuildTabbedPanes();
	  }
	  if(src == save) {
		  panelAdjustMachine.save();
		  panelAdjustPaper.save();
		  panelAdjustPen.save();
		  robot.getSettings().saveConfig();
		  robot.sendConfig();
		  this.dispose();
	  }
	  if(src == cancel) {
		  robot.getSettings().setHardwareVersion(originalHardwareVersion);
		  this.dispose();
	  }
  }
}
