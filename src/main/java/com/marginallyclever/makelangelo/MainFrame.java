package com.marginallyclever.makelangelo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

/**
 * A JFrame that remembers its size and position.
 */
public class MainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private final Preferences prefs;
    private static final String KEY_IS_FULLSCREEN = "isFullscreen";
    private static final String KEY_WINDOW_WIDTH = "windowWidth";
    private static final String KEY_WINDOW_HEIGHT = "windowHeight";
    private static final String KEY_WINDOW_X = "windowX";
    private static final String KEY_WINDOW_Y = "windowY";

    public MainFrame(String title, Preferences prefs) {
        super(title);
        this.prefs = prefs;
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                saveWindowSizeAndPosition();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                saveWindowSizeAndPosition();
            }
        });
    }

    public void setWindowSizeAndPosition() {
        // set the normal window size and position
        Dimension frameSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowW = prefs.getInt(KEY_WINDOW_WIDTH, frameSize.width);
        int windowH = prefs.getInt(KEY_WINDOW_HEIGHT, frameSize.height);
        int windowX = prefs.getInt(KEY_WINDOW_X, (frameSize.width - windowW)/2);
        int windowY = prefs.getInt(KEY_WINDOW_Y, (frameSize.height - windowH)/2);
        logger.info("Set window size and position "+windowW+"x"+windowH+" pos="+windowX+","+windowY);
        this.setBounds(windowX, windowY,windowW, windowH);

        if(prefs.getBoolean(KEY_IS_FULLSCREEN,false)) {
            // if we were in fullscreen mode, maximize the window.
            // this way the "go fullscreen" button will return the window to "normal" size.
            this.setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    // remember window location for next time.
    private void saveWindowSizeAndPosition() {
        int state = getExtendedState();
        boolean isFullscreen = ((state & JFrame.MAXIMIZED_BOTH)!=0);

        prefs.putBoolean(KEY_IS_FULLSCREEN, isFullscreen);
        if(!isFullscreen) {
            Dimension frameSize = this.getSize();
            Point p = this.getLocation();
            prefs.putInt(KEY_WINDOW_WIDTH, frameSize.width);
            prefs.putInt(KEY_WINDOW_HEIGHT, frameSize.height);
            prefs.putInt(KEY_WINDOW_X, p.x);
            prefs.putInt(KEY_WINDOW_Y, p.y);
        }
    }
}
