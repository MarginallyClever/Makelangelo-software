package com.marginallyclever.makelangelo;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.IOUtils;

public class DialogAbout {

	/**
	 * @return byte array containing data for image icon.
	 */
	private ImageIcon getImageIcon(String iconResourceName) {
		ImageIcon icon = null;
		try {
			final byte[] imageData = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(iconResourceName));
			icon = new ImageIcon(imageData);
		} catch (NullPointerException | IOException e) {
			Log.error("Error getting image icon: " + e);
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
	private String getAboutHtmlFromMultilingualString(Translator translator, String version) {
		final String aboutHtmlBeforeVersionNumber = Translator.get("AboutHTMLBeforeVersionNumber");
		final String aboutHmlAfterVersionNumber = Translator.get("AboutHTMLAfterVersionNumber");
		final int aboutHTMLBeforeVersionNumberLength = aboutHtmlBeforeVersionNumber.length();
		final int versionNumberStringLength = version.length();
		final int aboutHtmlAfterVersionNumberLength = aboutHmlAfterVersionNumber.length();
		final int aboutHtmlStringBuilderCapacity = aboutHTMLBeforeVersionNumberLength + versionNumberStringLength + aboutHtmlAfterVersionNumberLength;
		final StringBuilder aboutHtmlStringBuilder = new StringBuilder(aboutHtmlStringBuilderCapacity);
		aboutHtmlStringBuilder.append(aboutHtmlBeforeVersionNumber);
		aboutHtmlStringBuilder.append(version);
		aboutHtmlStringBuilder.append(aboutHmlAfterVersionNumber);
		return aboutHtmlStringBuilder.toString();
	}

	/**
	 * @param html String of valid HTML.
	 * @return a
	 */
	public JTextComponent createHyperlinkListenableJEditorPane(String html) {
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
							Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
						} catch (IOException | URISyntaxException exception) {
							// Auto-generated catch block
							exception.printStackTrace();
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
	public void display(Translator translator,String version) {
		final String aboutHtml = getAboutHtmlFromMultilingualString(translator,version);
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(aboutHtml);
		ImageIcon icon = getImageIcon("logo.png");
		final String menuAboutValue = Translator.get("MenuAbout");
		if (icon == null) {
			icon = getImageIcon("resources/logo.png");
		}
		JOptionPane.showMessageDialog(null, bottomText, menuAboutValue, JOptionPane.INFORMATION_MESSAGE, icon);
	}
}
