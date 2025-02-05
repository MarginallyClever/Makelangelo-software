package com.marginallyclever.makelangelo;

import com.marginallyclever.donatello.select.SelectReadOnlyText;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;

/**
 * A panel that displays localized information about this app.
 */
public class AboutPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(AboutPanel.class);

	public AboutPanel(String versionString, String detailedVersion) {
		super(new BorderLayout());

		String aboutHtml = Translator.get("DialogAbout.AboutHTML")
				.replace("%VERSION%", versionString)
				.replace("%DETAILED_VERSION%", detailedVersion);

		final JTextComponent bottomText = SelectReadOnlyText.createJEditorPaneWithHyperlinkListenerAndToolTipsForDesktopBrowse(aboutHtml);
		ImageIcon icon = getImageIcon("logo.png");
		if (icon == null) {
			icon = getImageIcon("resources/logo.png");
		}

		add(bottomText, BorderLayout.CENTER);
	}

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
}
