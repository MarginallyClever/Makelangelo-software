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

    public boolean updateSelected() {
        double w = paperSettings.getCurrentPaper().getPaperWidth();
        double h = paperSettings.getCurrentPaper().getPaperHeight();
        boolean match = paperSize.width == w && paperSize.height == h;
        setSelected(match);
        return match;
    }

}
