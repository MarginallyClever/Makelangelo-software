package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Toggles the GFXPreferences.showOrigin
 */
public class ActionShowWorkArea extends AbstractAction {
    public ActionShowWorkArea() {
        super(Translator.get("GFXPreferences.showWorkArea"));

        this.putValue(Action.SHORT_DESCRIPTION,Translator.get("GFXPreferences.showWorkArea"));
        this.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-workArea-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GFXPreferences.setShowWorkArea(!GFXPreferences.getShowWorkArea());
    }
}
