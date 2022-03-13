package com.marginallyClever.makelangelo;

import com.marginallyClever.makelangelo.select.SelectReadOnlyText;
import com.marginallyClever.util.PreferencesHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;

public class DialogAbout {
	private static final Logger logger = LoggerFactory.getLogger(DialogAbout.class);
	/**
	 * @return byte array containing data for image icon.
	 */
	private ImageIcon getImageIcon(String iconResourceName) {
		ImageIcon icon = null;
		try {
			final byte[] imageData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(iconResourceName));
			icon = new ImageIcon(imageData);
		} catch (NullPointerException | IOException e) {
			logger.warn("Error getting image icon {}", iconResourceName, e);
		}
		return icon;
	}

	/**
	 * Display the about dialog.
	 */
	public void display(Component parent, String versionString, String detailedVersion) {
		String aboutHtml = Translator.get("DialogAbout.AboutHTML")
				.replace("%VERSION%", versionString)
				.replace("%DETAILED_VERSION%", detailedVersion);
		
		final JTextComponent bottomText = SelectReadOnlyText.createJEditorPaneWithHyperlinkListenerAndToolTipsForDesktopBrowse(aboutHtml);
		ImageIcon icon = getImageIcon("logo.png");
		if (icon == null) {
			icon = getImageIcon("resources/logo.png");
		}
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("MenuAbout"), JOptionPane.INFORMATION_MESSAGE, icon);
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		Translator.start();
		DialogAbout a = new DialogAbout();
		a.display(null, "7.31.0", "hash-ee8c91a-dirty");
	}
}
