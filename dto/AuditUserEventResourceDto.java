// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditUserEventResourceDto {
  private String id;
  private String eventId;
  private String resourceId;
  private String resourceType;
  private String roles;
  private String permissions;
  private String userResourceAccessStatus;
  private AuditUserEventDto auditUserEventDto;
  private String activityDesc;
  private String prevPermissions;
  private String prevRole;

  /**
   * Constructor.
   * 
   * @param resourceId resourceId
   * @param resourceType resourceType
   * @param roles roles
   * @param permissions permissions
   * @param userResourceAccessStatus userResourceAccessStatus
   */
  public AuditUserEventResourceDto(String resourceId, String resourceType, String roles, String permissions,
      String userResourceAccessStatus) {
    this.resourceId = resourceId;
    this.resourceType = resourceType;
    this.roles = roles;
    this.permissions = permissions;
    this.userResourceAccessStatus = userResourceAccessStatus;
  }

  @Override
  public String toString() {
    return "AuditUserEventResourceDto{" + "id='" + id + '\'' + ", eventId='" + eventId + '\'' + ", resourceId='"
        + resourceId + '\'' + ", resourceType='" + resourceType + '\'' + ", roles='" + roles + '\'' + ", permissions='"
        + permissions + '\'' + ", userResourceAccessStatus='" + userResourceAccessStatus + '\'' + '}';
  }
}
