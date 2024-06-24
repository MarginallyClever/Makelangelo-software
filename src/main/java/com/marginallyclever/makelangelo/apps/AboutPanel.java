package com.marginallyclever.makelangelo.apps;

import com.marginallyclever.makelangelo.MakelangeloVersion;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class AboutPanel extends JPanel {
	public AboutPanel() {
		super();
		String aboutHtml = Translator.get("DialogAbout.AboutHTML")
				.replace("%VERSION%", MakelangeloVersion.VERSION)
				.replace("%DETAILED_VERSION%", MakelangeloVersion.DETAILED_VERSION);

		JTextComponent bottomText = SelectReadOnlyText.createJEditorPaneWithHyperlinkListenerAndToolTipsForDesktopBrowse(aboutHtml);
		add(bottomText, BorderLayout.CENTER);
	}
}
