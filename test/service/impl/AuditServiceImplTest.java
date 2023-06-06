// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.security.commons.RequestContext;
import com.paysafe.upf.user.provisioning.service.AsyncAuditService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventResourceDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.audit.AuditInfoRequestResource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AuditServiceImplTest {

  @Mock
  private AsyncAuditService mockAsyncAuditService;

  @InjectMocks
  private AuditServiceImpl auditServiceImplUnderTest;

  @Mock
  private AuditUserEventRepository auditUserEventRepository;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testCreateAuditEntry() {
    final AuditUserEventDto auditUserEventDto =
        new AuditUserEventDto("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId",
        AuditEventStatus.SUCCESS, "browser", "userIpAddress", "sourceApp", "userStatus", "application",
        "eventData", "createdBy", Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus")),
        DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), null,"correlationId");

    auditServiceImplUnderTest.createAuditEntry(auditUserEventDto);

    mockAsyncAuditService.createAuditEntry(
        new AuditUserEventDto("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
                "userResourceAccessStatus")),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), null, "correlationId"),
        Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
            "userResourceAccessStatus")));
    verify(mockAsyncAuditService, times(1)).createAuditEntry(any(), any());
  }

  @Test
  public void testCreateAuditEntryWithJwt() {

    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));

    RequestContext requestContext = new RequestContext();
    requestContext.setIpAddress("192.168.1.56");
    requestContext.setUserAgent("Chrome");
    CommonThreadLocal.setRequestContextLocal(requestContext);

    final AuditUserEventDto auditUserEventDto =
        new AuditUserEventDto("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId",
        AuditEventStatus.SUCCESS, null, null, null, "userStatus", null,
        "eventData", null, Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles",
            "permissions", "userResourceAccessStatus")),
        DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), null, "correlationId");

    auditServiceImplUnderTest.createAuditEntry(auditUserEventDto);

    mockAsyncAuditService.createAuditEntry(
        new AuditUserEventDto("id", AuditEventType.SIGNUP, "targetUserName", "targetUserId", AuditEventStatus.SUCCESS,
            "browser",
            "userIpAddress", "sourceApp", "userStatus", "application", "eventData", "createdBy",
            Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
                "userResourceAccessStatus")),
            DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), null, "correlationId"),
        Arrays.asList(new AuditUserEventResourceDto("resourceId", "resourceType", "roles", "permissions",
            "userResourceAccessStatus")));
    verify(mockAsyncAuditService, times(1)).createAuditEntry(any(), any());
    CommonThreadLocal.unsetAuthLocal();
    CommonThreadLocal.unsetRequestContextLocal();
  }

  @Test
  public void getAuditInfoTest_signup() {

    AuditUserEvent aud = new AuditUserEvent();
    aud.setId(UUID.randomUUID().toString());
    aud.setEventTimeStamp(new DateTime());
    aud.setCreatedBy("createdBy");
    aud.setApplication("app");
    aud.setEventType(AuditEventType.SIGNUP);

    List<AuditUserEvent> list = new ArrayList<>();
    list.add(new AuditUserEvent());

    AuditInfoRequestResource fetchAuditEventRequest = AuditInfoRequestResource.builder().application("app")
        .createdBy("createdBy").eventType(AuditEventType.SIGNUP).page(0).pageSize(10).build();

    Pageable pagedAndsortedByEventTimeStampDesc = PageRequest.of(fetchAuditEventRequest.getPage(),
        fetchAuditEventRequest.getPageSize(), Sort.by("eventTimeStamp").descending());

    Page<AuditUserEvent> auditEventsPage = new PageImpl<>(list, pagedAndsortedByEventTimeStampDesc, 20);
    when(auditUserEventRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(auditEventsPage);

    ObjectNode node = auditServiceImplUnderTest.getAuditInfo(fetchAuditEventRequest);
    assertNotNull(node);

  }

  @Test
  public void getAuditInfo_whenEventTypeNullAndWithTargetUserName_shouldSucceed() {
    List<AuditUserEvent> list = new ArrayList<>();
    list.add(new AuditUserEvent());
    AuditInfoRequestResource fetchAuditEventRequest = AuditInfoRequestResource.builder().application("app")
        .createdBy("createdBy").eventType(null).targetUserName("testuser@gmail.com").fromDate(new DateTime(456767))
        .toDate(new DateTime(656767)).page(0).pageSize(10).build();
    Pageable pagedAndsortedByEventTimeStampDesc = PageRequest.of(fetchAuditEventRequest.getPage(),
        fetchAuditEventRequest.getPageSize(), Sort.by("eventTimeStamp").descending());
    Page<AuditUserEvent> auditEventsPage = new PageImpl<>(list, pagedAndsortedByEventTimeStampDesc, 20);
    when(auditUserEventRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(auditEventsPage);
    ObjectNode node = auditServiceImplUnderTest.getAuditInfo(fetchAuditEventRequest);
    assertNotNull(node);
  }

  @Test
  public void getAuditInfo_whenEventTypeNullAndWithTargetUserId_shouldSucceed() {
    List<AuditUserEvent> list = new ArrayList<>();
    list.add(new AuditUserEvent());
    AuditInfoRequestResource fetchAuditEventRequest = AuditInfoRequestResource.builder().application("app")
        .createdBy("createdBy").eventType(null).targetUserName("testuser@gmail.com").fromDate(new DateTime(656767))
        .toDate(new DateTime(656767)).page(0).pageSize(10).build();
    Pageable pagedAndsortedByEventTimeStampDesc = PageRequest.of(fetchAuditEventRequest.getPage(),
        fetchAuditEventRequest.getPageSize(), Sort.by("eventTimeStamp").descending());
    Page<AuditUserEvent> auditEventsPage = new PageImpl<>(list, pagedAndsortedByEventTimeStampDesc, 20);
    when(auditUserEventRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(auditEventsPage);
    ObjectNode node = auditServiceImplUnderTest.getAuditInfo(fetchAuditEventRequest);
    assertNotNull(node);
  }

  @Test
  public void getAuditInfoTest_forgotpassword() {

    AuditUserEvent aud = new AuditUserEvent();
    aud.setId(UUID.randomUUID().toString());
    aud.setEventTimeStamp(new DateTime());
    aud.setCreatedBy("createdBy");
    aud.setApplication("app");
    aud.setEventType(AuditEventType.SIGNUP);

    List<AuditUserEvent> list = new ArrayList<>();
    list.add(new AuditUserEvent());

    AuditInfoRequestResource fetchAuditEventRequest = AuditInfoRequestResource.builder().application("app")
        .createdBy("createdBy").eventType(AuditEventType.FORGOT_PASSWORD).page(0).pageSize(10).build();

    Pageable pagedAndsortedByEventTimeStampDesc = PageRequest.of(fetchAuditEventRequest.getPage(),
        fetchAuditEventRequest.getPageSize(), Sort.by("eventTimeStamp").descending());

    Page<AuditUserEvent> auditEventsPage = new PageImpl<>(list, pagedAndsortedByEventTimeStampDesc, 20);
    when(auditUserEventRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(auditEventsPage);

    ObjectNode node = auditServiceImplUnderTest.getAuditInfo(fetchAuditEventRequest);
    assertNotNull(node);

  }

  @Test
  public void getAuditInfoTest() {

    AuditUserEvent aud = new AuditUserEvent();
    aud.setId(UUID.randomUUID().toString());
    aud.setEventTimeStamp(new DateTime());
    aud.setCreatedBy("createdBy");
    aud.setApplication("app");
    aud.setEventType(AuditEventType.SIGNUP);

    List<AuditUserEvent> list = new ArrayList<>();
    list.add(new AuditUserEvent());

    AuditInfoRequestResource fetchAuditEventRequest = AuditInfoRequestResource.builder().application("app")
        .createdBy("createdBy").eventType(AuditEventType.SEND_REACTIVATION_LINK).page(0).pageSize(10).build();

    Pageable pagedAndsortedByEventTimeStampDesc = PageRequest.of(fetchAuditEventRequest.getPage(),
        fetchAuditEventRequest.getPageSize(), Sort.by("eventTimeStamp").descending());

    Page<AuditUserEvent> auditEventsPage = new PageImpl<>(list, pagedAndsortedByEventTimeStampDesc, 20);
    when(auditUserEventRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(auditEventsPage);

    ObjectNode node = auditServiceImplUnderTest.getAuditInfo(fetchAuditEventRequest);
    assertNotNull(node);

  }

}
