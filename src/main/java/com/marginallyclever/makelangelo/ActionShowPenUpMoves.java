package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.applicationsettings.GFXPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.Objects;

public class ActionShowPenUpMoves extends AbstractAction {
    public ActionShowPenUpMoves() {
        super(Translator.get("GFXPreferences.showPenUp"));

        this.putValue(Action.SHORT_DESCRIPTION,Translator.get("GFXPreferences.showPenUp"));
        this.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));//"ctrl M"
        this.putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/makelangelo/icons8-plane-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GFXPreferences.setShowPenUp(!GFXPreferences.getShowPenUp());
    }
}
