package com.marginallyclever.nodeBasedEditor.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ActionPrint extends AbstractAction {
    NodeGraphEditorPanel editor;

    public ActionPrint(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BufferedImage awtImage = new BufferedImage(editor.getWidth(), editor.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = awtImage.getGraphics();
        editor.printAll(g);
/*
        if(popupBar.isVisible()) {
            g.translate(popupPoint.x, popupPoint.y);
            popupBar.printAll(g);
            g.translate(-popupPoint.x, -popupPoint.y);
        }
 */
        // TODO file selection dialog here
        File outputfile = new File("saved.png");

        try {
            ImageIO.write(awtImage, "png", outputfile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
