package com.marginallyclever.convenience.log;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(LogPanel.class);

	private static final long serialVersionUID = -2753297349917155256L;

	public LogPanel() {
		JTextArea logArea = new JTextArea();
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(logArea), BorderLayout.CENTER);

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

	public static JFrame createFrame() {
		JFrame frame = new JFrame(Log.getLogLocation().toString());
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1000, 400));
		frame.add(new LogPanel());
		frame.pack();
		return frame;
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		JFrame frame = createFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
