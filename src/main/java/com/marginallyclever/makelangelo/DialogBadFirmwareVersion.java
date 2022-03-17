package com.marginallyclever.makelangelo;

import com.marginallyClever.makelangelo.select.SelectReadOnlyText;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

@Deprecated
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
		return Translator.get("firmwareVersionBadMessage", new String[]{version});
	}

	public void display(Component parent,String version) {
		final String aboutHtml = getAboutHtmlFromMultilingualString(version);
		final JTextComponent bottomText = SelectReadOnlyText.createJEditorPaneWithHyperlinkListenerAndToolTipsForDesktopBrowse(aboutHtml);
		JOptionPane.showMessageDialog(parent, bottomText, Translator.get("firmwareVersionBadTitle"), JOptionPane.ERROR_MESSAGE);
	}
}
