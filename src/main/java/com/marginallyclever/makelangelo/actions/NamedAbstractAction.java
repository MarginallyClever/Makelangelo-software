package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.Translator;

import javax.swing.*;

/**
 * An AbstractAction that has a name (for display and translation purposes).  the name is also used for storing
 * menu shortcut in the config file.
 */
public abstract class NamedAbstractAction extends AbstractAction {
    private String name;

    public NamedAbstractAction(String name) {
        super(name == null ? "" : Translator.get(name));
        this.name = name;
    }

    public NamedAbstractAction(String name, Icon icon) {
        super(Translator.get(name), icon);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
