package com.marginallyclever.makelangelo.makelangeloSettingsPanel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * Application settings
 * @author Dan Royer
 *
 */
public class MakelangeloSettingPanel {
	private static final Logger logger = LoggerFactory.getLogger(MakelangeloSettingPanel.class);
	private transient JPanel panel; 
	private transient JButton buttonExport;
	private transient JButton buttonImport;
	private transient JButton buttonReset;
	
	public MakelangeloSettingPanel() {
		super();
	}
	
	public void run(Component parentComponent) {
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();  
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;

		buttonImport = new JButton(Translator.get("Import"));
		panel.add(buttonImport,c);
		buttonImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importPreferences(parentComponent);
			}
		});
		c.gridx++;

		buttonExport = new JButton(Translator.get("Export"));
		panel.add(buttonExport,c);
		buttonExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportPreferences(parentComponent);
			}
		});
		c.gridx++;

		buttonReset = new JButton(Translator.get("Reset"));
		panel.add(buttonReset,c);
		buttonReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetPreferences(parentComponent);
			}
		});
		c.gridx++;

		c.gridy=1;
		c.gridx=0;
		c.gridwidth=3;
		
		JTabbedPane pane = new JTabbedPane();
		pane.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		panel.add(pane,c);
		pane.add(Translator.get("MenuSoundsTitle"), SoundPreferences.buildPanel());
		pane.add(Translator.get("MenuGraphicsTitle"), GFXPreferences.buildPanel());
		pane.add(Translator.get("MenuLanguageTitle"), LanguagePreferences.buildPanel());
		pane.add(Translator.get("MenuMetricsTitle"), MetricsPreferences.buildPanel());

		
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

	@SuppressWarnings("deprecation")
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
	

	@SuppressWarnings("deprecation")
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
	

	@SuppressWarnings("deprecation")
	private void resetPreferences(Component parentComponent) {
		int dialogResult = JOptionPane.showConfirmDialog(parentComponent, Translator.get("MenuResetMachinePreferencesWarning"), Translator.get("MenuResetMachinePreferencesWarningHeader"), JOptionPane.YES_NO_OPTION);
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
