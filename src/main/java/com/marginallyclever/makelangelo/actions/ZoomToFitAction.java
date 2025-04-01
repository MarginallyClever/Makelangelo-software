package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ZoomToFitAction extends AbstractAction {
    private final Camera camera;
    private final Paper paper;

    public ZoomToFitAction(String label, Camera camera, Paper paper) {
        super(label);
        this.camera = camera;
        this.paper = paper;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control backspace"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        camera.zoomToFit(paper.getPaperWidth(),paper.getPaperHeight());
    }
}
