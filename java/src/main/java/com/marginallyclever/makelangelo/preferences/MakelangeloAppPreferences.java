package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class MakelangeloAppPreferences implements ActionListener {
	transient private Makelangelo app;
	transient private SoundPreferences sound;
	transient private GFXPreferences gfx;
	transient private LanguagePreferences language;
	
	transient private JPanel panel; 
	transient private JButton buttonExport;
	transient private JButton buttonImport;
	transient private JButton buttonReset;
	
	public MakelangeloAppPreferences(Makelangelo arg0) {
		app=arg0;
		
		sound = new SoundPreferences(app.getMainFrame());
		gfx = new GFXPreferences(app.getMainFrame());
		language = new LanguagePreferences(app.getMainFrame());
	}
	
	public void run() {
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();  
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.WEST;

		buttonImport = new JButton(Translator.get("Import"));
		panel.add(buttonImport,c);
		c.gridx++;

		buttonExport = new JButton(Translator.get("Export"));
		panel.add(buttonExport,c);
		c.gridx++;

		buttonReset = new JButton(Translator.get("Reset"));
		panel.add(buttonReset,c);
		c.gridx++;

		c.gridy=1;
		c.gridx=0;
		c.gridwidth=3;
		
		sound.buildPanel();
		gfx.buildPanel();
		language.buildPanel();
		
		JTabbedPane pane = new JTabbedPane();
		pane.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		panel.add(pane,c);
		pane.add(Translator.get("MenuSoundsTitle"), sound);
		pane.add(Translator.get("MenuGraphicsTitle"), gfx);
		pane.add(Translator.get("MenuLanguageTitle"), language);

		
		int result = JOptionPane.showConfirmDialog(app.getMainFrame(), panel, Translator.get("MenuPreferences"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			sound.save();
			gfx.save();
			language.save();
			
			app.getRobot().setShowPenUp(gfx.getShowPenUp());
			app.getRobot().getControlPanel().updateButtonAccess();
		} else {
			sound.cancel();
			gfx.cancel();
			language.cancel();
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		if (subject == buttonExport) exportPreferences();
		if (subject == buttonImport) importPreferences();
		if (subject == buttonReset) resetPreferences();
	}
	
	private void exportPreferences() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(app.getMainFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			try (final OutputStream fileOutputStream = new FileOutputStream(file)) {
				Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
				prefs.exportSubtree(fileOutputStream);
			} catch (IOException | BackingStoreException pe) {
				Log.error(pe.getMessage());
			}
		}
	}
	
	
	private void importPreferences() {
		final JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(app.getMainFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			try (final InputStream fileInputStream = new FileInputStream(file)) {
				Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
				prefs.flush();
				Preferences.importPreferences(fileInputStream);
				prefs.flush();
			} catch (IOException | InvalidPreferencesFormatException | BackingStoreException pe) {
				Log.error(pe.getMessage());
			}
		}
	}
	
	
	private void resetPreferences() {
		int dialogResult = JOptionPane.showConfirmDialog(app.getMainFrame(), Translator.get("MenuResetMachinePreferencesWarning"), Translator.get("MenuResetMachinePreferencesWarningHeader"), JOptionPane.YES_NO_OPTION);
		if(dialogResult == JOptionPane.YES_OPTION){
			try {
				Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
				prefs.removeNode();
				Preferences.userRoot().flush();
			} catch (BackingStoreException e1) {
				Log.error(e1.getMessage());
			}
		}
	}
	
	public GFXPreferences getGraphics() {
		return gfx;
	}
}
