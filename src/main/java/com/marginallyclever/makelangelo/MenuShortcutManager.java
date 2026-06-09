package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.actions.NamedAbstractAction;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages menu shortcuts by loading from and saving to a JSON file.
 */
public class MenuShortcutManager {
    private static final Logger logger = LoggerFactory.getLogger(MenuShortcutManager.class);
    private static final String SHORTCUT_FILE = "menuShortcuts.json";
    private final Map<String, String> shortcutMap = new HashMap<>();

    public void manage(JMenuBar menuBar) {
        Path path = getShortcutFilePath();
        if (!Files.exists(path)) {
            collectShortcuts(menuBar);
            saveShortcuts(path);
        }
        if(Files.exists(path)) {
            loadShortcuts(path);
            applyShortcuts(menuBar);
        }
    }

    private Path getShortcutFilePath() {
        return Paths.get(FileAccess.getHomeDirectory(), ".makelangelo", SHORTCUT_FILE);
    }

    private void loadShortcuts(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            JSONTokener tokener = new JSONTokener(is);
            JSONObject root = new JSONObject(tokener);
            shortcutMap.clear();
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                shortcutMap.put(key, root.getString(key));
            }
        } catch (Exception e) {
            logger.error("Failed to load menu shortcuts from {}", path, e);
        }
    }

    private void saveShortcuts(Path path) {
        try {
            Files.createDirectories(path.getParent());
            JSONObject root = new JSONObject();
            for (Map.Entry<String, String> entry : shortcutMap.entrySet()) {
                root.put(entry.getKey(), entry.getValue());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(root.toString(4));
            }
        } catch (IOException e) {
            logger.error("Failed to save menu shortcuts to {}", path, e);
        }
    }

    private void collectShortcuts(JMenuBar menuBar) {
        shortcutMap.clear();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            collectFromMenu(menuBar.getMenu(i));
        }
    }

    private void collectFromMenu(JMenu menu) {
        if (menu == null || menu instanceof RecentFiles) return;

        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) continue;
            if (item instanceof JMenu) {
                collectFromMenu((JMenu) item);
            } else if(item.getAction() instanceof NamedAbstractAction action) {
                String name = action.getName();
                String shortcut = item.getAccelerator() != null ? item.getAccelerator().toString() : "";
                shortcutMap.put(name, shortcut);
            }
        }
    }

    private void applyShortcuts(JMenuBar menuBar) {
        Map<String, String> usedShortcuts = new HashMap<>();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            applyToMenu(menuBar.getMenu(i), usedShortcuts);
        }
    }

    private void applyToMenu(JMenu menu, Map<String, String> usedShortcuts) {
        if (menu == null || menu instanceof RecentFiles) return;

        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) continue;
            if (item instanceof JMenu) {
                if(item instanceof RecentFiles) continue;
                applyToMenu((JMenu) item, usedShortcuts);
            } else if(item.getAction() instanceof NamedAbstractAction action) {
                String shortcut = shortcutMap.getOrDefault(action.getName(), "");
                if (!shortcut.isEmpty()) {
                    applyShortcutToItem(item, shortcut, usedShortcuts);
                } else {
                    item.setAccelerator(null);
                }
            }
        }
    }

    private String getItemId(JMenuItem item) {
        String text = item.getText();
        if (text == null) {
            Action action = item.getAction();
            if (action != null) {
                text = (String) action.getValue(Action.NAME);
            }
        }
        return text != null ? text : "unknown";
    }

    private void applyShortcutToItem(JMenuItem item, String shortcut, Map<String, String> usedShortcuts) {
        if (shortcut != null && !shortcut.isEmpty()) {
            KeyStroke ks = KeyStroke.getKeyStroke(shortcut);
            if (ks == null) {
                logger.error("Invalid shortcut '{}' for menu item '{}'", shortcut, item.getText());
            } else {
                if (usedShortcuts.containsKey(ks.toString())) {
                    logger.error("Duplicate shortcut '{}' for menu item '{}' (already used by '{}')",
                            shortcut, item.getText(), usedShortcuts.get(ks.toString()));
                } else {
                    item.setAccelerator(ks);
                    usedShortcuts.put(ks.toString(), item.getText());
                }
            }
        } else {
            item.setAccelerator(null);
        }
    }
}
