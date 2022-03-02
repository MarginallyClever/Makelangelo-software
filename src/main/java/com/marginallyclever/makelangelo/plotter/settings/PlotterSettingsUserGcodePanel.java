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

	private final JButton buttonSave;

	private final SelectTextArea userGeneralStartGcode;
	private final SelectTextArea userGeneralEndGcode;
	private final SelectTextArea userToolChangeStartGcode;
	private final SelectTextArea userToolChangeEndGcode;
	
	private final SelectTextArea replacementPatternList;
	

	public PlotterSettingsUserGcodePanel(Plotter robot) {
		super();
		this.myPlotter = robot;

		PlotterSettings settings = myPlotter.getSettings();

		add(userGeneralStartGcode
				= new SelectTextArea(
						"userGeneralStartGcode",
						Translator.get("PlotterSettings.userGeneralStartGcode"),
						settings.getUserGcode().getUserGeneralStartGcode()
				)
		);
		add(userGeneralEndGcode
				= new SelectTextArea(
						"userGeneralEndGcode",
						Translator.get("PlotterSettings.userGeneralEndGcode"),
						settings.getUserGcode().getUserGeneralEndGcode()
				)
		);
		add(userToolChangeStartGcode
				= new SelectTextArea(
						"userToolChangeStartGcode",
						Translator.get("PlotterSettings.userToolChangeStartGcode"),
						settings.getUserGcode().getUserToolChangeStartGcode()
				)
		);
		add(userToolChangeEndGcode
				= new SelectTextArea(
						"userToolChangeEndGcode",
						Translator.get("PlotterSettings.userToolChangeEndGcode"),
						settings.getUserGcode().getUserToolChangeEndGcode()
				)
		);
		
		
		add(replacementPatternList
				= new SelectTextArea(
						"replacementPattern",
						Translator.get("PlotterSettings.replacementPattern"),
						settings.getUserGcode().getAllPattern(settings)
				)
		);


		JPanel bottom = new JPanel(new FlowLayout());
		buttonSave = new JButton(Translator.get("Save"));
		bottom.add(buttonSave);
		buttonSave.addActionListener((e) -> {
			save();
		});

		add(bottom);

	}

	private void save() {

		PlotterSettings settings = myPlotter.getSettings();
		settings.getUserGcode().setUserGeneralStartGcode(userGeneralStartGcode.getText());
		settings.getUserGcode().setUserGeneralEndGcode(userGeneralEndGcode.getText());
		settings.getUserGcode().setUserToolChangeStartGcode(userToolChangeStartGcode.getText());
		settings.getUserGcode().setUserToolChangeEndGcode(userToolChangeEndGcode.getText());

	}
}
