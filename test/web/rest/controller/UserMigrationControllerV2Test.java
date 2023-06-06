// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.migration.service.MigrationScriptService;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.resource.BulkUserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
public class UserMigrationControllerV2Test {

  private MockMvc mockMvc;

  @Spy
  private UserAssembler userAssembler;

  @Mock
  private MigrationScriptService migrationScriptService;

  @Mock
  private UserProvisioningUtils userProvisioningUtils;

  @InjectMocks
  @Spy
  private UserMigrationControllerV2 userMigrationControllerV2;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    final StaticApplicationContext applicationContext = new StaticApplicationContext();
    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);
    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);
    mockMvc = MockMvcBuilders.standaloneSetup(this.userMigrationControllerV2).build();
  }

  @Test
  public void testMigrateUserV2() throws Exception {
    UserMigrationResource userMigrationResource = new UserMigrationResource();
    userMigrationResource.setUserName("userName");
    userMigrationResource.setIsAdmin("FALSE");
    userMigrationResource.setOwnerId("ownerId");
    userMigrationResource.setOwnerType("ownerType");
    final String jsonRequestObject = JsonUtil.toJsonString(userMigrationResource);
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(UserTestUtility.getIdentityManagementUserResource(), userProvisioningUserResource);
    when(migrationScriptService.migrateSingleUser(any(), anyBoolean())).thenReturn(new UserProvisioningUserResource());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v2/migration/users").content(jsonRequestObject)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isCreated());
  }

  @Test
  public void testMigrateUserV2_forUsIgamingUser() throws Exception {
    UserMigrationResource userMigrationResource = new UserMigrationResource();
    userMigrationResource.setUserName("userName");
    userMigrationResource.setIsAdmin("FALSE");
    userMigrationResource.setOwnerId("ownerId");
    userMigrationResource.setOwnerType("ownerType");
    userMigrationResource.setUSigaming(true);
    final String jsonRequestObject = JsonUtil.toJsonString(userMigrationResource);
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(UserTestUtility.getIdentityManagementUserResource(), userProvisioningUserResource);
    when(migrationScriptService.migrateUSiUser(any(), anyBoolean())).thenReturn(new UserProvisioningUserResource());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v2/migration/users").content(jsonRequestObject)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isCreated());
  }

  @Test
  public void uploadUserMigrationDataV2() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "Header\nMigrateduser1\nMigrateduser2".getBytes());
    when(migrationScriptService.processFile(file, false, false)).thenReturn(new BulkUserMigrationResponseResource());
    this.mockMvc
        .perform(MockMvcRequestBuilders.fileUpload("/admin/user-provisioning/v2/migration/users/upload").file(file))
        .andExpect(status().isOk());
  }
}
