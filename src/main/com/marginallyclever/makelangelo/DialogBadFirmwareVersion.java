package com.marginallyclever.makelangelo;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

public class DialogBadFirmwareVersion {
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
	private String getAboutHtmlFromMultilingualString(String version) {
		String message = Translator.get("firmwareVersionBadMessage");
		return message.replace("%VERSION%", version);
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
	public void display(Component parent,String version) {
		final String aboutHtml = getAboutHtmlFromMultilingualString(version);
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(aboutHtml);
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("firmwareVersionBadTitle"), JOptionPane.ERROR_MESSAGE);
	}
}
