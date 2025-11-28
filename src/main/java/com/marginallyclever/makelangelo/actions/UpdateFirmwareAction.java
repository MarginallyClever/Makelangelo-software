package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.firmwareuploader.FirmwareUploaderPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class UpdateFirmwareAction extends AbstractAction {
    private final MainFrame frame;

    public UpdateFirmwareAction(String label, MainFrame frame) {
        super(label);
        this.frame = frame;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-install-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(frame),"Firmware Update");
        dialog.add(new FirmwareUploaderPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(frame));
        dialog.setSize(new Dimension(600, 400));

        frame.enableMenuBar(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.enableMenuBar(true);
            }
        });

        dialog.setVisible(true);
    }
}
