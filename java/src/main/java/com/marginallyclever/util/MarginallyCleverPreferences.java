package com.marginallyclever.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Created on 6/7/15.
 *
 * @author Peter Colapietro
 * @since v7.1.4
 *
 * @see <a href="http://www.davidc.net/programming/java/java-preferences-using-file-backing-store">Java Preferences using a file as the backing store</a>
 * @see <a href="http://stackoverflow.com/a/25548386">SO answer to: How to synchronize file access in a Java servlet?</a>
 */
public class MarginallyCleverPreferences extends AbstractPreferences {

    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(MarginallyCleverPreferences.class);

    /**
     *
     */
    private final Map<String, String> root;

    /**
     *
     */
    private final Map<String, MarginallyCleverPreferences> children;

    /**
     *
     */
    private boolean thisIsRemoved;

    /**
     *
     */
    private final Object mutex = new Object();

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
    public MarginallyCleverPreferences(AbstractPreferences parent, String name) {
        super(parent, name);
        logger.info("Instantiating node {}", name);
        root = new TreeMap<>();
        children = new TreeMap<>();
        try {
            sync();
        } catch (BackingStoreException e) {
            logger.error("Unable to sync on creation of node {}. {}", name, e);
        }
    }

    @Override
    protected void putSpi(@NotNull String key, String value) {
        root.put(key, value);
        try {
            flush();
        } catch (BackingStoreException e) {
            logger.error("Unable to flush after putting {}. {}", key, e);
        }
    }

    @Override
    protected String getSpi(@NotNull String key) {
        return root.get(key);
    }

    @Override
    protected void removeSpi(@NotNull String key) {
        root.remove(key);
        try {
            flush();
        } catch (BackingStoreException e) {
            logger.error("Unable to flush after removing {}. {}", key, e);
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        thisIsRemoved = true;
        flush();
    }

    @NotNull
    @Override
    protected String[] keysSpi() throws BackingStoreException {
        final Set<String> keySet = root.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @NotNull
    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        final Set<String> childrenNames = children.keySet();
        return childrenNames.toArray(new String[childrenNames.size()]);
    }

    @NotNull
    @Override
    protected AbstractPreferences childSpi(@NotNull String name) {
        MarginallyCleverPreferences childPreferenceNode = children.get(name);
        if (childPreferenceNode == null || childPreferenceNode.isRemoved()) {
            childPreferenceNode = new MarginallyCleverPreferences(this, name);
            children.put(name, childPreferenceNode);
        }
        return childPreferenceNode;
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        if(isRemoved()) {
            return;
        }
        File file = MarginallyCleverJsonFilePreferencesFactory.getPreferencesFile();
        if (!file.exists()) {
            return;
        }
        synchronized (mutex) {
            final Properties p = new Properties();
            try {
                try(final FileInputStream inStream = new FileInputStream(file)) {
                    p.load(inStream);
                }

                final StringBuilder sb = new StringBuilder();
                getPath(sb);
                final String path = sb.toString();

                final Enumeration<?> pnen = p.propertyNames();
                while (pnen.hasMoreElements()) {
                    final String propKey = (String) pnen.nextElement();
                    if (propKey.startsWith(path)) {
                        final String subKey = propKey.substring(path.length());
                        // Only load immediate descendants
                        if (subKey.indexOf('.') == -1) {
                            root.put(subKey, p.getProperty(propKey));
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        final File file = MarginallyCleverJsonFilePreferencesFactory.getPreferencesFile();
        synchronized (mutex) {
            try {
                final Properties p = new Properties();
                final StringBuilder sb = new StringBuilder();
                getPath(sb);
                final String path = sb.toString();
                if (file.exists()) {
                    try (final FileInputStream fileInputStream = new FileInputStream(file)) {
                        p.load(fileInputStream);
                    }

                    final List<String> toRemove = new ArrayList<>();

                    // Make a list of all direct children of this node to be removed
                    final Enumeration<?> pnen = p.propertyNames();
                    while (pnen.hasMoreElements()) {
                        final String propKey = (String) pnen.nextElement();
                        if (propKey.startsWith(path)) {
                            final String subKey = propKey.substring(path.length());
                            // Only do immediate descendants
                            if (subKey.indexOf('.') == -1) {
                                toRemove.add(propKey);
                            }
                        }
                    }

                    // Remove them now that the enumeration is done with
                    for (String propKey : toRemove) {
                        p.remove(propKey);
                    }
                }
                // If this node hasn't been removed, add back in any values
                if (!thisIsRemoved) {
                    for (String s : root.keySet()) {
                        p.setProperty(path + s, root.get(s));
                    }
                }
                final String marginallyCleverPreferencesFileComments = "MarginallyCleverPreferences";
                try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    p.store(fileOutputStream, marginallyCleverPreferencesFileComments);
                }
            } catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }

    /**
     *
     * @param sb String builder
     */
    private void getPath(StringBuilder sb)
    {
        MarginallyCleverPreferences parent = null;
        try {
            parent = (MarginallyCleverPreferences) parent();
        } catch (ClassCastException e) {
            //logger.info("NOOP");
        }
        if (parent == null) {
            return;
        }
        parent.getPath(sb);
        sb.append(name()).append('.');
    }

    /**
     *
     * @return
     */
    public Map<String, ? extends Object> getChildren() {
        return new TreeMap<>(children);
    }

    /**
     *
     * @return
     */
    public Map<String, String> getRoot() {
        return new TreeMap<>(root);
    }
}
