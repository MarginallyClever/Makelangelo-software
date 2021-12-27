package com.marginallyclever.convenience;

import com.marginallyclever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ButtonIcon extends JButton {
    private static final Logger logger = LoggerFactory.getLogger(ButtonIcon.class);

    public ButtonIcon(String translationKey, String iconPath) {
        super(Translator.get(translationKey));
        setIcon(createImageIcon(iconPath));
        setFont(new Font("Arial", Font.PLAIN, 15));
        setMargin(new Insets(1, 1, 1, 3));
    }

    public void replaceIcon(String iconPath) {
        setIcon(createImageIcon(iconPath));
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.warn("Failed to load icon {}", path);
            return null;
        }
    }
}
