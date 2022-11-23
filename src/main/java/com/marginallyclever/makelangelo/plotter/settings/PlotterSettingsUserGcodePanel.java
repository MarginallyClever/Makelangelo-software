package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.select.SelectTextArea;

import javax.swing.*;
import java.awt.*;

public class PlotterSettingsUserGcodePanel extends JPanel {
    private final PlotterSettings settings;

    private final SelectTextArea userGeneralStartGcode;
    private final SelectTextArea userGeneralEndGcode;

    public PlotterSettingsUserGcodePanel(PlotterSettings settings) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.settings = settings;

        userGeneralStartGcode = new SelectTextArea("userGeneralStartGcode", null, settings.getUserGeneralStartGcode());
        userGeneralStartGcode.setLineWrap(false);
        userGeneralStartGcode.setAlignmentX(0);
        JLabel label0 = new JLabel(Translator.get("PlotterSettings.userGeneralStartGcode"));
        label0.setAlignmentX(0);
        label0.setHorizontalAlignment(SwingConstants.LEADING);
        this.add(label0);
        this.add(userGeneralStartGcode);

        userGeneralEndGcode = new SelectTextArea("userGeneralEndGcode", null, settings.getUserGeneralEndGcode());
        userGeneralEndGcode.setLineWrap(false);
        userGeneralEndGcode.setAlignmentX(0);
        label0 = new JLabel(Translator.get("PlotterSettings.userGeneralEndGcode"));
        label0.setAlignmentX(0);
        label0.setHorizontalAlignment(SwingConstants.LEADING);
        this.add(label0);
        this.add(userGeneralEndGcode);

        //userGeneralStartGcode.setMinimumSize(new Dimension(300, 200));
        //userGeneralEndGcode.setMinimumSize(new Dimension(300, 200));
    }

    public void save() {
        settings.setUserGeneralStartGcode(userGeneralStartGcode.getText());
        settings.setUserGeneralEndGcode(userGeneralEndGcode.getText());
    }

    public void reset() {
        userGeneralStartGcode.setText(settings.getUserGeneralStartGcode());
        userGeneralEndGcode.setText(settings.getUserGeneralEndGcode());
    }
}
