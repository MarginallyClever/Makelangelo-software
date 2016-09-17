package com.marginallyclever.makelangelo.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class GFXPreferences extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6401497555251948788L;
	private Preferences prefs;
	
	@SuppressWarnings("unused")
	private JFrame rootFrame;

	private JCheckBox showPenUpCheckbox;
	private JCheckBox antialiasOnCheckbox;
	private JCheckBox speedOverQualityCheckbox;
	private JCheckBox drawAllWhileRunningCheckbox;

	
	public GFXPreferences(JFrame arg0) {
		this.rootFrame=arg0;

		prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
	}
	
	public void buildPanel() {
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);
	
		showPenUpCheckbox = new JCheckBox(Translator.get("MenuGraphicsPenUp"));
		antialiasOnCheckbox = new JCheckBox(Translator.get("MenuGraphicsAntialias"));
		speedOverQualityCheckbox = new JCheckBox(Translator.get("MenuGraphicsSpeedVSQuality"));
		drawAllWhileRunningCheckbox = new JCheckBox(Translator.get("MenuGraphicsDrawWhileRunning"));
	
		showPenUpCheckbox.setSelected(prefs.getBoolean("show pen up", false));
		antialiasOnCheckbox.setSelected(prefs.getBoolean("antialias", true));
		speedOverQualityCheckbox.setSelected(prefs.getBoolean("speed over quality", true));
		drawAllWhileRunningCheckbox.setSelected(prefs.getBoolean("Draw all while running", true));
	
		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4;  c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);
	
		int y = 0;
	
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		this.add(showPenUpCheckbox, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		this.add(drawAllWhileRunningCheckbox, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		this.add(antialiasOnCheckbox, c);
		y++;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = y;
		this.add(speedOverQualityCheckbox, c);
		y++;
	}
	
	public void save() {
		//allowMetrics = allow_metrics.isSelected();
		prefs.putBoolean("show pen up", showPenUpCheckbox.isSelected());
		prefs.putBoolean("antialias", antialiasOnCheckbox.isSelected());
		prefs.putBoolean("speed over quality", speedOverQualityCheckbox.isSelected());
		prefs.putBoolean("Draw all while running", drawAllWhileRunningCheckbox.isSelected());
	}
	
	public void cancel() {
		
	}
	
	public boolean getShowPenUp() {
		return (showPenUpCheckbox != null) && showPenUpCheckbox.isSelected();
	}
}
