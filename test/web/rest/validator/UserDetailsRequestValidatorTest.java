// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.validator;

import static org.junit.Assert.assertThat;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserDetailsRequestValidatorTest {
  @InjectMocks
  private UserDetailsRequestValidator userDetailsRequestValidator;

  private UserUpdationResource userUpdationResource;

  private UserResource userResource;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    userUpdationResource = new UserUpdationResource();
    List<PermissionDto> permissionDtoList = new ArrayList<>();
    permissionDtoList.add(new PermissionDto(0,"permission1",0));
    permissionDtoList.add(new PermissionDto(1,"permission1",0));
    permissionDtoList.add(new PermissionDto(1,"permission2",0));

    UpdateUserAccessResources updateUserAccessResources = new UpdateUserAccessResources();
    updateUserAccessResources.setPermissions(permissionDtoList);
    updateUserAccessResources.setId("1234");
    userUpdationResource.setAccessResources(Arrays.asList(updateUserAccessResources));

    userResource = new UserResource();
    AccessResources accessResources = new AccessResources();
    accessResources.setPermissions(permissionDtoList);
    accessResources.setId("1234");
    userResource.setAccessResources(Arrays.asList(accessResources));
  }

  @Test
  public void validateUpdateUserRequestTest() throws JsonProcessingException {
    userDetailsRequestValidator.validateUpdateUserRequest(userUpdationResource);
    assertThat(userUpdationResource.getAccessResources().get(0).getPermissions().size(), Is.is(2));
  }

  @Test
  public void valiadteCreateUserRequestTest() throws JsonProcessingException {
    userDetailsRequestValidator.valiadteCreateUserRequest(userResource);
    assertThat(userResource.getAccessResources().get(0).getPermissions().size(), Is.is(2));
  }

  @Test(expected = BadRequestException.class)
  public void validateApplicationAndResourceNameAndId_whenResourceIdEmpty_throwsException() {
    userDetailsRequestValidator.validateApplicationAndResourceNameAndId(DataConstants.PORTAL, ResourceType.FMA, null);
  }

  @Test(expected = BadRequestException.class)
  public void validateApplicationAndResourceNameAndId_withSkrillApplicationAndFmaResourceName_throwsException() {
    userDetailsRequestValidator.validateApplicationAndResourceNameAndId(DataConstants.SKRILL, ResourceType.FMA,
        "100032");
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withNullOwnerId_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest(null, DataConstants.PMLE);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withEmptyOwnerId_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("", DataConstants.PMLE);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withNullOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("1234", null);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withEmptyOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("1234", "");
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withNullOwnerIdAndOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest(null, null);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withEmptyOwnerIdAndOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("", "");
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withUndefinedOwnerId_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest(DataConstants.UNDEFINED, DataConstants.PMLE);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withUndefinedOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("1234", DataConstants.UNDEFINED);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withUndefinedOwnerIdAndOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest(DataConstants.UNDEFINED, DataConstants.UNDEFINED);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withNullStringOwnerId_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("null", DataConstants.PMLE);
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withNullStringOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("1234", "Null");
  }

  @Test(expected = BadRequestException.class)
  public void validateV1HierarchyRequest_withNullStringOwnerIdAndOwnerType_throwsException() {
    userDetailsRequestValidator.validateV1HierarchyRequest("null", "NULL");
  }

  @Test
  public void validateUpdateUserStatusRequest_withSkrillActivateStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.ACTIVATE);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withSkrillActiveAllStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.ACTIVE_ALL);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withSkrillBlockAllStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.BLOCK_ALL);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withSkrillBlockedStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.BLOCKED);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withNetellerActivateStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.ACTIVATE);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withNetellerActiveAllStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.ACTIVE_ALL);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withNetellerBlockAllStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.BLOCK_ALL);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withNetellerBlockedStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.BLOCKED);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test(expected = BadRequestException.class)
  public void validateUpdateUserStatusRequest_withSkrillDeleteStatus_throwsException() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.DELETE);
    userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource);
  }

  @Test
  public void validateUpdateUserStatusRequest_withPortalActivateStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.ACTIVATE);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }

  @Test
  public void validateUpdateUserStatusRequest_withPortalBlockedStatus_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAction(UserAction.BLOCKED);
    Assertions.assertDoesNotThrow(
        () -> userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource));
  }
}
