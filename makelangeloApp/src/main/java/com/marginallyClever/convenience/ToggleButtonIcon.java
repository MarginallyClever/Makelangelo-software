package com.marginallyClever.convenience;

import com.marginallyClever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A button with multiple sets of icons, texts and optionally foreground colors
 */
public class ToggleButtonIcon extends JButton {
    private static final Logger logger = LoggerFactory.getLogger(ToggleButtonIcon.class);

    private final List<String> translations = new ArrayList<>();
    private final List<ImageIcon> icons = new ArrayList<>();
    private final List<Color> foregroundColors = new ArrayList<>();

    public ToggleButtonIcon(Item... items) {
        for (Item item : items) {
            translations.add(Translator.get(item.translation));
            icons.add(createImageIcon(item.icon));
            foregroundColors.add(item.foregroundColor);
        }
        setFont(new Font("Arial", Font.PLAIN, 15));
        setMargin(new Insets(1, 1, 1, 3));
        updateButton(0);
    }

    public void updateButton(int position) {
        setIcon(icons.get(position));
        if (foregroundColors != null) {
            setForeground(foregroundColors.get(position));
        }
        setText(translations.get(position));
    }

    /**
     * @return an ImageIcon, or null if the path was invalid.
     */
    private static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ToggleButtonIcon.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.warn("Failed to load icon {}", path);
            return null;
        }
    }

    public static class Item {
        private final String translation;
        private final String icon;
        private final Color foregroundColor;

        public Item(String translation, String icon, Color foregroundColor) {
            this.translation = translation;
            this.icon = icon;
            this.foregroundColor = foregroundColor;
        }
    }
}
