package com.marginallyclever.util;

import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Peter Colapietro
 * @since v7.1.4
 */
public interface Ancestryable {

  /**
   * @return
   */
  Map<String, Preferences> getChildren();

  /**
   * @return
   */
  Map<String, String> getRoot();

}
