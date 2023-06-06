// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.assembler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserAssemblerTest {

  @InjectMocks
  private UserAssembler userAssembler;

  @Test
  public void toUserMigrationResponseResourceTest() {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    UserMigrationResponseResource userMigrationResponseResource =
        userAssembler.toUserMigrationResponseResource(userProvisioningUserResource);
    assertNotNull(userMigrationResponseResource.getAccessResources());
    assertThat(userMigrationResponseResource.getUserName(), Is.is("test_user"));
  }

  @Test
  public void toSkrillUserMigrationResponseResourceTest() {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    UserMigrationResponseResource userMigrationResponseResource = userAssembler.toSkrillUserMigrationResponseResource(
        userProvisioningUserResource, UserTestUtility.getSkrillTellerMigrationDto());
    assertNotNull(userMigrationResponseResource.getAccessResources());
    assertThat(userMigrationResponseResource.getUserName(), Is.is("test_user"));
  }

  @Test
  public void toUserAccessGroupMappingDao_withValidData_shouldSucceed() {
    AccessResources accessResource = UserTestUtility.getAccessResources(DataConstants.ADMIN, DataConstants.WALLETS);
    IdentityManagementUserResource userResponse = UserTestUtility.getIdentityManagementUserResource();
    UserAccessGroupMappingDao userAccessGroupDao =
        userAssembler.toUserAccessGroupMappingDao(accessResource, userResponse);
    assertNotNull(userAccessGroupDao);
    assertThat(userAccessGroupDao.getLoginName(), Is.is("TEST_USER"));
  }

  @Test
  public void toUserAccessGroupMappingDao_withNullOwnerFields_shouldSucceed() {
    AccessResources accessResource = UserTestUtility.getAccessResources(DataConstants.ADMIN, DataConstants.WALLETS);
    accessResource.setOwnerId(null);
    accessResource.setOwnerType(null);
    IdentityManagementUserResource userResponse = UserTestUtility.getIdentityManagementUserResource();
    UserAccessGroupMappingDao userAccessGroupDao =
        userAssembler.toUserAccessGroupMappingDao(accessResource, userResponse);
    assertNotNull(userAccessGroupDao);
    assertThat(userAccessGroupDao.getUserFirstName(), Is.is("first"));
  }

  @Test
  public void toUserAccessGroupMappingDao_withNullFields_shouldReturnNull() {
    UserAccessGroupMappingDao userAccessGroupDao = userAssembler.toUserAccessGroupMappingDao(null, null);
    assertNull(userAccessGroupDao);
  }

  @Test
  public void toAuditUserEvent_withoutRequestEntity_shouldSucceed() throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json =
        objectMapper.treeToValue(UserTestUtility.getOktaEventHookResponse(true), OktaEventHookResource.class);
    json.getData().getEvents().get(0).setRequest(null);
    AuditUserEvent auditUserEvent = userAssembler.toAuditUserEvent(json.getData().getEvents().get(0), "NETELLER");
    assertNotNull(auditUserEvent);
    assertThat(auditUserEvent.getUserIpAddress(), Is.is("38.74.6.20"));
  }

  @Test
  public void toAuditUserEvent_withRequestEntity_shouldSucceed() throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json =
        objectMapper.treeToValue(UserTestUtility.getOktaEventHookResponse(true), OktaEventHookResource.class);
    AuditUserEvent auditUserEvent = userAssembler.toAuditUserEvent(json.getData().getEvents().get(0), "NETELLER");
    assertNotNull(auditUserEvent);
    assertThat(auditUserEvent.getUserIpAddress(), Is.is("38.74.6.20"));
  }

  @Test
  public void toAuditUserEvent_withSessionStartFailedRequestEntity_shouldSucceed()
      throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json = objectMapper.treeToValue(UserTestUtility.getOktaEventHookResponseGeneralNonSuccess(),
        OktaEventHookResource.class);
    AuditUserEvent auditUserEvent = userAssembler.toAuditUserEvent(json.getData().getEvents().get(0), "NETELLER");
    assertNotNull(auditUserEvent);
    assertThat(auditUserEvent.getUserIpAddress(), Is.is("116.50.59.180"));
  }

  @Test
  public void toAuditUserEvent_withSessionStartSucceessRequestEntityForPortal_shouldSucceed()
      throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json =
        objectMapper.treeToValue(UserTestUtility.getOktaEventHookResponseSuccess(), OktaEventHookResource.class);
    AuditUserEvent auditUserEvent =
        userAssembler.toAuditUserEvent(json.getData().getEvents().get(0), DataConstants.PORTAL);
    assertNotNull(auditUserEvent);
    assertThat(auditUserEvent.getUserIpAddress(), Is.is("116.50.59.180"));
  }

  @Test
  public void toAuditUserEvent_withSessionStartSucceessRequestEntityForSkrill_shouldSucceed()
      throws JsonProcessingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json =
        objectMapper.treeToValue(UserTestUtility.getOktaEventHookResponseSuccess(), OktaEventHookResource.class);
    AuditUserEvent auditUserEvent =
        userAssembler.toAuditUserEvent(json.getData().getEvents().get(0), DataConstants.SKRILL);
    assertNull(auditUserEvent);
  }
}
