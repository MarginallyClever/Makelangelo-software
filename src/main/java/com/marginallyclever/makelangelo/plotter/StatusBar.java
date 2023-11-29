package com.marginallyclever.makelangelo.plotter;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.Translator;

import javax.swing.*;
import java.awt.*;

/**
 *  manages the status bar at the bottom of the application window
 */
@Deprecated
public class StatusBar extends JPanel {
	protected long t_start;
	protected final String sSoFar = "so far: ";
	protected final String sRemaining = " remaining: ";
	protected String sElapsed = "";
	protected Translator translator;
	protected JLabel mLines;
	protected JLabel mTime;
	protected JProgressBar bar;


	public StatusBar() {
		super();
		setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
		setLayout(new GridBagLayout());

		mLines = new JLabel("", SwingConstants.LEFT);
		mTime = new JLabel("", SwingConstants.RIGHT);

		bar = new JProgressBar();

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTH;
		c.gridwidth=3;
		add(bar,c);
		c.gridy++;

		c.weightx = 1;
		c.gridwidth=1;
		add(mLines,c);
		c.gridx++;
		c.weightx = 0;
		add(mTime,c);
		c.gridy++;
		
		c.gridx=0;
		c.gridwidth=3;
		c.weightx=1;
		c.weighty=1;
		JLabel area = new JLabel();
		// TODO make link to https://twitter.com/search?q=%23makelangelo&lang=en ?
		area.setText(Translator.get("SharePromo"));
		area.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		add(area, c);

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
		mLines.setText("");
		mTime.setText("");
	}

	public void start() {
		t_start = System.currentTimeMillis();
	}

	/**
	 * Set progress bar
	 * @param sofar number of gcode lines processed.
	 * @param total number of gcode lines total.
	 */
	public void setProgress(long sofar, long total) {
		if (total <= 0) return;
		
		bar.setMaximum((int) total);
		bar.setValue((int) sofar);

		long t_draw_now = (sofar > 0) ? System.currentTimeMillis() - t_start : 0;
		long total_time = (long) ((float) t_draw_now * (float) total / (float) sofar);
		long remaining = total_time - t_draw_now;

		mLines.setText(sofar + " / " + total + " "+StringHelper.formatDouble(100*(double)sofar/(double)total)+"%");
		mTime.setText(millisecondsToHumanReadable(t_draw_now) + " / " + millisecondsToHumanReadable(remaining));
	}

	/**
	 * Set progress bar
	 * @param seconds total estimated drawing time
	 */
	public void setProgressEstimate(double seconds, long totalLines) {
		if(seconds <= 0) return;
		
		bar.setMaximum(100);
		bar.setValue(0);

		mLines.setText(0 + " / " + totalLines + " "+StringHelper.formatDouble(0)+"%");
		mTime.setText("0s / "+secondsToHumanReadable(seconds));
	}

	public static String secondsToHumanReadable(double totalTime) {
		return millisecondsToHumanReadable((long)(totalTime*1000));
	}

	/**
	 * Turns milliseconds into h:m:s
	 * @param millis milliseconds
	 * @return human-readable string
	 */
	public static String millisecondsToHumanReadable(long millis) {
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
}
