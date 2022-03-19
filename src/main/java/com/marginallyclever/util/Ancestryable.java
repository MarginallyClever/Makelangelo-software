package com.marginallyclever.util;

import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Peter Colapietro
 * @since v7.1.4
 */
public interface Ancestryable {

  /**
   * @return children
   */
  Map<String, Preferences> getChildren();

  /**
   * @return root
   */
  Map<String, String> getRoot();

}
