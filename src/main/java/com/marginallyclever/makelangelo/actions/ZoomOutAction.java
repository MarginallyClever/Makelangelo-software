package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.preview.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class ZoomOutAction extends AbstractAction {
    private final Camera camera;

    public ZoomOutAction(String label, Camera camera) {
        super(label);
        this.camera = camera;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control -"));
        putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-zoom-out-16.png"))));
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_MINUS);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.zoom(1);
    }
}
