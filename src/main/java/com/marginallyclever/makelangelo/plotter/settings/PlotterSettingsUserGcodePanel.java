package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.select.SelectTextArea;

import javax.swing.*;
import java.awt.*;

public class PlotterSettingsUserGcodePanel extends JPanel {

    private final Plotter myPlotter;

    private SelectTextArea userGeneralStartGcode;
    private SelectTextArea userGeneralEndGcode;

    public PlotterSettingsUserGcodePanel(Plotter robot) {
        super();
        this.myPlotter = robot;
        initComponents();
    }

    private void initComponents() {
        PlotterSettings settings = myPlotter.getSettings();
        setLayout(new BorderLayout());

        JTabbedPane jTabbedPane = new JTabbedPane();
        userGeneralStartGcode = new SelectTextArea("userGeneralStartGcode", null, settings.getUserGeneralStartGcode());
        userGeneralStartGcode.setLineWrap(false);
        jTabbedPane.addTab(Translator.get("PlotterSettings.userGeneralStartGcode"), userGeneralStartGcode);
        //
        userGeneralEndGcode = new SelectTextArea("userGeneralEndGcode", null, settings.getUserGeneralEndGcode());
        userGeneralEndGcode.setLineWrap(false);
        jTabbedPane.addTab(Translator.get("PlotterSettings.userGeneralEndGcode"), userGeneralEndGcode);
        //
        add(jTabbedPane, BorderLayout.CENTER);
        //
        JPanel bottom = new JPanel(new FlowLayout());
        JButton buttonSave = new JButton(Translator.get("Save"));
        bottom.add(buttonSave);
        buttonSave.addActionListener((e) -> {
            save();
        });
        add(bottom, BorderLayout.SOUTH);
        //
        setMinimumSize(new Dimension(640, 400));
        setPreferredSize(new Dimension(640, 400));
    }

    private void save() {
        PlotterSettings settings = myPlotter.getSettings();
        settings.setUserGeneralStartGcode(userGeneralStartGcode.getText());
        settings.setUserGeneralEndGcode(userGeneralEndGcode.getText());
    }
}
