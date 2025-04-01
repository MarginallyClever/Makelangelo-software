package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.applicationsettings.ApplicationSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class AdjustPreferencesAction extends AbstractAction {
    private final MainFrame frame;
    private final ApplicationSettings myPreferencesPanel = new ApplicationSettings();

    public AdjustPreferencesAction(String label, MainFrame frame) {
        super(label);
        this.frame = frame;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-settings-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        myPreferencesPanel.run(SwingUtilities.getWindowAncestor(frame));
    }
}
