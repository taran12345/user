// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.audit.AuditInfoRequestResource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/auditevents", "/user-provisioning/v1/auditevents"})
public class AuditEventsController {

  @Autowired
  private AuditService auditService;

  /**
   * API to create audit event.
   * 
   */
  @PostMapping(
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(
      value = "API to create audit event")
  public ResponseEntity<HttpStatus> createAuditEvent(
      @RequestHeader( value = "Authorization", required = false) String auth,
      @RequestHeader(
          value = "Application", required = false) String application,
      @ApiParam(
          value = "request resource") @RequestBody AuditUserEventDto auditUserEventDto) {

    if (auditUserEventDto.getEventTimeStampMillis() != null) {
      auditUserEventDto.setEventTimeStamp(new DateTime(auditUserEventDto.getEventTimeStampMillis(), DateTimeZone.UTC));
    }
    
    auditService.createAuditEntry(auditUserEventDto);

    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * API to fetch audit event.
   * 
   */
  @GetMapping(
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation("Api to get User audit logs")
  public ResponseEntity<ObjectNode> getAuditInfo(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(
          value = "createdBy", required = false) String createdBy,
      @RequestParam(value = "targetUserName", required = false) String targetUserName,
      @RequestParam(value = "targetUserId", required = false) String targetUserId,
      @ApiParam(value = "provide the date in dd.MM.yyyy format") @RequestParam(value = "fromDate",
          required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") DateTime fromDate,
      @ApiParam(value = "provide the date in dd.MM.yyyy format") @RequestParam(value = "toDate",
          required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") DateTime toDate,
      @RequestParam(value = "eventType", required = false) AuditEventType eventType,
      @ApiParam(
          value = "BUSINESS_PORTAL, BACKOFFICE") @RequestParam(
              value = "sourceApp", required = false) String sourceApp,
      @ApiParam(
          value = "PORTAL, SKRILL, NETELLER") @RequestParam(
          value = "application", required = false) String application,
      @RequestParam(
          value = "page", defaultValue = "0") Integer page,
      @RequestParam(
          value = "pageSize", defaultValue = "50") Integer pageSize
      
  ) {
    

    if (fromDate != null) {
      fromDate = new DateTime(fromDate.getYear(), fromDate.getMonthOfYear(), fromDate.getDayOfMonth(), 0, 0,
          DateTimeZone.UTC);
    }
    if (toDate != null) {
      toDate = new DateTime(toDate.getYear(), toDate.getMonthOfYear(), toDate.getDayOfMonth(), 0, 0,
          DateTimeZone.UTC);
    }
    
    AuditInfoRequestResource fetchAuditEventRequest =
        AuditInfoRequestResource.builder().eventType(eventType).fromDate(fromDate).page(page).pageSize(pageSize)
            .sourceApp(sourceApp).application(application).toDate(toDate).createdBy(createdBy)
            .targetUserName(targetUserName).targetUserId(targetUserId).build();

    return new ResponseEntity<>(
        auditService.getAuditInfo(fetchAuditEventRequest),
        HttpStatus.OK);
  }

}
