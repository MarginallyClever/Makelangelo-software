package com.marginallyclever.convenience;

import com.marginallyclever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ButtonIcon extends JButton {
	private static final long serialVersionUID = 6329805223648415348L;
	private static final Logger logger = LoggerFactory.getLogger(ButtonIcon.class);

    public ButtonIcon(String translationKey, String iconPath) {
        super();
        if (translationKey != null && !translationKey.isEmpty() && !translationKey.isBlank()) {
            setText(Translator.get(translationKey));
        }
        setIcon(createImageIcon(iconPath));
        setFont(new Font("Arial", Font.PLAIN, 15));
        setMargin(new Insets(1, 1, 1, 3));
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
