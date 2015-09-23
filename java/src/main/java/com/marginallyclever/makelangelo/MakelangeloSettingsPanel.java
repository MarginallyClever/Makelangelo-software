package com.marginallyclever.makelangelo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Controls related to configuring a makelangelo machine
 *
 * @author danroyer
 * @since 7.1.4
 */
public class MakelangeloSettingsPanel
extends JScrollPane
implements ActionListener {
    
  /**
   * @see Serializable
   */
  private static final long serialVersionUID = 1L;

  // settings pane
  private JButton buttonAdjustMachineSize, buttonJogMotors, buttonChangeTool, buttonAdjustTool;

  protected MultilingualSupport translator;
  protected MachineConfiguration machineConfiguration;
  protected MainGUI gui;


  // settings menu
  public void createPanel(MainGUI _gui, MultilingualSupport _translator, MachineConfiguration _machineConfiguration) {
    translator = _translator;
    gui = _gui;
    machineConfiguration = _machineConfiguration;

    JPanel p = new JPanel(new GridLayout(0,1));
    this.setViewportView(p);
    
        // TODO: move all these into a pop-up menu with tabs
        buttonAdjustMachineSize = new JButton(translator.get("MenuSettingsMachine"));
        buttonAdjustMachineSize.addActionListener(this);
        p.add(buttonAdjustMachineSize);

        buttonJogMotors = new JButton(translator.get("JogMotors"));
        buttonJogMotors.addActionListener(this);
    p.add(buttonJogMotors);

        //p.add(new JSeparator());
        
        buttonChangeTool = new JButton(translator.get("MenuSelectTool"));
        buttonChangeTool.addActionListener(this);
        p.add(buttonChangeTool);

        buttonAdjustTool = new JButton(translator.get("MenuAdjustTool"));
        buttonAdjustTool.addActionListener(this);
        p.add(buttonAdjustTool);
  }


  protected void jogMotors() {
    JDialog driver = new JDialog(gui.getMainframe(), translator.get("JogMotors"), true);
    driver.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    final JButton buttonAneg = new JButton(translator.get("JogIn"));
    final JButton buttonApos = new JButton(translator.get("JogOut"));
    final JCheckBox m1i = new JCheckBox(translator.get("Invert"), machineConfiguration.m1invert);

    final JButton buttonBneg = new JButton(translator.get("JogIn"));
    final JButton buttonBpos = new JButton(translator.get("JogOut"));
    final JCheckBox m2i = new JCheckBox(translator.get("Invert"), machineConfiguration.m2invert);

    c.gridx = 0;
    c.gridy = 0;
    driver.add(new JLabel(translator.get("Left")), c);
    c.gridx = 0;
    c.gridy = 1;
    driver.add(new JLabel(translator.get("Right")), c);

    c.gridx = 1;
    c.gridy = 0;
    driver.add(buttonAneg, c);
    c.gridx = 1;
    c.gridy = 1;
    driver.add(buttonBneg, c);

    c.gridx = 2;
    c.gridy = 0;
    driver.add(buttonApos, c);
    c.gridx = 2;
    c.gridy = 1;
    driver.add(buttonBpos, c);

    c.gridx = 3;
    c.gridy = 0;
    driver.add(m1i, c);
    c.gridx = 3;
    c.gridy = 1;
    driver.add(m2i, c);

    ActionListener driveButtons = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object subject = e.getSource();
        if (subject == buttonApos) gui.sendLineToRobot("D00 L400");
        if (subject == buttonAneg) gui.sendLineToRobot("D00 L-400");
        if (subject == buttonBpos) gui.sendLineToRobot("D00 R400");
        if (subject == buttonBneg) gui.sendLineToRobot("D00 R-400");
        gui.sendLineToRobot("M114");
      }
    };

    ActionListener invertButtons = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        machineConfiguration.m1invert = m1i.isSelected();
        machineConfiguration.m2invert = m2i.isSelected();
        machineConfiguration.saveConfig();
        gui.sendConfig();
      }
    };

    buttonApos.addActionListener(driveButtons);
    buttonAneg.addActionListener(driveButtons);

    buttonBpos.addActionListener(driveButtons);
    buttonBneg.addActionListener(driveButtons);

    m1i.addActionListener(invertButtons);
    m2i.addActionListener(invertButtons);

    gui.sendLineToRobot("M114");
    driver.pack();
    driver.setVisible(true);
  }

  public void updateButtonAccess(boolean isConfirmed, boolean isRunning) {
    buttonAdjustMachineSize.setEnabled(!isRunning);
    buttonJogMotors.setEnabled(isConfirmed && !isRunning);
    buttonChangeTool.setEnabled(!isRunning);
    buttonAdjustTool.setEnabled(!isRunning);
  }


  // The user has done something.  respond to it.
  public void actionPerformed(ActionEvent e) {
    Object subject = e.getSource();

    if (subject == buttonAdjustMachineSize) {
      machineConfiguration.adjustMachineSize();
      gui.updateMachineConfig();
      return;
    }
    if (subject == buttonChangeTool) {
      machineConfiguration.changeTool();
      gui.updateMachineConfig();
      return;
    }
    if (subject == buttonAdjustTool) {
      machineConfiguration.adjustTool();
      gui.updateMachineConfig();
      return;
    }
    if (subject == buttonJogMotors) {
      jogMotors();
      return;
    }
  }
}
