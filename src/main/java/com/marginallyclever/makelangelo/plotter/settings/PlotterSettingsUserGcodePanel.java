/*
 */
package com.marginallyclever.makelangelo.plotter.settings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 */
public class PlotterSettingsUserGcodePanel extends JPanel {

	private final Plotter myPlotter;

	private JButton buttonSave;

	private SelectTextArea userGeneralStartGcode;
	private SelectTextArea userGeneralEndGcode;
	private SelectTextArea userToolChangeStartGcode;
	private SelectTextArea userToolChangeEndGcode;

	private SelectTextArea replacementPatternList;

	private javax.swing.JTabbedPane jTabbedPane1;

	public PlotterSettingsUserGcodePanel(Plotter robot) {
		super();
		
		
		this.myPlotter = robot;
		initComponents();

	}

	private void initComponents() {

		PlotterSettings settings = myPlotter.getSettings();
		setLayout(new java.awt.BorderLayout());
		jTabbedPane1 = new javax.swing.JTabbedPane();

		userGeneralStartGcode = new SelectTextArea(
				"userGeneralStartGcode",
				Translator.get("PlotterSettings.userGeneralStartGcode"),
				settings.getUserGcode().getUserGeneralStartGcode()
		);
		userGeneralStartGcode.setLineWrap(false);
		//add(userGeneralStartGcode);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userGeneralStartGcode"), userGeneralStartGcode);
		
		userGeneralEndGcode
				= new SelectTextArea(
						"userGeneralEndGcode",
						Translator.get("PlotterSettings.userGeneralEndGcode"),
						settings.getUserGcode().getUserGeneralEndGcode()
				);
		userGeneralEndGcode.setLineWrap(false);
		//add(userGeneralEndGcode);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userGeneralEndGcode"), userGeneralEndGcode);

		userToolChangeStartGcode
				= new SelectTextArea(
						"userToolChangeStartGcode",
						Translator.get("PlotterSettings.userToolChangeStartGcode"),
						settings.getUserGcode().getUserToolChangeStartGcode()
				);
		userToolChangeStartGcode.setLineWrap(false);
		//add(userToolChangeStartGcode);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userToolChangeStartGcode"), userToolChangeStartGcode);
		
		userToolChangeEndGcode
				= new SelectTextArea(
						"userToolChangeEndGcode",
						Translator.get("PlotterSettings.userToolChangeEndGcode"),
						settings.getUserGcode().getUserToolChangeEndGcode()
				);
		userToolChangeEndGcode.setLineWrap(false);
		//add(userToolChangeEndGcode);
		jTabbedPane1.addTab(Translator.get("PlotterSettings.userToolChangeEndGcode"), userToolChangeEndGcode);
		//
		add(jTabbedPane1, java.awt.BorderLayout.CENTER);
		//

		replacementPatternList
				= new SelectTextArea(
						"replacementPattern",
						Translator.get("PlotterSettings.replacementPattern"),
						settings.getUserGcode().getAllPattern(settings)
				);
		replacementPatternList.setLineWrap(false);
		replacementPatternList.setEditable(false);
		replacementPatternList.setDragEnabled(true);
		add(replacementPatternList, java.awt.BorderLayout.WEST);

		JPanel bottom = new JPanel(new FlowLayout());
		buttonSave = new JButton(Translator.get("Save"));
		bottom.add(buttonSave);
		buttonSave.addActionListener((e) -> {
			save();
		});

		
		
		add(bottom, java.awt.BorderLayout.SOUTH);
	}

	private void save() {

		PlotterSettings settings = myPlotter.getSettings();
		settings.getUserGcode().setUserGeneralStartGcode(userGeneralStartGcode.getText());
		settings.getUserGcode().setUserGeneralEndGcode(userGeneralEndGcode.getText());
		settings.getUserGcode().setUserToolChangeStartGcode(userToolChangeStartGcode.getText());
		settings.getUserGcode().setUserToolChangeEndGcode(userToolChangeEndGcode.getText());

	}
}
