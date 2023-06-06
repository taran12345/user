// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
  /**
   * Method that can be used to serialize any Java value as a String.
   *
   * @param object object
   * @return JSON string
   */
  public static String toJsonString(final Object object) {
    String result;
    ObjectMapper mapper = new ObjectMapper();

    try {
      result = mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return result;
  }
}