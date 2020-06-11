package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties;

/**
 * Controls related to configuring a Makelangelo machine
 *
 * @author danroyer
 * @since 7.1.4
 */
public class MakelangeloSettingsDialog
extends JPanel
implements ActionListener {
	/**
	 * See Serializable
	 */
	private static final long serialVersionUID = 1L;

	protected MakelangeloRobot robot;
	protected Frame parentFrame;
	protected JTabbedPane panes;
	protected JButton save, cancel;

	private JComboBox<String> hardwareVersionChoices;
	private ArrayList<String> availableHardwareVersions;
	private String[] hardwareVersionNames;
	private String originalHardwareVersion;

	private JPanel modelPanel;
	protected PanelAdjustMachine panelAdjustMachine;
	protected PanelAdjustPaper panelAdjustPaper;
	protected PanelAdjustPen panelAdjustPen;

	protected int dialogWidth = 450;
	protected int dialogHeight = 500;

	public MakelangeloSettingsDialog(Frame parent, MakelangeloRobot robot) {
		super();
		this.parentFrame = parent;
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
	  
	  int result = JOptionPane.showConfirmDialog(parentFrame, this, Translator.get("configureMachine"),
			  JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	  if (result == JOptionPane.OK_OPTION) {
		  panelAdjustMachine.save();
		  panelAdjustPaper.save();
		  panelAdjustPen.save();
		  robot.getSettings().saveConfig();
		  robot.sendConfig();
	  } else {
		  robot.getSettings().setHardwareVersion(originalHardwareVersion);
	  }
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
	  
	  findAvailableHardwareVersions();
	  
	  d.gridx=1;
	  d.gridwidth=2;
	  hardwareVersionChoices = new JComboBox<>(hardwareVersionNames);
	  // set the default
	  String hv = robot.getSettings().getHardwareVersion();
	  for(int i=0;i<availableHardwareVersions.size();++i) {
		  if(availableHardwareVersions.get(i).equals(hv)) {
			  hardwareVersionChoices.setSelectedIndex(i);
			  break;
		  }
	  }
	  modelPanel.add(hardwareVersionChoices, d);
	  hardwareVersionChoices.addActionListener(this);
  }

  private void findAvailableHardwareVersions() {
	  availableHardwareVersions = new ArrayList<String>();
	  
	  // get version numbers
	  ServiceLoader<MakelangeloHardwareProperties> knownHardware = ServiceLoader.load(MakelangeloHardwareProperties.class);
	  Iterator<MakelangeloHardwareProperties> i = knownHardware.iterator();
	  while(i.hasNext()) {
		  MakelangeloHardwareProperties hw = i.next();
		  availableHardwareVersions.add(new String(hw.getVersion()));
	  }

	  // get names
	  hardwareVersionNames = new String[availableHardwareVersions.size()];
	  i = knownHardware.iterator();
	  int j=0;
	  while(i.hasNext()) {
		  MakelangeloHardwareProperties hw = i.next();
		  hardwareVersionNames[j] = hw.getName();
		  ++j;
	  }	  
  }
  
  
  private void rebuildTabbedPanes() {
	  // returns tab index or -1 if none selected
	  int previouslySelectedTab = panes.getSelectedIndex();
	  panes.removeAll();
	  
	  panelAdjustMachine = new PanelAdjustMachine(robot);
	  panes.addTab(Translator.get("MenuSettingsMachine"),panelAdjustMachine.getPanel());

	  panelAdjustPaper = new PanelAdjustPaper(robot);
	  panes.addTab(Translator.get("MenuAdjustPaper"),panelAdjustPaper.getPanel());

	  panelAdjustPen = new PanelAdjustPen(robot);
	  panes.addTab(Translator.get("MenuAdjustTool"),panelAdjustPen.getPanel());

	  // if one tab was selected, make sure to reselect it	  
	  if(previouslySelectedTab!=-1) {
		  panes.setSelectedIndex(previouslySelectedTab);
	  }
  }
  
  public void actionPerformed(ActionEvent e) {
	  Object src = e.getSource();
	  
	  if(src == hardwareVersionChoices) {
		  String newChoice=availableHardwareVersions.get(hardwareVersionChoices.getSelectedIndex());
		  robot.getSettings().setHardwareVersion(newChoice);
		  rebuildTabbedPanes();
	  }
  }
}
