// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PageResponseDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupNameAvailabilityResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupsListRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicy;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyCreateRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyUpdateRequestResource;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "paysafe-ss-access-groups", configuration = FeignClientConfig.class)
public interface AccessGroupFeignClient {

  @RequestMapping(value = "/admin/access-groups/v1/accessgroups", method = RequestMethod.POST)
  ResponseEntity<AccessGroupResponseResource> createAccessGroup(@RequestBody CustomAccessGroupDto request);

  @RequestMapping(value = "/admin/access-groups/v1/accessgroups/ids", method = RequestMethod.GET)
  ResponseEntity<List<String>> getAccessGroupsPresentFromInputList(
      @RequestParam(value = "accessGroups") List<String> accessGroupIds);

  @RequestMapping(value = "/admin/access-groups/v1/accessgroups/availablilty/names", method = RequestMethod.POST)
  ResponseEntity<AccessGroupNameAvailabilityResponseResource> getAccessGroupNameAvailabilityList(
      @RequestParam(value = "accessGroupName") List<String> accessGroupNames);

  @RequestMapping(value = "/admin/access-groups/v1/accessgroups/name={name}", method = RequestMethod.GET)
  ResponseEntity<AccessGroupResponseResource> fetchAccessGroupByName(
      @PathVariable(value = "name") String accessGroupName);

  @RequestMapping(method = RequestMethod.GET, value = "/admin/access-groups/v1/accessgroups/{accessGroupCode}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<AccessGroupResponseResource> fetchAccessGroupByCode(
      @PathVariable(value = "accessGroupCode") String accessGroupCode);

  @RequestMapping(value = "/admin/access-groups/v1/accessgroups/policies", method = RequestMethod.POST)
  ResponseEntity<AccessPolicy> createAccessPolicy(@RequestBody AccessPolicyCreateRequest accessPolicyCreateRequest);

  @RequestMapping(method = RequestMethod.GET, value = "/admin/access-groups/v1/accessgroups",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  PageResponseDto<AccessGroupResponseResource> fetchAccessGroups(@RequestParam(value = "merchantId") String merchantId,
      @RequestParam(value = "merchantType") String merchantType,
      @RequestParam(value = "accessGroupType") AccessGroupType accessGroupType,
      @RequestParam(value = "createdBy") String createdBy,
      @RequestParam(value = "resourceType") ResourceType resourceType,
      @RequestParam(value = "resourceId") String resourceId, @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer pageSize);

  @RequestMapping(method = RequestMethod.POST, value = "/admin/access-groups/v1/accessgroups/list",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  List<AccessGroupResponseResource> getAccessGroupsFromInputList(
      @RequestBody AccessGroupsListRequestResource accessGroupsListRequestResource);

  @RequestMapping(method = RequestMethod.PATCH, value = "/admin/access-groups/v1/accesspolicies/{accessPolicyCode}")
  AccessPolicy updateAccessPolicy(@PathVariable("accessPolicyCode") String code,
      @RequestBody AccessPolicyUpdateRequestResource accessPolicyUpdateRequestResource);
}
