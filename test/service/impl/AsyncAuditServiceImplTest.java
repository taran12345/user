// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.domain.AuditUserEventResource;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventResourceRepository;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventResourceDto;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

public class AsyncAuditServiceImplTest {

  @Mock
  private AuditUserEventRepository mockAuditUserEventRepository;
  @Mock
  private AuditUserEventResourceRepository mockAuditUserEventResourceRepository;

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @InjectMocks
  private AsyncAuditServiceImpl asyncAuditServiceImplUnderTest;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testCreateAuditEntry() {

    final AuditUserEventDto auditUserEventDto = new AuditUserEventDto("id", AuditEventType.SIGNUP, "targetUserName",
        "targetUserId",
        AuditEventStatus.SUCCESS, "browser", "userIpAddress", "sourceApp", "userStatus", "application",
        "eventData", "createdBy", Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus")),
        DateTime.now(DateTimeZone.UTC), new DateTime(DateTimeZone.UTC), null, "correlationId");
    final List<AuditUserEventResourceDto> auditUserEventResourceDtos =
        Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
            "userResourceAccessStatus"));

    final AuditUserEvent auditUserEvent =
        new AuditUserEvent("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId",
        AuditEventStatus.SUCCESS, "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
        "createdBy", Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus", null, null, null)),
        DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId");
    when(mockAuditUserEventRepository
        .saveAndFlush(
            new AuditUserEvent("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles", "permissions",
                "userResourceAccessStatus", null, null, null)),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC),
                    "correlationId"))).thenReturn(auditUserEvent);

    final AuditUserEventResource auditUserEventResource = new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy", Arrays.asList(),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null);
    when(mockAuditUserEventResourceRepository.saveAndFlush(new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy", Arrays.asList(),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null)))
            .thenReturn(auditUserEventResource);

    asyncAuditServiceImplUnderTest.createAuditEntry(auditUserEventDto, auditUserEventResourceDtos);
    verify(mockAuditUserEventResourceRepository, times(1)).saveAndFlush(any());
  }

  @Test
  public void testUpdateAuditEntry() {

    final AuditUserEventDto auditUserEventDto =
        new AuditUserEventDto("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
            AuditEventStatus.SUCCESS, "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
            "createdBy", Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles",
                "permissions", "userResourceAccessStatus")),
            DateTime.now(DateTimeZone.UTC), new DateTime(DateTimeZone.UTC), null, "correlationId");
    final List<AuditUserEventResourceDto> auditUserEventResourceDtos =
        Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
            "userResourceAccessStatus"));

    final AuditUserEvent auditUserEvent =
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
        AuditEventStatus.SUCCESS, "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
        "createdBy", Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus", null, null, null)),
        DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId");
    when(mockAuditUserEventRepository.saveAndFlush(
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles", "permissions",
                "userResourceAccessStatus", null, null, null)),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC),
                "correlationId"))).thenReturn(auditUserEvent);

    final AuditUserEventResource auditUserEventResource = new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(), DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC),"correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null);
    when(mockAuditUserEventResourceRepository.saveAndFlush(new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(), DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null)))
            .thenReturn(auditUserEventResource);

    asyncAuditServiceImplUnderTest.createAuditEntry(auditUserEventDto, auditUserEventResourceDtos);
    verify(mockAuditUserEventResourceRepository, times(1)).saveAndFlush(any());
  }

  @Test
  public void testUpdateAuditEntry_Exception() {

    final AuditUserEventDto auditUserEventDto =
        new AuditUserEventDto("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
            AuditEventStatus.SUCCESS, "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
            "createdBy", Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles",
                "permissions", "userResourceAccessStatus")),
            DateTime.now(DateTimeZone.UTC), new DateTime(DateTimeZone.UTC), null,"correlationId");
    final List<AuditUserEventResourceDto> auditUserEventResourceDtos =
        Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
            "userResourceAccessStatus"));

    final AuditUserEvent auditUserEvent =
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
        AuditEventStatus.SUCCESS, "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
        "createdBy", Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus", null, null, null)),
        DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId");
    when(mockAuditUserEventRepository
        .saveAndFlush(new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
            AuditEventStatus.SUCCESS,
            "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles", "permissions",
                "userResourceAccessStatus", null, null, null)),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC),
                "correlationId"))).thenReturn(auditUserEvent);

    final AuditUserEventResource auditUserEventResource = new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy", Arrays.asList(),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null);
    when(mockAuditUserEventResourceRepository.saveAndFlush(new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy", Arrays.asList(),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null)))
            .thenReturn(auditUserEventResource);
    when(identityManagementFeignClient.getUser(any(), any())).thenThrow(InternalErrorException.builder().build());
    asyncAuditServiceImplUnderTest.createAuditEntry(auditUserEventDto, auditUserEventResourceDtos);
    verify(mockAuditUserEventResourceRepository, times(1)).saveAndFlush(any());
  }

  @Test
  public void testUpdateAuditEntry_WithoutException() {

    final AuditUserEventDto auditUserEventDto =
        new AuditUserEventDto("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
            AuditEventStatus.SUCCESS, "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
            "createdBy", Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles",
                "permissions", "userResourceAccessStatus")),
            DateTime.now(DateTimeZone.UTC), new DateTime(DateTimeZone.UTC), null, "correlationId");
    final List<AuditUserEventResourceDto> auditUserEventResourceDtos =
        Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
            "userResourceAccessStatus"));

    final AuditUserEvent auditUserEvent =
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
        AuditEventStatus.SUCCESS, "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData",
        "createdBy", Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus", null, null, null)),
        DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId");
    when(mockAuditUserEventRepository
        .saveAndFlush(new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId",
            AuditEventStatus.SUCCESS,
            "browser", "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(new AuditUserEventResource("id", null, "resourceId", "resourceType", "roles", "permissions",
                "userResourceAccessStatus", null, null, null)),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC),
                "correlationId"))).thenReturn(auditUserEvent);

    final AuditUserEventResource auditUserEventResource = new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy", Arrays.asList(),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null);
    when(mockAuditUserEventResourceRepository.saveAndFlush(new AuditUserEventResource("id",
        new AuditUserEvent("id", AuditEventType.UPDATE_USER, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy", Arrays.asList(),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), "correlationId"),
        "resourceId", "resourceType", "roles", "permissions", "userResourceAccessStatus", null, null, null)))
            .thenReturn(auditUserEventResource);
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    asyncAuditServiceImplUnderTest.createAuditEntry(auditUserEventDto, auditUserEventResourceDtos);
    verify(mockAuditUserEventResourceRepository, times(1)).saveAndFlush(any());
  }
}
