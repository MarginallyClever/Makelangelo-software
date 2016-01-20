package com.marginallyclever.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.jetbrains.annotations.NotNull;

import com.marginallyclever.makelangelo.Log;

/**
 * Created on 6/7/15.
 *
 * @author Peter Colapietro
 * @see <a href="http://www.davidc.net/programming/java/java-preferences-using-file-backing-store">Java Preferences using a file as the backing store</a>
 * @see <a href="http://stackoverflow.com/a/25548386">SO answer to: How to synchronize file access in a Java servlet?</a>
 * @since v7.1.4
 */
public class MarginallyCleverPreferences extends AbstractPreferences implements Ancestryable {

  /**
   *
   */
  private final Map<String, String> root;

  /**
   *
   */
  private final Map<String, Preferences> children;

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
    Log.message("Instantiating node "+ name);
    root = new TreeMap<>();
    children = new TreeMap<>();
    try {
      sync();
    } catch (BackingStoreException e) {
      Log.error("Unable to sync on creation of node "+name+". "+e);
    }
  }

  @Override
  protected void putSpi(@NotNull String key, String value) {
    root.put(key, value);
    try {
      flush();
    } catch (BackingStoreException e) {
    	Log.error("Unable to flush after putting "+key+". "+e);
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
    	Log.error("Unable to flush after removing "+key+". "+e);
    }
  }

  @Override
  protected void removeNodeSpi() throws BackingStoreException {
    flush();
    thisIsRemoved = true;
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

  /**
   * http://stackoverflow.com/a/24249709
   *
   * @param name
   * @return
   */
  @NotNull
  @Override
  protected AbstractPreferences childSpi(@NotNull String name) {
	AbstractPreferences childPreferenceNode = (AbstractPreferences) children.get(name);
    boolean isChildRemoved = false;
    if (childPreferenceNode != null) {
      try {
        isChildRemoved = getIsRemoved(childPreferenceNode);
      } catch (ReflectiveOperationException e) {
        Log.error( e.getMessage() );
      }
    }
    if (childPreferenceNode == null || isChildRemoved) {
      final AbstractPreferences castedPreferences = new MarginallyCleverPreferences(this, name);
      childPreferenceNode = castedPreferences;
      children.put(name, childPreferenceNode);
    }
    return childPreferenceNode;
  }

  /**
   * FIXME - Pure hack to get around erasure.
   *
   * @param abstractPreference
   * @return
   * @throws ReflectiveOperationException
   */
  private boolean getIsRemoved(AbstractPreferences abstractPreference) throws ReflectiveOperationException {
    Log.message( abstractPreference.toString() );
    final Method declaredMethod = AbstractPreferences.class.getDeclaredMethod("isRemoved");
    declaredMethod.setAccessible(true);
    Object isRemoved = declaredMethod.invoke(abstractPreference, new Object[]{null});
    return (boolean) isRemoved;
  }

  @Override
  protected void syncSpi() throws BackingStoreException {
    if (isRemoved()) {
      return;
    }
    final File propertiesPreferencesFile = MarginallyCleverPreferencesFileFactory.getPropertiesPreferencesFile();
    if (!propertiesPreferencesFile.exists()) {
      return;
    }
    synchronized (mutex) {
      final Properties p = new Properties();
      try {
        try (final InputStream inStream = new FileInputStream(propertiesPreferencesFile)) {
          p.load(inStream);
        }

        final StringBuilder sb = new StringBuilder();
        getPath(sb);
        final String path = sb.toString();

        final Enumeration<?> propertyNames = p.propertyNames();
        while (propertyNames.hasMoreElements()) {
          final String propKey = (String) propertyNames.nextElement();
          if (propKey.startsWith(path)) {
            final String subKey = propKey.substring(path.length());
            // Only load immediate descendants
            if (subKey.indexOf('.') == -1) {
              root.put(subKey, p.getProperty(propKey));
            }
          }
        }
      } catch (IOException e) {
        throw new BackingStoreException(e);
      }
    }
  }

  @Override
  protected void flushSpi() throws BackingStoreException {
    final File xmlPreferencesFile = MarginallyCleverPreferencesFileFactory.getXmlPreferencesFile();
    final File file = MarginallyCleverPreferencesFileFactory.getPropertiesPreferencesFile();
    synchronized (mutex) {
      try {
        final Properties p = new Properties();
        final StringBuilder sb = new StringBuilder();
        getPath(sb);
        final String path = sb.toString();
        if (file.exists()) {
          try (final InputStream fileInputStream = new FileInputStream(file)) {
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
          storePreferencesInFile(file, p);
          storeNodeInFile(xmlPreferencesFile);
        }
      } catch (IOException e) {
        throw new BackingStoreException(e);
      }
    }
  }

  private void storeNodeInFile(File file) throws IOException, BackingStoreException {
    final Preferences parent = recursiveGetParent(this.parent() != null ? this.parent() : this);
    try (final OutputStream fileOutputStream = new FileOutputStream(file)) {
      parent.exportNode(fileOutputStream);
    }
  }

  private Preferences recursiveGetParent(Preferences node) {
    Preferences parent = node.parent();
    if (parent == null) {
      node = parent;
    } else {
      recursiveGetParent(parent);
    }
    return node;
  }

  /**
   * @param file
   * @param p
   * @throws IOException
   */
  private void storePreferencesInFile(File file, Properties p) throws IOException, BackingStoreException {
    final String marginallyCleverPreferencesFileComments = "MarginallyCleverPreferences";
    try (final OutputStream fileOutputStream = new FileOutputStream(file)) {
      p.store(fileOutputStream, marginallyCleverPreferencesFileComments);
    }
  }

  /**
   * @param sb String builder
   */
  private void getPath(StringBuilder sb) {
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
   * @return
   */
  @Override
  public Map<String, Preferences> getChildren() {
    return new TreeMap<>(children);
  }

  /**
   * @return
   */
  @Override
  public Map<String, String> getRoot() {
    return new TreeMap<>(root);
  }

}
