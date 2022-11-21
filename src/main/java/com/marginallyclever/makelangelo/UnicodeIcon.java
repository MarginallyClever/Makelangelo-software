package com.marginallyclever.makelangelo;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a single color {@link Icon} based on a Unicode symbol.
 * @author Dan Royer
 * @since 2022-03-15
 */
public class UnicodeIcon implements Icon {
    private final static int HEIGHT = 24;
    private final static int WIDTH = 24;
    private final String unicode;

    public UnicodeIcon(String unicode) {
        super();
        this.unicode = unicode;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setFont(new Font("SansSerif",Font.PLAIN,(int)((double)HEIGHT/1.25)));
        FontMetrics fm = g.getFontMetrics();
        int x2 = (WIDTH - fm.stringWidth(unicode)) / 2;
        int y2 = y+(fm.getAscent() + (HEIGHT - (fm.getAscent() + fm.getDescent())) / 2);
        g2.drawString(unicode,x2, y2);
    }

    @Override
    public int getIconWidth() {
        return WIDTH;
    }

    @Override
    public int getIconHeight() {
        return HEIGHT;
    }
}
