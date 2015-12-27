package com.marginallyclever.makelangelo;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

// manages the status bar at the bottom of the application window
public class StatusBar extends JPanel {
  static final long serialVersionUID = 1;

  long t_start;
  protected DecimalFormat fmt = new DecimalFormat("#0.00");
  protected String sSoFar = "so far: ";
  protected String sRemaining = " remaining: ";
  protected String sElapsed = "";
  protected Translator translator;
  protected JLabel message;
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

    GridBagConstraints c = new GridBagConstraints();
    message = new JLabel();
    bar = new JProgressBar();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    //c.anchor=GridBagConstraints.WEST;
    gridbag.setConstraints(bar, c);
    this.add(bar);
    //c.anchor=GridBagConstraints.EAST;
    c.weightx = 10;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridbag.setConstraints(message, c);
    this.add(message);

    Font f = getFont();
    setFont(f.deriveFont(Font.BOLD, 15));

    clear();
  }

  public void setMessage(String text) {
    message.setText(" " + text);
  }

  public String getElapsed() {
    return sElapsed;
  }

  public void clear() {
    setMessage("Ready");
  }

  public void start() {
    t_start = System.currentTimeMillis();
  }

  public void setProgress(long sofar, long total) {
    sElapsed = "";
    if (total > 0) {
      bar.setMaximum((int) total);
      bar.setValue((int) sofar);

      long t_draw_now = (sofar > 0) ? System.currentTimeMillis() - t_start : 0;
      long total_time = (long) ((float) t_draw_now * (float) total / (float) sofar);
      long remaining = total_time - t_draw_now;
      sElapsed = translator.get("StatusSoFar") + formatTime(t_draw_now) +
          translator.get("StatusRemaining") + formatTime(remaining);
    }

    setMessage("% (" + sofar + "/" + total + ") " + sElapsed);
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
