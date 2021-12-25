package com.marginallyclever.makelangelo;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
	 * <p>
	 * Uses {@link java.lang.StringBuilder#append(String)} to create an internationalization supported {@code String}
	 * representing the About Message Dialog's HTML.
	 * </p>
	 * <p>
	 * <p>
	 * The summation of {@link String#length()} for each of the respective values retrieved with the
	 * {@code "AboutHTMLBeforeVersionNumber"}, and {@code "AboutHTMLAfterVersionNumber"} {@link Translator} keys,
	 * in conjunction with {@link Makelangelo#VERSION} is calculated for use with {@link java.lang.StringBuilder#StringBuilder(int)}.
	 * </p>
	 *
	 * @return An HTML string used for the About Message Dialog.
	 */
	private String getAboutHtmlFromMultilingualString() {
		return Translator.get("AboutHTML");
	}

	/**
	 * @param html String of valid HTML.
	 * @return a
	 */
	private JTextComponent createHyperlinkListenableJEditorPane(String html) {
		final JEditorPane bottomText = new JEditorPane();
		bottomText.setContentType("text/html");
		bottomText.setEditable(false);
		bottomText.setText(html);
		bottomText.setOpaque(false);
		final HyperlinkListener hyperlinkListener = new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
				if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							URI u = hyperlinkEvent.getURL().toURI();
							Desktop.getDesktop().browse(u);
						} catch (IOException | URISyntaxException e) {
							logger.error("Failed to open the browser to the url", e);
						}
					}

				}
			}
		};
		bottomText.addHyperlinkListener(hyperlinkListener);
		return bottomText;
	}


	/**
	 * Display the about dialog.
	 */
	public void display(Component parent,String versionString) {
		String aboutHtml = getAboutHtmlFromMultilingualString();
		aboutHtml = aboutHtml.replace("%VERSION%",versionString);
		
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(aboutHtml);
		ImageIcon icon = getImageIcon("logo.png");
		if (icon == null) {
			icon = getImageIcon("resources/logo.png");
		}
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("MenuAbout"), JOptionPane.INFORMATION_MESSAGE, icon);
	}
}
