package com.marginallyclever.convenience.log;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(LogPanel.class);

	private static final long serialVersionUID = -2753297349917155256L;

	private final JTextArea logArea = new JTextArea();

	public LogPanel() {
		this.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(logArea);
		this.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(1000, 400));
		File log = Log.getLogLocation();
		try (BufferedReader br = new BufferedReader(new FileReader(log))) {
			String b;
			while ((b = br.readLine()) != null) {
				if (b.trim().length() == 0)
					continue;
				logArea.append(b);
				logArea.append(System.lineSeparator());
			}
		} catch (IOException e) {
			logger.error("Failed to read log file {}", log, e);
			logArea.append("Failed to read log file: " + e.getLocalizedMessage());
		}
	}

	private String getText() {
		return logArea.getText();
	}

	public static void runAsDialog(JFrame frame) {
		JDialog dialog = new JDialog(frame, Log.getLogLocation().toString(), Dialog.ModalityType.DOCUMENT_MODAL);

		JButton copyClipboardButton = new JButton(Translator.get("CopyClipboard"));

		JPanel outerPanel = new JPanel(new BorderLayout());
		LogPanel logPanel = new LogPanel();
		outerPanel.add(logPanel,BorderLayout.CENTER);

		outerPanel.add(copyClipboardButton,BorderLayout.SOUTH);

		copyClipboardButton.addActionListener((e)-> {
			StringSelection stringSelection = new StringSelection(logPanel.getText());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
		});

		dialog.add(outerPanel);
		dialog.pack();
		dialog.setVisible(true);
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		Translator.start();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		runAsDialog(frame);
	}
}
