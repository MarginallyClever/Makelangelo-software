package com.marginallyclever.makelangelo.applicationsettings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * Application settings
 * @author Dan Royer
 */
public class ApplicationSettings {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationSettings.class);
	
	public ApplicationSettings() {
		super();
	}
	
	public void run(Component parentComponent) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		JPanel top = new JPanel(new FlowLayout());
		top.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		JButton buttonImport = new JButton(Translator.get("Import"));
		top.add(buttonImport);
		buttonImport.addActionListener(e -> importPreferences(parentComponent));

		JButton buttonExport = new JButton(Translator.get("Export"));
		top.add(buttonExport);
		buttonExport.addActionListener(e -> exportPreferences(parentComponent));

		JButton buttonReset = new JButton(Translator.get("Reset"));
		top.add(buttonReset);
		buttonReset.addActionListener(e -> resetPreferences(parentComponent));

		panel.add(top,BorderLayout.NORTH);

		JTabbedPane pane = new JTabbedPane();
		pane.add(Translator.get("MenuSoundsTitle"), SoundPreferences.buildPanel());
		pane.add(Translator.get("MenuGraphicsTitle"), GFXPreferences.buildPanel());
		pane.add(Translator.get("MenuLanguageTitle"), LanguagePreferences.buildPanel());
		pane.add(Translator.get("MenuMetricsTitle"), MetricsPreferences.buildPanel());
		panel.add(pane,BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(parentComponent, panel, Translator.get("MenuPreferences"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			SoundPreferences.save();
			GFXPreferences.save();
			LanguagePreferences.save();
			MetricsPreferences.save();
		} else {
			SoundPreferences.cancel();
			GFXPreferences.cancel();
			LanguagePreferences.cancel();
			MetricsPreferences.cancel();
		}
	}

	private void exportPreferences(Component parentComponent) {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(parentComponent);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			try (final OutputStream fileOutputStream = new FileOutputStream(file)) {
				Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
				prefs.exportSubtree(fileOutputStream);
			} catch (IOException | BackingStoreException e) {
				logger.error("Failed to export preferences to {}", file, e);
			}
		}
	}
	
	private void importPreferences(Component parentComponent) {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(parentComponent);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			try (final InputStream fileInputStream = new FileInputStream(file)) {
				Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
				prefs.flush();
				Preferences.importPreferences(fileInputStream);
				prefs.flush();
			} catch (IOException | InvalidPreferencesFormatException | BackingStoreException e) {
				logger.error("Failed to import preferences from {}", file, e);
			}
		}
	}

	private void resetPreferences(Component parentComponent) {
		int dialogResult = JOptionPane.showConfirmDialog(parentComponent, Translator.get("MenuResetMachinePreferencesWarning"), Translator.get("QuestionTitle"), JOptionPane.YES_NO_OPTION);
		if(dialogResult == JOptionPane.YES_OPTION){
			try {
				Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
				prefs.removeNode();
				Preferences.userRoot().flush();
				PreferencesHelper.start();
			} catch (BackingStoreException e) {
				logger.error("Failed to reset preferences", e);
			}
		}
	}
}
