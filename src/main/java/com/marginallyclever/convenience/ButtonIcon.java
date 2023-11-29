package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.Serial;

/**
 * A button with an icon and a translation key.
 * @author Dan Royer
 */
public class ButtonIcon extends JButton {
	@Serial
    private static final long serialVersionUID = 6329805223648415348L;
	private static final Logger logger = LoggerFactory.getLogger(ButtonIcon.class);

    public ButtonIcon(String translation, String iconPath) {
        super();
        setText(translation);
        setIcon(createImageIcon(iconPath));
    }

    public void replaceIcon(String iconPath) {
        setIcon(createImageIcon(iconPath));
    }

    /**
     * @return an ImageIcon, or null if the path was invalid.
     */ 
    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.warn("Failed to load icon {}", path);
            return null;
        }
    }
}
