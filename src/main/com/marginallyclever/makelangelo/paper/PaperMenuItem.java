package com.marginallyclever.makelangelo.paper;

import javax.swing.*;

public class PaperMenuItem extends JRadioButtonMenuItem {

    private final PaperSize paperSize;
    private final PaperSettings paperSettings;

    public PaperMenuItem(PaperSettings paperSettings, PaperSize paperSize) {
        super(paperSize.toString());
        this.paperSettings = paperSettings;
        this.paperSize = paperSize;
        addActionListener((e)-> {
            paperSettings.changePaperSize(paperSize);
        });
    }

    public void updateSelected() {
        double w = paperSettings.getCurrentPaper().getPaperWidth();
        double h = paperSettings.getCurrentPaper().getPaperHeight();
        setSelected(paperSize.width == w && paperSize.height == h);
    }

}
