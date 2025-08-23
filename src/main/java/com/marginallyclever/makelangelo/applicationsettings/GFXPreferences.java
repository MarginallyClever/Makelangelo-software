package com.marginallyclever.makelangelo.applicationsettings;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectPanel;
import com.marginallyclever.donatello.select.SelectSpinner;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

/**
 * Graphics plottersettings
 * @author Dan Royer
 */
public class GFXPreferences {
	static private SelectBoolean activateDebugGL;
	static private SelectBoolean activateTraceGL;
	static private SelectBoolean showPenUp;
	static private SelectBoolean antialias;
	static private SelectBoolean speedOverQuality;
	static private SelectBoolean showAllWhileDrawing;
	static private SelectSpinner dragSpeed;
	static EventListenerList listeners = new EventListenerList();

	static public void addListener(PropertyChangeListener p) {
		listeners.add(PropertyChangeListener.class,p);
	}

	static public void removeListener(PropertyChangeListener p) {
		listeners.remove(PropertyChangeListener.class,p);
	}

	static protected void firePropertyChange(PropertyChangeEvent e) {
		for(PropertyChangeListener p : listeners.getListeners(PropertyChangeListener.class)) {
			p.propertyChange(e);
		}
	}

	/**
	 * Get the preferences node for this class
	 * @return Preferences node
	 */
	static private Preferences getPreferences() {
		return PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
	}
	
	static public SelectPanel buildPanel() {
		Preferences prefs = getPreferences();

		SelectPanel panel = new SelectPanel();
		activateDebugGL = new SelectBoolean("debugGL",Translator.get("GFXPreferences.debugGL"),prefs.getBoolean("debug GL", prefs.getBoolean("debugGL",false)));
		activateTraceGL = new SelectBoolean("traceGL",Translator.get("GFXPreferences.traceGL"),prefs.getBoolean("trace GL", prefs.getBoolean("traceGL",false)));
		showPenUp = new SelectBoolean("penup",Translator.get("GFXPreferences.showPenUp"),prefs.getBoolean("show pen up", false));
		antialias = new SelectBoolean("antialias",Translator.get("GFXPreferences.antialias"),prefs.getBoolean("antialias", true));
		speedOverQuality = new SelectBoolean("SpeedVSQuality",Translator.get("GFXPreferences.speedVSQuality"),prefs.getBoolean("speed over quality", true));
		showAllWhileDrawing = new SelectBoolean("drawWhileRunning",Translator.get("GFXPreferences.showAllWhileDrawing"),prefs.getBoolean("Draw all while running", true));
		dragSpeed = new SelectSpinner("dragSpeed", Translator.get("GFXPreferences.dragSpeed"), 1, 5, prefs.getInt("dragSpeed", 1));

		panel.add(activateDebugGL);
		panel.add(activateTraceGL);
		panel.add(showPenUp);
		panel.add(showAllWhileDrawing);
		panel.add(antialias);
		panel.add(speedOverQuality);
		panel.add(dragSpeed);

		GFXPreferences.addListener((e)->{
			showPenUp.setSelected((boolean)e.getNewValue());
		});
		showPenUp.addSelectListener((e)->{
			GFXPreferences.setShowPenUp((boolean)e.getNewValue());
		});

		return panel;
	}

	static public void save() {
		Preferences prefs = getPreferences();
		prefs.putBoolean("debug GL", activateDebugGL.isSelected());
		prefs.putBoolean("trace GL", activateTraceGL.isSelected());
		prefs.putBoolean("show pen up", showPenUp.isSelected());
		prefs.putBoolean("antialias", antialias.isSelected());
		prefs.putBoolean("speed over quality", speedOverQuality.isSelected());
		prefs.putBoolean("Draw all while running", showAllWhileDrawing.isSelected());
		prefs.putInt("dragSpeed", dragSpeed.getValue());
	}

	static public boolean getShowPenUp() {
		Preferences prefs = getPreferences();
		return prefs.getBoolean("show pen up",false);
	}
	
	static public void setShowPenUp(boolean state) {
		boolean old = getShowPenUp();
		if(old != state) {
			Preferences prefs = getPreferences();
			prefs.putBoolean("show pen up", state);
			firePropertyChange(new PropertyChangeEvent(prefs,"show pen up",old,state));
		}
	}
}
