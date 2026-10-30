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
	public static final String SHOW_ORIGIN = "show origin";
	public static final String SHOW_PEN_UP = "show pen up";
	public static final String SHOW_WORK_AREA = "show work area";
	public static final String ANTIALIAS = "antialias";
	public static final String SPEED_OVER_QUALITY = "speed over quality";
	public static final String DRAW_WHILE_RUNNING = "draw all while running";
	public static final String DRAG_SPEED = "dragSpeed";

	static private SelectBoolean showOrigin;
	static private SelectBoolean showPenUp;
	static private SelectBoolean showWorkArea;
	static private SelectBoolean antialias;
	static private SelectBoolean speedOverQuality;
	static private SelectBoolean showAllWhileDrawing;
	static private SelectSpinner dragSpeed;
	static EventListenerList listeners = new EventListenerList();

	public static void addListener(PropertyChangeListener p) {
		listeners.add(PropertyChangeListener.class,p);
	}

	public static void removeListener(PropertyChangeListener p) {
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
	
	public static SelectPanel buildPanel() {
		Preferences prefs = getPreferences();
		SelectPanel panel = new SelectPanel();
		showOrigin = new SelectBoolean(SHOW_ORIGIN,Translator.get("GFXPreferences.showOrigin"),prefs.getBoolean(SHOW_ORIGIN, false));
		showPenUp = new SelectBoolean(SHOW_PEN_UP,Translator.get("GFXPreferences.showPenUp"),prefs.getBoolean(SHOW_PEN_UP, false));
		showWorkArea = new SelectBoolean(SHOW_WORK_AREA,Translator.get("GFXPreferences.showWorkArea"),prefs.getBoolean(SHOW_WORK_AREA, false));
		antialias = new SelectBoolean(ANTIALIAS,Translator.get("GFXPreferences.antialias"),prefs.getBoolean(ANTIALIAS, true));
		speedOverQuality = new SelectBoolean(SPEED_OVER_QUALITY,Translator.get("GFXPreferences.speedVSQuality"),prefs.getBoolean(SPEED_OVER_QUALITY, true));
		showAllWhileDrawing = new SelectBoolean(DRAW_WHILE_RUNNING,Translator.get("GFXPreferences.showAllWhileDrawing"),prefs.getBoolean(DRAW_WHILE_RUNNING, true));
		dragSpeed = new SelectSpinner(DRAG_SPEED, Translator.get("GFXPreferences.dragSpeed"), 1, 5, prefs.getInt(DRAG_SPEED, 1));

		panel.add(showOrigin);
		panel.add(showPenUp);
		panel.add(showWorkArea);
		panel.add(showAllWhileDrawing);
		panel.add(antialias);
		panel.add(speedOverQuality);
		panel.add(dragSpeed);

		GFXPreferences.addListener( e -> {
			if(SHOW_ORIGIN.equals(e.getPropertyName())) showOrigin.setSelected((boolean)e.getNewValue());
		} );
		GFXPreferences.addListener( e -> {
			if(SHOW_PEN_UP.equals(e.getPropertyName())) showPenUp.setSelected((boolean)e.getNewValue());
		} );
		GFXPreferences.addListener( e -> {
			if(SHOW_WORK_AREA.equals(e.getPropertyName())) showWorkArea.setSelected((boolean)e.getNewValue());
		} );

		showOrigin.addSelectListener((e)-> GFXPreferences.setShowOrigin((boolean)e.getNewValue()) );
		showPenUp.addSelectListener((e)-> GFXPreferences.setShowPenUp((boolean)e.getNewValue()) );

		return panel;
	}

	public static void save() {
		Preferences prefs = getPreferences();
		prefs.putBoolean(SHOW_ORIGIN, showOrigin.isSelected());
		prefs.putBoolean(SHOW_PEN_UP, showPenUp.isSelected());
		prefs.putBoolean(SHOW_WORK_AREA, showWorkArea.isSelected());
		prefs.putBoolean(ANTIALIAS, antialias.isSelected());
		prefs.putBoolean(SPEED_OVER_QUALITY, speedOverQuality.isSelected());
		prefs.putBoolean(DRAW_WHILE_RUNNING, showAllWhileDrawing.isSelected());
		prefs.putInt(DRAG_SPEED, dragSpeed.getValue());
	}

	public static boolean getShowOrigin() {
		Preferences prefs = getPreferences();
		return prefs.getBoolean(SHOW_ORIGIN,false);
	}

	public static void setShowOrigin(boolean state) {
		boolean old = getShowOrigin();
		if(old != state) {
			Preferences prefs = getPreferences();
			prefs.putBoolean(SHOW_ORIGIN, state);
			firePropertyChange(new PropertyChangeEvent(prefs,SHOW_ORIGIN,old,state));
		}
	}

	public static boolean getShowPenUp() {
		Preferences prefs = getPreferences();
		return prefs.getBoolean(SHOW_PEN_UP,false);
	}
	
	public static void setShowPenUp(boolean state) {
		boolean old = getShowPenUp();
		if(old != state) {
			Preferences prefs = getPreferences();
			prefs.putBoolean(SHOW_PEN_UP, state);
			firePropertyChange(new PropertyChangeEvent(prefs,SHOW_PEN_UP,old,state));
		}
	}

	public static boolean getShowWorkArea() {
		Preferences prefs = getPreferences();
		return prefs.getBoolean(SHOW_WORK_AREA,false);
	}
	
	public static void setShowWorkArea(boolean state) {
		boolean old = getShowWorkArea();
		if(old != state) {
			Preferences prefs = getPreferences();
			prefs.putBoolean(SHOW_WORK_AREA, state);
			firePropertyChange(new PropertyChangeEvent(prefs,SHOW_WORK_AREA,old,state));
		}
	}
}
