package com.marginallyclever.makelangeloRobot.settings;

import java.awt.FlowLayout;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties;

/**
 * Controls related to configuring a Makelangelo machine
 *
 * @author dan royer
 * @since 7.1.4
 */
public class MakelangeloSettingsDialog extends JDialog implements ActionListener {
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
		super(parent,Translator.get("configureMachine"));
		this.parentFrame = parent;
		this.robot = robot;
		
	}

	// display settings menu
	public void run() {
		originalHardwareVersion = robot.getSettings().getHardwareVersion();

		JPanel panel = new JPanel(new GridBagLayout());

		buildModelPanel();

		// hardware model settings
		panes = new JTabbedPane(JTabbedPane.TOP);
		panes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		// panes.setPreferredSize(new Dimension(dialogWidth,dialogHeight));

		rebuildTabbedPanes();

		JPanel bottom = new JPanel(new FlowLayout());
		JButton buttonSave = new JButton(Translator.get("Save"));
		JButton buttonCancel = new JButton(Translator.get("Cancel"));
		bottom.add(buttonSave);
		bottom.add(buttonCancel);

		// now assemble the dialog
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty=0;
		gbc.weightx=1;
		gbc.gridx=0;
		gbc.gridy=0;
		panel.add(modelPanel,gbc);
		gbc.gridy++;
		gbc.weighty=1;
		panel.add(panes,gbc);
		gbc.gridy++;
		gbc.weighty=0;
		panel.add(bottom,gbc);

		setResizable(false);
		setContentPane(panel);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
        setLocationRelativeTo(parentFrame);

		final JDialog dialog = this;
		
		buttonSave.addActionListener((e)->{
			panelAdjustMachine.save();
			panelAdjustPaper.save();
			panelAdjustPen.save();
			robot.getSettings().saveConfig();
			robot.sendConfig();
			dialog.setVisible(false);
		});
		buttonCancel.addActionListener((e)->{
			robot.getSettings().setHardwareVersion(originalHardwareVersion);
			dialog.setVisible(false);
		});
		
	}

	// hardware model choice
	private void buildModelPanel() {
		modelPanel = new JPanel(new GridBagLayout());
		modelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		GridBagConstraints d = new GridBagConstraints();
		// the panes for the selected machine configuration
		d.fill = GridBagConstraints.BOTH;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.weighty = 0;

		JLabel modelLabel = new JLabel(Translator.get("HardwareVersion"));
		modelLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		modelPanel.add(modelLabel, d);

		findAvailableHardwareVersions();

		d.gridx = 1;
		d.gridwidth = 2;
		hardwareVersionChoices = new JComboBox<String>(hardwareVersionNames);
		// set the default
		String hv = robot.getSettings().getHardwareVersion();
		for (int i = 0; i < availableHardwareVersions.size(); ++i) {
			if (availableHardwareVersions.get(i).equals(hv)) {
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
		ServiceLoader<MakelangeloHardwareProperties> knownHardware = ServiceLoader
				.load(MakelangeloHardwareProperties.class);
		Iterator<MakelangeloHardwareProperties> i = knownHardware.iterator();
		while (i.hasNext()) {
			MakelangeloHardwareProperties hw = i.next();
			availableHardwareVersions.add(new String(hw.getVersion()));
		}

		// get names
		hardwareVersionNames = new String[availableHardwareVersions.size()];
		i = knownHardware.iterator();
		int j = 0;
		while (i.hasNext()) {
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
		panes.addTab(Translator.get("MenuSettingsMachine"), panelAdjustMachine.getPanel());

		panelAdjustPaper = new PanelAdjustPaper(robot);
		panes.addTab(Translator.get("MenuAdjustPaper"), panelAdjustPaper.getPanel());

		panelAdjustPen = new PanelAdjustPen(robot);
		panes.addTab(Translator.get("MenuAdjustTool"), panelAdjustPen.getPanel());

		// if one tab was selected, make sure to reselect it
		if (previouslySelectedTab != -1) {
			panes.setSelectedIndex(previouslySelectedTab);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src == hardwareVersionChoices) {
			String newChoice = availableHardwareVersions.get(hardwareVersionChoices.getSelectedIndex());
			robot.getSettings().setHardwareVersion(newChoice);
			rebuildTabbedPanes();
			resizeDialog();
		}
	}

	private void resizeDialog() {
		this.pack();
	}
}
