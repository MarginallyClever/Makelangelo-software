package com.marginallyclever.util;

import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Peter Colapietro
 * @since v7.1.4
 * */
public interface Ancestryable<P extends Preferences> {

  /**
   *
   * @return
   */
  Map<String, P> getChildren();

  /**
   *
   * @return
   */
  Map<String, String> getRoot();

}
