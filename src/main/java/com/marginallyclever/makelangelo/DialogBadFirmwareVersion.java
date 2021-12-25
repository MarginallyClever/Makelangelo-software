package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

@Deprecated
public class DialogBadFirmwareVersion {
	private static final Logger logger = LoggerFactory.getLogger(DialogBadFirmwareVersion.class);
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
		return Translator.get("firmwareVersionBadMessage", new String[]{version});
	}

	/**
	 * @param html String of valid HTML.
	 * @return a text component with clickable links.
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
							Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
						} catch (IOException | URISyntaxException e) {
							logger.error("Failed to open the browser to the firmware url", e);
						}
					}

				}
			}
		};
		bottomText.addHyperlinkListener(hyperlinkListener);
		return bottomText;
	}


	public void display(Component parent,String version) {
		final String aboutHtml = getAboutHtmlFromMultilingualString(version);
		final JTextComponent bottomText = createHyperlinkListenableJEditorPane(aboutHtml);
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("firmwareVersionBadTitle"), JOptionPane.ERROR_MESSAGE);
	}
}
