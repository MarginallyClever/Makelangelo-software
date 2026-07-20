package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.Translator;

import javax.swing.*;

/**
 * An AbstractAction that has a name (for display and translation purposes).  the name is also used for storing
 * menu shortcut in the config file.
 */
public abstract class NamedAbstractAction extends AbstractAction {
    private String name;

    /**
     * @param name the name of the translation table lookup for the label
     */
    public NamedAbstractAction(String name) {
        super(name == null ? "" : Translator.get(name));
        this.name = name;
    }

    /**
     * @param name the name of the translation table lookup for the label
     * @param icon the 16x16 icon for the action.
     */
    public NamedAbstractAction(String name, Icon icon) {
        super(Translator.get(name), icon);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
