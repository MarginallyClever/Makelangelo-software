package com.marginallyclever.makelangelo.apps;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsListener;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Collection;

/**
 * {@link PlotterSettingsManagerPanel} manages the list of available machine configurations.
 * A single machine configuration must be loaded into the active {@link Plotter} at any given time.
 * TODO changes to this panel should be immediately reflected in the preview panel.
 * @author Dan Royer
 */
public class PlotterSettingsManagerPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsManagerPanel.class);

	private final PlotterSettingsManager plotterSettingsManager;
	private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
	private final JComboBox<String> configurationList = new JComboBox<>(model);
	private final JPanel container = new JPanel(new BorderLayout());
	private PlotterSettingsPanel plotterSettingsPanel = null;
	private PlotterSettingsListener listener;

	public PlotterSettingsManagerPanel() {
		this(new PlotterSettingsManager());
	}

	public PlotterSettingsManagerPanel(PlotterSettingsManager plotterSettingsManager) {
		super(new BorderLayout());
		this.plotterSettingsManager = plotterSettingsManager;

		model.addAll(plotterSettingsManager.getProfileNames());

		Component topButtons = createToolBar();

		this.add(topButtons,BorderLayout.NORTH);
		this.add(container,BorderLayout.CENTER);

		container.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		if(model.getSize()>0) {
			PlotterSettings lastSelectedProfile = plotterSettingsManager.getLastSelectedProfile();
			model.setSelectedItem(lastSelectedProfile.getUID());
		}
	}

	private Component createToolBar() {
		JToolBar topButtons = new JToolBar();

		topButtons.add(configurationList);
		configurationList.setName("configurationList");
		configurationList.addActionListener((e)->changeProfile((String)configurationList.getSelectedItem()));

		JButton add = new JButton(new AbstractAction(Translator.get("PlotterSettingsManagerPanel.AddProfile")) {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				// copy the current profile and rename the new instance.
				runRenameProfileDialog((String)model.getSelectedItem());
			}
		});
		add.setName("addProfile");
		topButtons.add(add);

		JButton remove = new JButton(new AbstractAction(Translator.get("PlotterSettingsManagerPanel.RemoveProfile")) {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				deleteProfile((String)model.getSelectedItem());
			}
		});
		add.setName("removeProfile");
		topButtons.add(remove);

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
				goAgain = copyAndRenameProfile(uid,newUID);
			}
		} while(goAgain);
	}

	/**
	 * Creates a copy of the current profile, changes the RobotUID, and saves it as a new instance.  Does not change the
	 * old profile.
	 *
	 * @param oldUID the name of the profile to copy
	 * @param newUID the name of the new profile
	 * @return true if there was a problem.
	 */
	private boolean copyAndRenameProfile(String oldUID, String newUID) {
		plotterSettingsManager.saveAs(oldUID,newUID);
		// in with the new
		model.addElement(newUID);
		model.setSelectedItem(newUID);
		return false;
	}

	/**
	 * Checks if the given name is already in use.
	 * @param newUID the name to check
	 * @return true if the name is already in use.
	 */
	private boolean nameIsTaken(String newUID) {
		Collection<String> list = plotterSettingsManager.getProfileNames();
		return list.contains(newUID);
	}

	private void deleteProfile(String uid) {
		PlotterSettings me = new PlotterSettings(uid);
		String ancestorName = me.getString(PlotterSettings.ANCESTOR);
		if(!plotterSettingsManager.deleteProfile(uid)) {
			model.removeElement(uid);
			model.setSelectedItem(ancestorName);
		}
	}

	/**
	 * Swap the active profile.
	 */
	private void changeProfile(String name) {
		logger.debug("changing profile to {}",name);
		if(plotterSettingsPanel!=null) {
			plotterSettingsPanel.removeListener(this::firePlotterSettingsChanged);
			container.remove(plotterSettingsPanel);
		}
		plotterSettingsManager.setLastSelectedProfile(name);
		PlotterSettings plotterSettings = new PlotterSettings(name);
		plotterSettingsPanel = new PlotterSettingsPanel(plotterSettings);
		plotterSettingsPanel.addListener(this::firePlotterSettingsChanged);
		container.add(plotterSettingsPanel,BorderLayout.CENTER);
		firePlotterSettingsChanged(plotterSettings);
		container.revalidate();
	}

	/**
	 * Add a listener to be notified when the settings change.
	 * @param listener the listener to add
	 */
	public void addListener(PlotterSettingsListener listener) {
		this.listener = listener;
	}

	/**
	 * Fire a settings changed event.
	 * @param settings the new settings
	 */
	private void firePlotterSettingsChanged(PlotterSettings settings) {
		if(listener!=null) {
			listener.settingsChangedEvent(settings);
		}
	}

	/**
	 * Test the PlotterSettingsManagerPanel
	 * @param args not used
	 */
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			logger.warn("failed to set native look and feel.", ex);
		}

		JFrame frame = new JFrame(PlotterSettingsManagerPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(new PlotterSettingsManagerPanel());
		frame.setMinimumSize(new Dimension(350,300));
		frame.pack();
		frame.setVisible(true);
	}
}
