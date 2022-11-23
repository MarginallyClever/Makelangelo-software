package com.marginallyclever.makelangelo.plotter.settings;

import java.awt.*;
import java.io.Serial;
import java.util.Collection;
import java.util.prefs.BackingStoreException;

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
		topButtons.add(new JButton(new AbstractAction(Translator.get("PlotterSettingsManagerPanel.AddProfile")) {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// copy the current profile and rename the new instance.
				runRenameProfileDialog((String)model.getSelectedItem());
			}
		}));
		topButtons.add(new JButton(new AbstractAction(Translator.get("PlotterSettingsManagerPanel.RemoveProfile")) {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				removeProfile((String)model.getSelectedItem());
			}
		}));
		return topButtons;
	}

	private void runRenameProfileDialog(String uid) {
		boolean goAgain;

		do {
			String newUID = JOptionPane.showInputDialog(this, Translator.get("PlotterSettingsManagerPanel.NewProfileName"), uid);
			if( newUID.isEmpty() || newUID.isBlank() ) {
				JOptionPane.showMessageDialog(this, Translator.get("PlotterSettingsManagerPanel.NewProfileNameCannotBeBlank"));
				goAgain = true;
			}
			if( nameIsTaken(newUID) ) {
				JOptionPane.showMessageDialog(this, Translator.get("PlotterSettingsManagerPanel.NewProfileNameAlreadyExists"));
				goAgain = true;
			} else {
				// found a unique name.  try to update the backing store.
				goAgain = renameProfile(uid,newUID);
			}
		} while(goAgain);
	}

	/**
	 * Creates a copy of the current profile, changes the RobotUID, and saves it as a new instance.  Does not change the
	 * old profile.
	 * TODO needs a unit test
	 * @param oldUID
	 * @param newUID
	 * @return true if there was a problem.
	 */
	private boolean renameProfile(String oldUID,String newUID) {
		PlotterSettings ps = plotterSettingsManager.loadProfile(oldUID);
		ps.setRobotUID(newUID);
		try {
			ps.saveConfig();
		} catch(Exception e) {
			logger.error("failed to rename {} to {}. {}",oldUID,newUID,e);
			return true;
		}

		// in with the new
		plotterSettingsManager.loadAllProfiles();
		model.addElement(newUID);
		return false;
	}

	// TODO could use a unit test
	private boolean nameIsTaken(String newUID) {
		Collection<String> list = plotterSettingsManager.getProfileNames();
		return list.contains(newUID);
	}

	private void removeProfile(String uid) {
		if(plotterSettingsManager.deleteProfile(uid)) {
			model.removeElement(uid);
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
		frame.setMinimumSize(new Dimension(350,300));
		frame.pack();
		frame.setVisible(true);
	}
}
