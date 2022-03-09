/*
 */
package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 */
public class PlotterSettingsUserGcodePanel extends JPanel {

	private final Plotter myPlotter;

	private javax.swing.JSplitPane jSplitPane1;
	
	private SelectTextArea replacementPatternList;
	
	private javax.swing.JTabbedPane jTabbedPane1;
	
	private SelectTextArea userGeneralStartGcode;
	private SelectTextArea userGeneralEndGcode;
	private SelectTextArea userToolChangeStartGcode;
	private SelectTextArea userToolChangeEndGcode;

	private JButton buttonSave;

	public PlotterSettingsUserGcodePanel(Plotter robot) {
		super();
		this.myPlotter = robot;
		initComponents();
	}

	private void initComponents() {
		PlotterSettings settings = myPlotter.getSettings();
		setLayout(new java.awt.BorderLayout());
		//
		jTabbedPane1 = new javax.swing.JTabbedPane();		
        jSplitPane1 = new javax.swing.JSplitPane();
		//
		userGeneralStartGcode = new SelectTextArea(
				"userGeneralStartGcode",
				null,
				settings.getUserGcode().getUserGeneralStartGcode()
		);
		userGeneralStartGcode.setLineWrap(false);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userGeneralStartGcode"), userGeneralStartGcode);
		//
		userGeneralEndGcode = new SelectTextArea(
				"userGeneralEndGcode",
				null,
				settings.getUserGcode().getUserGeneralEndGcode()
		);
		userGeneralEndGcode.setLineWrap(false);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userGeneralEndGcode"), userGeneralEndGcode);
		//
		userToolChangeStartGcode = new SelectTextArea(
				"userToolChangeStartGcode",
				null,
				settings.getUserGcode().getUserToolChangeStartGcode()
		);
		userToolChangeStartGcode.setLineWrap(false);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userToolChangeStartGcode"), userToolChangeStartGcode);
		//
		userToolChangeEndGcode = new SelectTextArea(
				"userToolChangeEndGcode",
				null,
				settings.getUserGcode().getUserToolChangeEndGcode()
		);
		userToolChangeEndGcode.setLineWrap(false);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userToolChangeEndGcode"), userToolChangeEndGcode);
		//		
		replacementPatternList = new SelectTextArea(
				"replacementPattern",
				Translator.get("PlotterSettings.replacementPattern"),
				settings.getUserGcode().getAllPattern(settings)
		);
		replacementPatternList.setLineWrap(false);
		replacementPatternList.setEditable(false);
		replacementPatternList.setDragEnabled(true);
		//
		jSplitPane1.setLeftComponent(replacementPatternList);
		jSplitPane1.setRightComponent(jTabbedPane1);
		jSplitPane1.setDividerLocation(250);
		//
		add(jSplitPane1, java.awt.BorderLayout.CENTER);
		//
		JPanel bottom = new JPanel(new FlowLayout());
		buttonSave = new JButton(Translator.get("Save"));
		bottom.add(buttonSave);
		buttonSave.addActionListener((e) -> {
			save();
		});
		add(bottom, java.awt.BorderLayout.SOUTH);
		//
		setMinimumSize(new Dimension(640, 400));
		setPreferredSize(new Dimension(640, 400));
	}

	private void save() {
		PlotterSettings settings = myPlotter.getSettings();
		settings.getUserGcode().setUserGeneralStartGcode(userGeneralStartGcode.getText());
		settings.getUserGcode().setUserGeneralEndGcode(userGeneralEndGcode.getText());
		settings.getUserGcode().setUserToolChangeStartGcode(userToolChangeStartGcode.getText());
		settings.getUserGcode().setUserToolChangeEndGcode(userToolChangeEndGcode.getText());
	}
}
