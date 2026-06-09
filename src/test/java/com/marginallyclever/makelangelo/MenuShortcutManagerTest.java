package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.FileAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MenuShortcutManagerTest {
    private Path shortcutFilePath;

    @BeforeEach
    public void setUp() throws IOException {
        shortcutFilePath = Paths.get(FileAccess.getHomeDirectory(), ".makelangelo", "menuShortcuts.json");
        if (Files.exists(shortcutFilePath)) {
            Files.move(shortcutFilePath, shortcutFilePath.resolveSibling("menuShortcuts.json.bak"));
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(shortcutFilePath);
        Path bak = shortcutFilePath.resolveSibling("menuShortcuts.json.bak");
        if (Files.exists(bak)) {
            Files.move(bak, shortcutFilePath);
        }
    }

    @Test
    public void testCollectAndSave() throws IOException {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem item = new JMenuItem("New");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        menu.add(item);
        menuBar.add(menu);

        MenuShortcutManager manager = new MenuShortcutManager();
        manager.manage(menuBar);

        assertTrue(Files.exists(shortcutFilePath), "Shortcut file should be created");
        String content = new String(Files.readAllBytes(shortcutFilePath));
        assertTrue(content.contains("New"), "Content should contain menu item text");
        assertTrue(content.contains("ctrl pressed N"), "Content should contain accelerator");
    }

    @Test
    public void testLoadAndApply() throws IOException {
        // Create a shortcut file manually
        Files.createDirectories(shortcutFilePath.getParent());
        String json = "{\"New\": \"ctrl pressed M\"}";
        Files.write(shortcutFilePath, json.getBytes());

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem item = new JMenuItem("New");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        menu.add(item);
        menuBar.add(menu);

        MenuShortcutManager manager = new MenuShortcutManager();
        manager.manage(menuBar);

        KeyStroke ks = item.getAccelerator();
        assertNotNull(ks);
        assertEquals(KeyEvent.VK_M, ks.getKeyCode());
        assertTrue((ks.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0);
    }
}
