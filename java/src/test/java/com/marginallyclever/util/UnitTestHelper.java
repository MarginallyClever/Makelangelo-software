package com.marginallyclever.util;

/**
 * @author Peter Colapietro
 * @since v7.1.4
 */
public final class UnitTestHelper {

  /**
   * "If you are not concerned with potential overflow problems
   * this function will perform about 20-30 times faster than using Integer.parseInt()." ~ Jonas Klemming
   *
   * @param string
   * @return if the string is an integer.
   * @see <a href="http://stackoverflow.com/a/237204">What's the best way to check to see if a String represents an integer in Java?</a>
   */
  public static boolean isInteger(String string) {
    if (string == null) {
      return false;
    }
    int length = string.length();
    if (length == 0) {
      return false;
    }
    int i = 0;
    if (string.charAt(0) == '-') {
      if (length == 1) {
        return false;
      }
      i = 1;
    }
    for (; i < length; i++) {
      char c = string.charAt(i);
      if (c <= '/' || c >= ':') {
        return false;
      }
    }
    return true;
  }

  /**
   * NOOP Constructor.
   *
   * @throws IllegalStateException
   */
  private UnitTestHelper() throws IllegalStateException {
    throw new IllegalStateException();
  }
}
