package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class NewFileAction extends AbstractAction {
    private final MainFrame frame;

    public NewFileAction(String displayName,MainFrame frame) {
        super(displayName);
        this.frame = frame;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-new-16.png"))));
        putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke("control 0"));
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setTurtle(new Turtle());
        frame.setMainTitle("");
    }
}
