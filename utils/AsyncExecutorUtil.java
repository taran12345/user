// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AsyncExecutorUtil {

  /**
   * customized completable future all of function.
   *
   * @param completableFutures list of futures
   * @param <T> object nodes
   * @return returns consolidates features
   */
  public static <T> List<T> allOf(List<CompletableFuture<T>> completableFutures) {
    if (CollectionUtils.isNotEmpty(completableFutures)) {
      return completableFutures.stream().map(CompletableFuture::join).collect(Collectors.<T>toList());
    }
    return Collections.emptyList();
  }
}
