package com.marginallyclever.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Created on 6/7/15.
 *
 * @author Peter Colapietro
 * @since v7.1.3
 */
public class MarginallyCleverPreferences extends AbstractPreferences {

    /**
     * FIXME
     */
    protected Map<String, String> preferences = new HashMap<>();

    /**
     * FIXME
     */
    protected Map<String, AbstractPreferences> children = new HashMap<>();

    /**
     * Creates a preference node with the specified parent and the specified
     * name relative to its parent.
     *
     * @param parent the parent of this preference node, or null if this
     *               is the root.
     * @param name   the name of this preference node, relative to its parent,
     *               or <tt>""</tt> if this is the root.
     * @throws IllegalArgumentException if <tt>name</tt> contains a slash
     *                                  (<tt>'/'</tt>),  or <tt>parent</tt> is <tt>null</tt> and
     *                                  name isn't <tt>""</tt>.
     */
    protected MarginallyCleverPreferences(AbstractPreferences parent, String name) {
        super(parent, name);
    }

    @Override
    protected void putSpi(String key, String value) {
        preferences.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return preferences.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        preferences.remove(key);
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        for (Map.Entry<String, AbstractPreferences> entry: children.entrySet()) {
            entry.getValue().removeNode();
            flush();
        }
        children.clear();
        preferences.clear();
        removeNode();
        flush();
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        final Set<String> keySet = preferences.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        final Set<String> childrenNames = children.keySet();
        return childrenNames.toArray(new String[childrenNames.size()]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        AbstractPreferences childPreferenceNode = children.get(name);
        if (childPreferenceNode == null) {
            childPreferenceNode = new MarginallyCleverPreferences(this, name);
            children.put(name, childPreferenceNode);
        }
        return childPreferenceNode;
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        // NOOP
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        //FIXME
    }
}
