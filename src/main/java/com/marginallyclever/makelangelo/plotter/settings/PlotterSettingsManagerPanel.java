package com.marginallyclever.makelangelo.plotter.settings;

import java.awt.*;
import java.io.Serial;
import java.util.prefs.BackingStoreException;

import javax.print.attribute.standard.JobMessageFromOperator;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.util.PreferencesHelper;

/**
 * {@link PlotterSettingsManagerPanel} manages the list of available machine configurations.
 * A single machine configuration must be loaded into the active {@link Plotter} at any given time. 
 * @author Dan Royer
 */
public class PlotterSettingsManagerPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 7163572330672713872L;
	private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsManagerPanel.class);

	private final PlotterSettingsManager plotterSettingsManager;

	private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
	private final JComboBox<String> configurationList = new JComboBox<>(model);

	private PlotterSettingsPanel plotterSettingsPanel = null;

	public PlotterSettingsManagerPanel(PlotterSettingsManager plotterSettingsManager) {
		super(new BorderLayout());
		this.plotterSettingsManager = plotterSettingsManager;

		model.addAll(plotterSettingsManager.getProfileNames());

		Component topButtons = createTopButtons();
		this.add(topButtons,BorderLayout.NORTH);

		configurationList.addActionListener((e)->changeProfile());
		if(model.getSize()>0) {
			PlotterSettings lastSelectedProfile = plotterSettingsManager.getLastSelectedProfile();
			model.setSelectedItem(lastSelectedProfile.getUID());
		}
	}

	private Component createTopButtons() {
		JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topButtons.add(configurationList);
		topButtons.add(new JButton(new AbstractAction("+") {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				PlotterSettings ps = new PlotterSettings();
				String newUID = JOptionPane.showInputDialog(this, Translator.get("PlotterSettingsManagerPanel.NewProfileName"));
				if(!newUID.contentEquals(ps.getUID())) {
					ps.setRobotUID(newUID);
					ps.saveConfig();
					model.addElement(newUID);
				}
			}
		}));
		topButtons.add(new JButton(new AbstractAction("-") {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String uid = (String)model.getSelectedItem();
				if(!testForDefaultProfile(uid)) {
					removeProfile(uid);
				}
			}
		}));
		return topButtons;
	}

	private boolean testForDefaultProfile(String uid) {
		// TODO make this more sophisticated later
		return false;
	}

	private void removeProfile(String uid) {
		model.removeElement(uid);
		try {
			plotterSettingsManager.deleteProfile(uid);
		} catch (BackingStoreException ex) {
			logger.error("Failed to delete profile", ex);
		}
	}

	private void changeProfile() {
		String name = (String)configurationList.getSelectedItem();
		if(name!=null) {
			logger.debug("changing profile to {}",name);
			if(plotterSettingsPanel!=null) {
				this.remove(plotterSettingsPanel);
			}
			PlotterSettings plotterSettings = plotterSettingsManager.loadProfile(name);
			plotterSettingsPanel = new PlotterSettingsPanel(plotterSettings);
			this.add(plotterSettingsPanel,BorderLayout.CENTER);
			this.revalidate();
		}
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(PlotterSettingsManagerPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(new PlotterSettingsManagerPanel(new PlotterSettingsManager()));
		frame.pack();
		frame.setVisible(true);
	}
}
