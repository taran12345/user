// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

public class LoggingUtil {

  private static final String SPECIAL_CHARS_REGEX = "[\n|\r|\t]";
  private static final String SPECIAL_CHARS_REPLACEMENT = "_";

  private LoggingUtil() {
    // intentinally empty.
  }

  /**
   * this method replaces special characters from the input data.
   */
  public static String replaceSpecialChars(Object data) {
    return (data != null
        ? data.toString().replaceAll(SPECIAL_CHARS_REGEX, SPECIAL_CHARS_REPLACEMENT)
        : null);
  }
}
