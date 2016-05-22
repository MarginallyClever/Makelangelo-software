package com.marginallyclever.makelangeloRobot;

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

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;

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


	public StatusBar() {
		super();
		setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);

		mFinished = new JLabel("", SwingConstants.LEFT);
		mExactly = new JLabel("", SwingConstants.CENTER);
		mRemaining = new JLabel("", SwingConstants.RIGHT);

		bar = new JProgressBar();

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridwidth=3;
		add(bar,c);
		c.gridy++;
		
		c.gridwidth=1;
		add(mFinished,c);
		c.gridx++;
		add(mExactly,c);
		c.gridx++;
		add(mRemaining,c);
		c.gridy++;
		
		c.gridx=0;
		c.ipady=20;
		c.gridwidth=3;
		add(new JLabel("\n"+Translator.get("SharePromo")), c);

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

		mFinished.setText(Log.millisecondsToHumanReadable(t_draw_now));
		DecimalFormat df = new DecimalFormat("#.##");
		mExactly.setText(sofar + "/" + total + " "+df.format(100*(double)sofar/(double)total)+"%");
		mRemaining.setText(Log.millisecondsToHumanReadable(remaining));
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
