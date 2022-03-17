package com.marginallyclever.makelangelo.plotter.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.util.PreferencesHelper;

/**
 * {@link PlotterSettingsManager} manages the list of available machine configurations.
 * A single machine configuration must be loaded into the active {@link Plotter} at any given time. 
 * @author Dan Royer
 */
public class PlotterSettingsManager extends JPanel {
	private static final long serialVersionUID = 7163572330672713872L;
	private static final Logger logger = LoggerFactory.getLogger(PlotterSettingsManager.class);
	
	private List<String> configurationNames = new ArrayList<>();

	public PlotterSettingsManager() {
		super();
		loadAllConfigurations();
	}
	
	private void loadAllConfigurations() {
		configurationNames.clear();
		
		try {
			Preferences topLevelMachinesPreferenceNode = PreferencesHelper
					.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
			configurationNames = Arrays.asList( topLevelMachinesPreferenceNode.childrenNames() );
		} catch (Exception e) {
			logger.error("Failed to load preferences", e);
			configurationNames.add("Default");
		}
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(PlotterSettingsManager.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(new PlotterSettingsManager());
		frame.pack();
		frame.setVisible(true);
	}
}
