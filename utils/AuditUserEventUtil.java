// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto.AuditUserEventDtoBuilder;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventResourceDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AuditUserEventUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserProvisioningUtils.class);
  private static final String REMOVED = "REMOVED";
  private static final String NEWLY_ADDED = "NEWLY_ADDED";
  private static final String ROLE_CHANGE = "ROLE_CHANGE";
  private static final String PERMISSIONS_CHANGE = "PERMISSIONS_CHANGE";

  /**
   * Method to create AuditUserEventDto.
   */
  public AuditUserEventDtoBuilder constructAuditUserEventDto(UsersListResponseResource userListResponseResource,
      AuditEventStatus status) {
    return AuditUserEventDto.builder().eventTimeStamp(DateTime.now(DateTimeZone.UTC))
        .auditUserEventResources(
            constructCreateUserAuditResourceDtos(userListResponseResource.getUsers().get(0).getAccessResources()))
        .eventStatus(status).targetUserName(userListResponseResource.getUsers().get(0).getUserName())
        .targetUserId(userListResponseResource.getUsers().get(0).getId()).eventType(AuditEventType.UPDATE_USER);
  }

  /**
   * Method to create AuditUserEventResourceDtos.
   */
  public List<AuditUserEventResourceDto> constructCreateUserAuditResourceDtos(List<AccessResources> accessResources) {
    List<AuditUserEventResourceDto> auditUserEventResourceDtoList = new ArrayList<>();
    try {
      accessResources.stream().forEach(aR -> {
        if (aR.getId() == null && aR.getIds() != null) {
          aR.getIds()
              .forEach(a1 -> auditUserEventResourceDtoList.add(new AuditUserEventResourceDto(a1,
                  aR.getType(), aR.getRole(), aR.getPermissions().stream().filter(Objects::nonNull)
                      .map(PermissionDto::getLabel).collect(Collectors.joining(",")),
                  AccessResourceStatus.ACTIVE.name())));
        } else {
          auditUserEventResourceDtoList
              .add(new AuditUserEventResourceDto(aR.getId(),
                  aR.getType(), aR.getRole(), aR.getPermissions().stream().filter(Objects::nonNull)
                      .map(PermissionDto::getLabel).collect(Collectors.joining(",")),
                  AccessResourceStatus.ACTIVE.name()));
        }
      });
    } catch (Exception e) {
      LOGGER.error("Error while constructing Audit Entry Resources: {}.", e.getMessage());
    }
    return auditUserEventResourceDtoList;
  }

  /**
   * Method to create AuditUserEventResourceDtos for edit user.
   */
  public List<AuditUserEventResourceDto> constructEditUserAuditResourceDtos(List<AccessResources> prevAccessResources1,
      List<AccessResources> latestAccessResources1) {
    List<AuditUserEventResourceDto> auditUserEventResources = new ArrayList<>();
    try {

      List<AccessResources> prevAccessResources = convertAccessResources(prevAccessResources1);
      List<AccessResources> latestAccessResources = convertAccessResources(latestAccessResources1);

      Map<String, AccessResources> prevAccessResourcesMap = prevAccessResources.stream()
          .collect(Collectors.toMap(AccessResources::getId, accessResource -> accessResource));
      constructAuditResource(prevAccessResources, latestAccessResources, prevAccessResourcesMap,
          auditUserEventResources);
      for (AccessResources prevAccessResource : prevAccessResources) {
        auditUserEventResources.add(AuditUserEventResourceDto.builder().resourceId(prevAccessResource.getId())
            .resourceType(prevAccessResource.getType()).roles(prevAccessResource.getRole())
            .permissions(prevAccessResource.getPermissions().stream().filter(Objects::nonNull)
                .map(PermissionDto::getLabel).collect(Collectors.joining(",")))
            .userResourceAccessStatus(AccessResourceStatus.BLOCKED.name()).activityDesc(REMOVED).build());
      }
    } catch (Exception e) {
      LOGGER.error("Error while constructing Audit Entry Resources for edit user: {}.", e.getMessage());
    }
    return auditUserEventResources;
  }

  private List<AccessResources> convertAccessResources(List<AccessResources> accessResources) {
    List<AccessResources> enrichedAccessresource = new ArrayList<>();

    accessResources.stream().forEach(aR -> {
      if (aR.getId() == null && !aR.getIds().isEmpty()) {
        aR.getIds().forEach(a1 -> enrichedAccessresource.add(createAccessResource(a1, aR)));
      } else {
        enrichedAccessresource.add(createAccessResource(aR.getId(), aR));
      }
    });
    return enrichedAccessresource;
  }

  private AccessResources createAccessResource(String a1, AccessResources accessRes) {
    AccessResources accessResources = new AccessResources();
    BeanUtils.copyProperties(accessRes, accessResources);
    accessResources.setId(a1);
    accessResources.setPermissions(accessRes.getPermissions());
    accessResources.setStatus(accessRes.getStatus());
    return accessResources;
  }

  private void constructAuditResource(List<AccessResources> prevAccessResources,
      List<AccessResources> latestAccessResources, Map<String, AccessResources> prevAccessResourcesMap,
      List<AuditUserEventResourceDto> auditUserEventResources) {

    for (AccessResources latestAccessResource : latestAccessResources) {
      if (CollectionUtils.isNotEmpty(prevAccessResources) && prevAccessResources
          .removeIf(prevAccessResource -> (prevAccessResource.getId().equals(latestAccessResource.getId())))) {
        AuditUserEventResourceDto auditUserEventResource = constructAuditUserEventForChangedResource(
            prevAccessResourcesMap.get(latestAccessResource.getId()), latestAccessResource);
        if (auditUserEventResource != null) {
          auditUserEventResources.add(auditUserEventResource);
        }

      } else {
        auditUserEventResources.add(AuditUserEventResourceDto.builder().resourceId(latestAccessResource.getId())
            .resourceType(latestAccessResource.getType()).roles(latestAccessResource.getRole())
            .permissions(latestAccessResource.getPermissions().stream().filter(Objects::nonNull)
                .map(PermissionDto::getLabel).collect(Collectors.joining(",")))
            .userResourceAccessStatus(AccessResourceStatus.ACTIVE.name()).activityDesc(NEWLY_ADDED).build());
      }
    }
  }

  private AuditUserEventResourceDto constructAuditUserEventForChangedResource(AccessResources prevAccessResource,
      AccessResources latestAccessResource) {
    if (prevAccessResource.getRole().equals(latestAccessResource.getRole())) {
      if (prevAccessResource.getRole().equals(DataConstants.ADMIN)) {
        return null;
      }
      boolean isBothContainsSamePerms =
          isBothAccessResourcesContainsSamePerms(prevAccessResource, latestAccessResource);
      if (isBothContainsSamePerms) {
        return null;
      }
      return AuditUserEventResourceDto.builder().resourceId(latestAccessResource.getId())
          .resourceType(latestAccessResource.getType()).roles(latestAccessResource.getRole())
          .permissions(latestAccessResource.getPermissions().stream().filter(Objects::nonNull)
              .map(PermissionDto::getLabel).collect(Collectors.joining(",")))
          .userResourceAccessStatus(AccessResourceStatus.ACTIVE.name()).activityDesc(PERMISSIONS_CHANGE)
          .prevPermissions(prevAccessResource.getPermissions().stream().filter(Objects::nonNull)
              .map(PermissionDto::getLabel).collect(Collectors.joining(",")))
          .build();
    } else {
      return AuditUserEventResourceDto.builder().resourceId(latestAccessResource.getId())
          .resourceType(latestAccessResource.getType()).roles(latestAccessResource.getRole())
          .permissions(latestAccessResource.getPermissions().stream().filter(Objects::nonNull)
              .map(PermissionDto::getLabel).collect(Collectors.joining(",")))
          .userResourceAccessStatus(AccessResourceStatus.ACTIVE.name()).activityDesc(ROLE_CHANGE)
          .prevRole(prevAccessResource.getRole()).build();
    }
  }

  private boolean isBothAccessResourcesContainsSamePerms(AccessResources prevAccessResource,
      AccessResources latestAccessResource) {
    List<String> prevPerms =
        prevAccessResource.getPermissions().stream().map(PermissionDto::getLabel).collect(Collectors.toList());
    List<String> latestPerms =
        latestAccessResource.getPermissions().stream().map(PermissionDto::getLabel).collect(Collectors.toList());
    return CollectionUtils.isEqualCollection(prevPerms, latestPerms);
  }
}
