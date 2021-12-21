package com.marginallyclever.makelangelo.paper;

import javax.swing.*;

public class PaperMenuItem extends JRadioButtonMenuItem {

    private final PaperSize paperSize;
    private final PaperSettings paperSettings;

    public PaperMenuItem(PaperSettings paperSettings, PaperSize paperSize) {
        this.paperSettings = paperSettings;
        this.paperSize = paperSize;
        addActionListener((e)-> {
            paperSettings.changePaperSize(paperSize);
        });
    }

    public boolean updateSelected() {
        Paper currentPaper = paperSettings.getCurrentPaper();
        double w = currentPaper.getPaperWidth();
        double h = currentPaper.getPaperHeight();
        boolean match = (!currentPaper.isLandscape() && paperSize.width == w && paperSize.height == h)
                || (currentPaper.isLandscape() && paperSize.width == h && paperSize.height == w);
        setSelected(match);
        setText(paperSize.toString() + (match && currentPaper.isLandscape()?" \u21cb":""));
        return match;
    }

}
