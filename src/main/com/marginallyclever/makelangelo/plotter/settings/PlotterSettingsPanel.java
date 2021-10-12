package com.marginallyclever.makelangelo.plotter.settings;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.plotterTypes.PlotterTypeFactory;

/**
 * Controls related to configuring a Makelangelo machine
 *
 * @author dan royer
 * @since 7.1.4
 */
@Deprecated
public class PlotterSettingsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private Plotter robot;

	private JComboBox<String> hardwareVersionChoices;
	private ArrayList<String> availableHardwareVersions = PlotterTypeFactory.getHardwareVersions();
	private ArrayList<String> hardwareVersionNames = PlotterTypeFactory.getNames();

	private JTabbedPane panes = new JTabbedPane();
	private JPanel modelPanel;
	private JButton buttonSave;
	private JButton buttonCancel;
	
	public PlotterSettingsPanel(Plotter robot) {
		super();
		this.robot = robot;

		buttonSave = new JButton(Translator.get("Save"));
		buttonCancel = new JButton(Translator.get("Cancel"));

		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(buttonSave);
		bottom.add(buttonCancel);

		buildModelPanel();

		// hardware model settings
		panes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		// panes.setPreferredSize(new Dimension(450,500));

		rebuildTabbedPanes();


		// now assemble the dialog
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty=0;
		gbc.weightx=1;
		gbc.gridx=0;
		gbc.gridy=0;
		this.add(modelPanel,gbc);
		gbc.gridy++;
		gbc.weighty=1;
		this.add(panes,gbc);
		gbc.gridy++;
		gbc.weighty=0;
		this.add(bottom,gbc);

		buttonSave.addActionListener((e)->{
			robot.getSettings().saveConfig();
			//robot.sendConfig();
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

		d.gridx = 1;
		d.gridwidth = 2;
		
		hardwareVersionChoices = new JComboBox<String>((String[])hardwareVersionNames.toArray());
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

	private void rebuildTabbedPanes() {
		// returns tab index or -1 if none selected
		int previouslySelectedTab = panes.getSelectedIndex();
		panes.removeAll();

		PanelAdjustMachine panelAdjustMachine = new PanelAdjustMachine(robot);
		panes.addTab(Translator.get("MenuSettingsMachine"), panelAdjustMachine.getPanel());
		buttonSave.addActionListener((e)-> panelAdjustMachine.save() );

		//PanelAdjustPaper panelAdjustPaper = new PanelAdjustPaper(myPaper);
		//panes.addTab(Translator.get("MenuAdjustPaper"), panelAdjustPaper.getPanel());
		//buttonSave.addActionListener((e)-> panelAdjustPaper.save() );

		PenSettingsPanel panelAdjustPen = new PenSettingsPanel(robot);
		panes.addTab(Translator.get("MenuAdjustTool"), panelAdjustPen.getPanel());
		buttonSave.addActionListener((e)-> panelAdjustPen.save() );

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
		}
	}
}
