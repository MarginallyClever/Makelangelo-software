package com.marginallyclever.util;

import org.json.JSONObject;

import java.util.Map;
import java.util.prefs.AbstractPreferences;

/**
 * Created on 6/7/15.
 *
 * @author Peter Colapietro
 * @since 0.1.0
 */
public final class MarginallyCleverJsonPreferences extends MarginallyCleverPreferences {

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
    public MarginallyCleverJsonPreferences(AbstractPreferences parent, String name) {
        super(parent, name);
    }

    /**
     *
     * @return
     */
    public JSONObject getPreferencesAsJsonObject() {
        final JSONObject jsonObject = (JSONObject) JSONObject.wrap(super.preferences);
        for(Map.Entry<String, AbstractPreferences> entry: super.children.entrySet()) {
            jsonObject.put(entry.getKey(),JSONObject.wrap(entry.getValue()));
        }
        return jsonObject;
    }
}
