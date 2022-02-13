package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
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
	 * @return An HTML string used for the About Message Dialog.
	 */
	private String getAboutHtmlFromMultilingualString() {
		return Translator.get("AboutHTML");
	}

	/**
	 * Display the about dialog.
	 */
	public void display(Component parent,String versionString) {
		String aboutHtml = getAboutHtmlFromMultilingualString();
		aboutHtml = aboutHtml.replace("%VERSION%",versionString);
		
		final JTextComponent bottomText = SelectReadOnlyText.createJEditorPaneWithHyperlinkListenerAndToolTipsForDesktopBrowse(aboutHtml);
		ImageIcon icon = getImageIcon("logo.png");
		if (icon == null) {
			icon = getImageIcon("resources/logo.png");
		}
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("MenuAbout"), JOptionPane.INFORMATION_MESSAGE, icon);
	}
}
