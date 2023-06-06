// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class AuditEventsControllerTest {

  private MockMvc mockMvc;

  private static final String AUDIT_EVENTS_ENDPOINT = "/admin/user-provisioning/v1/auditevents";

  @Mock
  private AuditService auditService;

  @InjectMocks
  @Spy
  private AuditEventsController auditEventsController;

  /**
   * Initial setup.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    final StaticApplicationContext applicationContext = new StaticApplicationContext();
    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);

    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);

    mockMvc = MockMvcBuilders.standaloneSetup(this.auditEventsController).build();
  }

  @Test
  public void fetchAuditEventsSuccessTest() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode auditEventDataObjectNode = objectMapper.createObjectNode(); 
    auditEventDataObjectNode.put("test", "test");
    
    when(auditService.getAuditInfo(any())).thenReturn(auditEventDataObjectNode);
    this.mockMvc
        .perform(MockMvcRequestBuilders.get(AUDIT_EVENTS_ENDPOINT + "?createdBy=sunil&sourceApp=BUSINESS_PORTAL")
            .contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().is2xxSuccessful());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAuditEventsSuccessTest() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode auditEventDataObjectNode = objectMapper.createObjectNode();
    auditEventDataObjectNode.put("test", "test");

    AuditUserEventDto dto =
        AuditUserEventDto.builder().application("BUSINESS_PORTAL").createdBy("testuser").application("SKRILL").build();
    final String requestObjectContent = JsonUtil.toJsonString(dto);

    when(auditService.getAuditInfo(any())).thenReturn(auditEventDataObjectNode);
    this.mockMvc
        .perform(MockMvcRequestBuilders.post(AUDIT_EVENTS_ENDPOINT).content(requestObjectContent)
            .contentType(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().is2xxSuccessful());
    CommonThreadLocal.unsetAuthLocal();
  }

}
