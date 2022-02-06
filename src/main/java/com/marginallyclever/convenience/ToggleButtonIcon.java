package com.marginallyclever.convenience;

import com.marginallyclever.makelangelo.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A button with multiple sets of icons, texts and optionally foreground colors
 */
public class ToggleButtonIcon extends JButton {
    private static final Logger logger = LoggerFactory.getLogger(ToggleButtonIcon.class);

    private final List<String> translations;
    private final List<ImageIcon> icons;
    private final List<Color> foregroundColors;

    public ToggleButtonIcon(List<String> translationKeys, List<String> iconPaths) {
        this(translationKeys, iconPaths, null);
    }

    public ToggleButtonIcon(List<String> translationKeys, List<String> iconPaths, List<Color> foregroundColors) {
        translations = translationKeys.stream().map(Translator::get).collect(Collectors.toList());
        icons = iconPaths.stream().map(ToggleButtonIcon::createImageIcon).collect(Collectors.toList());
        this.foregroundColors = foregroundColors;
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
}
