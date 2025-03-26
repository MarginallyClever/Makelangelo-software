package com.marginallyclever.convenience.log;

import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class LogPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(LogPanel.class);

	private final JTextArea logArea = new JTextArea();
	private final JComboBox<File> filesJComboBox = new JComboBox<>();

	public LogPanel() {
		this.setLayout(new BorderLayout());
		JPanel innerPanel = new JPanel(new BorderLayout());
		this.add(innerPanel);

		Arrays.stream(Objects.requireNonNull(Log.logDir.listFiles((dir1, name) -> name.matches(Log.LOG_FILE_NAME_PATTERN))))
				.sorted(Comparator.reverseOrder())
				.forEach(logFile -> {
					if (logFile.exists() && logFile.isFile()) {
						filesJComboBox.addItem(logFile);
					}
				});
		filesJComboBox.addItemListener(e -> displayLog((File) e.getItem()));

		JPanel topPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(Translator.get("LogPanel.LogFiles"));
		label.setBorder(new EmptyBorder(0,10,0,10));//top,left,bottom,right
		topPanel.add(label, BorderLayout.LINE_START);
		topPanel.add(filesJComboBox, BorderLayout.CENTER);

		innerPanel.add(topPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(logArea);
		innerPanel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(1000, 400));

		displayLog(Log.getLogLocation());

		ButtonIcon copyClipboardButton = new ButtonIcon(Translator.get("LogPanel.CopyClipboard"), "/images/page_copy.png");
		copyClipboardButton.addActionListener((e)-> {
			StringSelection stringSelection = new StringSelection(logArea.getText());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
		});
		innerPanel.add(copyClipboardButton ,BorderLayout.SOUTH);
	}

	private void displayLog(File log) {
		logArea.setText("");
		try (BufferedReader br = new BufferedReader(new FileReader(log))) {
			String b;
			while ((b = br.readLine()) != null) {
				if (b.trim().isEmpty())
					continue;
				logArea.append(b);
				logArea.append(System.lineSeparator());
			}
		} catch (IOException e) {
			logger.error("Failed to read log file {}", log, e);
			logArea.append("Failed to read log file: " + e.getLocalizedMessage());
		}
	}

	public static void runAsDialog(Window frame) {
		JDialog dialog = new JDialog(frame, Translator.get("LogPanel.Title"), Dialog.ModalityType.DOCUMENT_MODAL);

		JPanel outerPanel = new JPanel(new BorderLayout());
		LogPanel logPanel = new LogPanel();
		outerPanel.add(logPanel,BorderLayout.CENTER);

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
