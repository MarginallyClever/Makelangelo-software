package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

// manages the status bar at the bottom of the application window
public class StatusBar extends JPanel {
	static final long serialVersionUID = 1;

	long t_start;
	protected DecimalFormat fmt = new DecimalFormat("#0.00");
	protected String sSoFar = "so far: ";
	protected String sRemaining = " remaining: ";
	protected String sElapsed = "";
	protected Translator translator;
	protected JLabel mFinished;
	protected JLabel mExactly;
	protected JLabel mRemaining;
	protected JProgressBar bar;

	public String formatTime(long millis) {
		long s = millis / 1000;
		long m = s / 60;
		long h = m / 60;
		m %= 60;
		s %= 60;

		String elapsed = "";
		if (h > 0) elapsed += h + "h";
		if (h > 0 || m > 0) elapsed += m + "m";
		elapsed += s + "s ";

		return elapsed;
	}


	public StatusBar(Translator ms) {
		super();
		this.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);

		translator = ms;

		mFinished = new JLabel("", SwingConstants.LEFT);
		mExactly = new JLabel("", SwingConstants.CENTER);
		mRemaining = new JLabel("", SwingConstants.RIGHT);

		bar = new JProgressBar();

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridwidth=3;
		this.add(bar,c);
		c.gridwidth=1;
		c.gridy++;
		this.add(mFinished,c);
		c.gridx++;
		this.add(mExactly,c);
		c.gridx++;
		this.add(mRemaining,c);
		c.gridx=0;
		c.gridy++;
		c.ipady=20;
		c.gridwidth=3;
		this.add(new JLabel("\n"+Translator.get("SharePromo")), c);

		Dimension preferredSize = bar.getPreferredSize();
		preferredSize.setSize(preferredSize.getWidth(), preferredSize.getHeight()*2);
		bar.setPreferredSize(preferredSize);
		Font f = getFont();
		setFont(f.deriveFont(Font.BOLD, 15));

		clear();
	}

	public String getElapsed() {
		return sElapsed;
	}

	public void clear() {
		mFinished.setText("");
		mExactly.setText("");
		mRemaining.setText("");
	}

	public void start() {
		t_start = System.currentTimeMillis();
	}

	public void setProgress(long sofar, long total) {
		if (total <= 0) return;
		
		bar.setMaximum((int) total);
		bar.setValue((int) sofar);

		long t_draw_now = (sofar > 0) ? System.currentTimeMillis() - t_start : 0;
		long total_time = (long) ((float) t_draw_now * (float) total / (float) sofar);
		long remaining = total_time - t_draw_now;

		mFinished.setText(formatTime(t_draw_now));
		mExactly.setText(sofar + "/" + total);
		mRemaining.setText(formatTime(remaining));
	}
}

/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
