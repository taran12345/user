// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.gbp.commons.bigdata.Issuer;
import com.paysafe.ss.logging.correlation.feign.InternalHeadersContext;
import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.repository.specifications.AuditSearchCriteria;
import com.paysafe.upf.user.provisioning.repository.specifications.AuditSearchOperation;
import com.paysafe.upf.user.provisioning.repository.specifications.AuditUserEventSpecification;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AsyncAuditService;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventResourceDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AuditUserEventsResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.audit.AuditInfoRequestResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RefreshScope
@Transactional
public class AuditServiceImpl implements AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
  private static final String UNKNOWN = "Unknown";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Autowired
  private AuditUserEventRepository auditUserEventRepository;

  @Autowired
  private AsyncAuditService asyncAuditService;

  @Override
  public void createAuditEntry(AuditUserEventDto auditUserEventDto) {
    try {
      String eventId = UUID.randomUUID().toString();
      auditUserEventDto.setId(eventId);
      auditUserEventDto.setCreatedDate(DateTime.now(DateTimeZone.UTC));

      Map<String, String> headers = InternalHeadersContext.getInternalHeaders();
      auditUserEventDto.setCorrelationId(MapUtils.getString(
              headers, InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER));

      populateFields(auditUserEventDto);

      if (StringUtils.isEmpty(auditUserEventDto.getCreatedBy()) && CommonThreadLocal.getAuthLocal() != null) {
        setCreatedBy(auditUserEventDto);
      }

      List<AuditUserEventResourceDto> auditUserEventResourceDtos = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(auditUserEventDto.getAuditUserEventResources())) {
        auditUserEventResourceDtos = new ArrayList<>(auditUserEventDto.getAuditUserEventResources());
        auditUserEventDto.getAuditUserEventResources().clear();
        auditUserEventResourceDtos.forEach(auditUserEventResourceDto -> {
          auditUserEventResourceDto.setEventId(eventId);
          auditUserEventResourceDto.setId(UUID.randomUUID().toString());
          auditUserEventResourceDto.setAuditUserEventDto(auditUserEventDto);
        });
      }
      asyncAuditService.createAuditEntry(auditUserEventDto, auditUserEventResourceDtos);
    } catch (Exception e) {
      logger.error("Error while making Audit Entry: {}.", e.getMessage());
    }
  }

  private void populateFields(AuditUserEventDto auditUserEventDto) {
    if (StringUtils.isEmpty(auditUserEventDto.getUserIpAddress())
        && CommonThreadLocal.getRequestContextLocal() != null) {
      setUserIpAddress(auditUserEventDto);
    }

    if (StringUtils.isEmpty(auditUserEventDto.getApplication()) && CommonThreadLocal.getAuthLocal() != null) {
      setApplication(auditUserEventDto);
    }

    if (StringUtils.isEmpty(auditUserEventDto.getSourceApp()) && CommonThreadLocal.getAuthLocal() != null) {
      setSourceApp(auditUserEventDto);
    }

    if (StringUtils.isEmpty(auditUserEventDto.getBrowser()) && CommonThreadLocal.getRequestContextLocal() != null) {
      setBrowser(auditUserEventDto);
    }
  }

  private void setUserIpAddress(AuditUserEventDto auditUserEventDto) {
    if (StringUtils.isNotEmpty(CommonThreadLocal.getRequestContextLocal().getIpAddress())) {
      auditUserEventDto.setUserIpAddress(CommonThreadLocal.getRequestContextLocal().getIpAddress());
    } else {
      auditUserEventDto.setUserIpAddress(UNKNOWN);
    }
  }

  private void setBrowser(AuditUserEventDto auditUserEventDto) {
    if (StringUtils.isNotEmpty(CommonThreadLocal.getRequestContextLocal().getUserAgent())) {
      auditUserEventDto.setBrowser(CommonThreadLocal.getRequestContextLocal().getUserAgent());
    } else {
      auditUserEventDto.setBrowser(UNKNOWN);
    }
  }

  private void setSourceApp(AuditUserEventDto auditUserEventDto) {
    if (CommonThreadLocal.getAuthLocal().getIssuer() != null) {
      auditUserEventDto.setSourceApp(
          CommonThreadLocal.getAuthLocal().getIssuer().equals(Issuer.OKTA) ? "BUSINESS_PORTAL" : "BACKOFFICE");
    } else {
      auditUserEventDto.setSourceApp(UNKNOWN);
    }
  }

  private void setApplication(AuditUserEventDto auditUserEventDto) {
    if (StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      auditUserEventDto.setApplication(CommonThreadLocal.getAuthLocal().getApplication());
    } else {
      auditUserEventDto.setApplication(UNKNOWN);
    }
  }

  private void setCreatedBy(AuditUserEventDto auditUserEventDto) {
    String userName = CommonThreadLocal.getAuthLocal().getUserName();
    if (StringUtils.isEmpty(userName) || ("system").equalsIgnoreCase(userName)
        || ("unknown").equalsIgnoreCase(userName)) {
      auditUserEventDto.setCreatedBy(auditUserEventDto.getTargetUserName());
    } else {
      auditUserEventDto.setCreatedBy(userName);
    }
  }

  @Override
  public ObjectNode getAuditInfo(AuditInfoRequestResource fetchAuditEventRequest) {

    AuditUserEventSpecification eventSpecification = new AuditUserEventSpecification();

    if (StringUtils.isNotEmpty(fetchAuditEventRequest.getCreatedBy())) {
      eventSpecification
          .add(new AuditSearchCriteria("createdBy", fetchAuditEventRequest.getCreatedBy(), AuditSearchOperation.EQUAL));

    }

    if (StringUtils.isNotEmpty(fetchAuditEventRequest.getSourceApp())) {
      eventSpecification
          .add(new AuditSearchCriteria("sourceApp", fetchAuditEventRequest.getSourceApp(), AuditSearchOperation.EQUAL));
    }

    if (StringUtils.isNotEmpty(fetchAuditEventRequest.getApplication())) {
      eventSpecification.add(
          new AuditSearchCriteria("application", fetchAuditEventRequest.getApplication(), AuditSearchOperation.EQUAL));
    }
    configureDateQueryParams(fetchAuditEventRequest, eventSpecification);
    addUserInfoToAuditUserEventSpecification(fetchAuditEventRequest, eventSpecification);

    if (null != fetchAuditEventRequest.getEventType()) {
      if (fetchAuditEventRequest.getEventType().compareTo(AuditEventType.SIGNUP) == 0) {
        constructSignUpSearchCriteria(eventSpecification);
      } else if (fetchAuditEventRequest.getEventType().compareTo(AuditEventType.FORGOT_PASSWORD) == 0) {
        constructForgotPasswordSearchCriteria(eventSpecification);
      } else {
        eventSpecification.add(
            new AuditSearchCriteria("eventType", fetchAuditEventRequest.getEventType(), AuditSearchOperation.EQUAL));
      }
    } else {
      if (StringUtils.isNotEmpty(fetchAuditEventRequest.getTargetUserName())
          || StringUtils.isNotEmpty(fetchAuditEventRequest.getTargetUserId())) {
        constructActivityOnUserSearchCriteria(eventSpecification, fetchAuditEventRequest);
      }
    }

    AuditUserEventsResponse auditUserEventsResponse = new AuditUserEventsResponse();
    auditUserEventsResponse.setAuditUserEventsList(new ArrayList<>());

    Pageable pagedAndsortedByEventTimeStampDesc = PageRequest.of(fetchAuditEventRequest.getPage(),
        fetchAuditEventRequest.getPageSize(), Sort.by("eventTimeStamp").descending());

    Page<AuditUserEvent> pagedResponse =
        auditUserEventRepository.findAll(eventSpecification, pagedAndsortedByEventTimeStampDesc);

    auditUserEventsResponse.getAuditUserEventsList().addAll(pagedResponse.getContent());
    auditUserEventsResponse.setTotalElements(pagedResponse.getTotalElements());
    auditUserEventsResponse.setHasNext(pagedResponse.hasNext());
    auditUserEventsResponse.setHasPrevious(pagedResponse.hasPrevious());

    return MAPPER.convertValue(auditUserEventsResponse, ObjectNode.class);
  }

  private void addUserInfoToAuditUserEventSpecification(AuditInfoRequestResource fetchAuditEventRequest,
      AuditUserEventSpecification eventSpecification) {
    if (StringUtils.isNotEmpty(fetchAuditEventRequest.getTargetUserName())) {
      eventSpecification.add(new AuditSearchCriteria("targetUserName", fetchAuditEventRequest.getTargetUserName(),
          AuditSearchOperation.EQUAL));
    }
    if (StringUtils.isNotEmpty(fetchAuditEventRequest.getTargetUserId())) {
      eventSpecification.add(new AuditSearchCriteria("targetUserId", fetchAuditEventRequest.getTargetUserId(),
          AuditSearchOperation.EQUAL));
    }
  }

  private void constructForgotPasswordSearchCriteria(AuditUserEventSpecification eventSpecification) {
    eventSpecification
        .add(new AuditSearchCriteria("eventType", AuditEventType.FORGOT_PASSWORD, AuditSearchOperation.OR_EQUAL));
    eventSpecification
        .add(new AuditSearchCriteria("eventType", AuditEventType.RESET_PASSWORD, AuditSearchOperation.OR_EQUAL));
  }

  private void constructSignUpSearchCriteria(AuditUserEventSpecification eventSpecification) {
    eventSpecification
        .add(new AuditSearchCriteria("eventType", AuditEventType.ADD_USER, AuditSearchOperation.OR_EQUAL));
    eventSpecification.add(new AuditSearchCriteria("eventType", AuditEventType.SIGNUP, AuditSearchOperation.OR_EQUAL));
  }

  private void constructActivityOnUserSearchCriteria(AuditUserEventSpecification eventSpecification,
      AuditInfoRequestResource fetchAuditEventRequest) {
    if (StringUtils.isNotEmpty(fetchAuditEventRequest.getTargetUserName())) {
      eventSpecification.add(new AuditSearchCriteria("createdBy", fetchAuditEventRequest.getTargetUserName(),
          AuditSearchOperation.NOT_EQUAL));
    } else {
      eventSpecification.add(new AuditSearchCriteria("targetUserId", fetchAuditEventRequest.getTargetUserId(),
          AuditSearchOperation.NOT_EQUAL));
    }
    eventSpecification
        .add(new AuditSearchCriteria("eventType", AuditEventType.ADD_USER, AuditSearchOperation.NOT_EQUAL));
    eventSpecification.add(
        new AuditSearchCriteria("eventType", AuditEventType.SEND_REACTIVATION_LINK, AuditSearchOperation.NOT_EQUAL));
    eventSpecification
        .add(new AuditSearchCriteria("eventType", AuditEventType.FORGOT_PASSWORD, AuditSearchOperation.NOT_EQUAL));
  }

  private void configureDateQueryParams(AuditInfoRequestResource fetchAuditEventRequest,
      AuditUserEventSpecification eventSpecification) {

    if (null != fetchAuditEventRequest.getFromDate() && null != fetchAuditEventRequest.getToDate()
        && fetchAuditEventRequest.getFromDate().equals(fetchAuditEventRequest.getToDate())) {
      eventSpecification.add(
          new AuditSearchCriteria("createdDate", fetchAuditEventRequest.getFromDate(), AuditSearchOperation.EQUAL));
    } else {
      if (null != fetchAuditEventRequest.getFromDate()) {
        eventSpecification.add(new AuditSearchCriteria("createdDate", fetchAuditEventRequest.getFromDate(),
            AuditSearchOperation.GREATER_THAN_EQUAL));
      }
      if (null != fetchAuditEventRequest.getToDate()) {
        eventSpecification.add(new AuditSearchCriteria("createdDate", fetchAuditEventRequest.getToDate(),
            AuditSearchOperation.LESS_THAN_EQUAL));
      }
    }
  }

}
