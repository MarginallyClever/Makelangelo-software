package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class QuitAction extends AbstractAction {
    private final JFrame frame;

    public QuitAction(String label, MainFrame frame) {
        super(label);
        this.frame = frame;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Q);
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-stop-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WindowEvent windowClosing = new WindowEvent(SwingUtilities.getWindowAncestor(frame), WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(windowClosing);
    }
}
