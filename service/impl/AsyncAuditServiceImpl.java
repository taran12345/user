// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.domain.AuditUserEventResource;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventResourceRepository;
import com.paysafe.upf.user.provisioning.service.AsyncAuditService;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventResourceDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RefreshScope
@Transactional
public class AsyncAuditServiceImpl implements AsyncAuditService {

  private static final Logger logger = LoggerFactory.getLogger(AsyncAuditServiceImpl.class);

  @Autowired
  private AuditUserEventRepository auditUserEventRepository;

  @Autowired
  private AuditUserEventResourceRepository auditUserEventResourceRepository;

  @Override
  @Async("upfAsyncExecutor")
  public void createAuditEntry(AuditUserEventDto auditUserEventDto,
      List<AuditUserEventResourceDto> auditUserEventResourceDtos) {
    logEvent(auditUserEventDto, auditUserEventResourceDtos);
    AuditUserEvent auditUserEvent = toAuditUserEvent(auditUserEventDto);
    auditUserEventRepository.saveAndFlush(auditUserEvent);
    if (CollectionUtils.isNotEmpty(auditUserEventResourceDtos)) {
      for (AuditUserEventResourceDto auditUserEventResourceDto : auditUserEventResourceDtos) {
        if (auditUserEventResourceDto != null) {
          AuditUserEventResource auditUserEventResource =
              toAuditUserEventResource(auditUserEventResourceDto, auditUserEvent);
          auditUserEventResourceRepository.saveAndFlush(auditUserEventResource);
        }
      }
    }
  }

  private void logEvent(AuditUserEventDto auditUserEventDto,
      List<AuditUserEventResourceDto> auditUserEventResourceDtos) {
    String logPattern = "USER_ACTIVITY_LOG" + " | ";
    AuditUserEventDto auditUserEventDto1 = new AuditUserEventDto();
    BeanUtils.copyProperties(auditUserEventDto, auditUserEventDto1);
    auditUserEventDto1.setAuditUserEventResources(auditUserEventResourceDtos);
    try {
      logger.info(logPattern + auditUserEventDto.getEventType().name() + " | "
          + new ObjectMapper().writeValueAsString(auditUserEventDto1));
    } catch (JsonProcessingException e) {
      logger.error("error while constructing json object for logging", e);
    }
  }

  private AuditUserEvent toAuditUserEvent(AuditUserEventDto auditUserEventDto) {
    AuditUserEvent auditUserEvent = new AuditUserEvent();
    BeanUtils.copyProperties(auditUserEventDto, auditUserEvent);
    return auditUserEvent;
  }

  private AuditUserEventResource toAuditUserEventResource(AuditUserEventResourceDto auditUserEventResourceDto,
      AuditUserEvent auditUserEvent) {
    AuditUserEventResource auditUserEventResource = new AuditUserEventResource();
    BeanUtils.copyProperties(auditUserEventResourceDto, auditUserEventResource);
    auditUserEventResource.setAuditUserEvent(auditUserEvent);
    return auditUserEventResource;
  }
}
