package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Toggles the GFXPreferences.showOrigin
 */
public class ActionShowOrigin extends AbstractAction {
    public ActionShowOrigin() {
        super(Translator.get("GFXPreferences.showOrigin"));

        this.putValue(Action.SHORT_DESCRIPTION,Translator.get("GFXPreferences.showOrigin"));
        this.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/actions/icons8-target-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GFXPreferences.setShowOrigin(!GFXPreferences.getShowOrigin());
    }
}
