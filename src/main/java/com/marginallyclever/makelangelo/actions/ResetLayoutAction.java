package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;

import javax.swing.*;
import java.util.Objects;

public class ResetLayoutAction extends AbstractAction {
    private final MainFrame frame;

    public ResetLayoutAction(String label, MainFrame frame) {
        super(label);
        this.frame = frame;
        // no accelerator key.
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-reset-16.png"))));
        putValue(Action.SHORT_DESCRIPTION, "Reset the layout to the default.");
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        frame.resetDefaultLayout();
    }
}
