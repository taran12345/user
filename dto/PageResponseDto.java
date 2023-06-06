// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(Include.NON_NULL)
public class PageResponseDto<T> {
  Long totalItems;
  int totalPages;
  int page;
  int size;
  List<T> items;

  /**
   * all args constructor.
   *
   * @param totalItems - count of total number of elements.
   * @param totalPages - total number of pages possible with the size.
   * @param page - current page number.
   * @param size - page size.
   * @param items - results for the current page.
   */
  public PageResponseDto(Long totalItems, int totalPages, int page, int size, List<T> items) {
    super();
    this.totalItems = totalItems;
    this.totalPages = totalPages;
    this.page = page;
    this.size = size;
    this.items = items;
  }

}