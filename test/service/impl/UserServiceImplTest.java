// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingKey;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PermissionServiceClient;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.repository.HashedPasswordRepository;
import com.paysafe.upf.user.provisioning.repository.SkrillTellerUserSpecification;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UserSpecification;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.repository.UsersSummaryRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AccessGroupService;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.FeatureFlagService;
import com.paysafe.upf.user.provisioning.service.MailService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.AuditUserEventUtil;
import com.paysafe.upf.user.provisioning.utils.PasswordEncryptionUtil;
import com.paysafe.upf.user.provisioning.utils.UserCreationUtil;
import com.paysafe.upf.user.provisioning.utils.UserFilterUtil;
import com.paysafe.upf.user.provisioning.utils.UserPasswordManagementUtil;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomRoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserPasswordMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUpdateUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.HashedPassword;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserServiceImplTest {
  @InjectMocks
  UserServiceImpl userServiceImpl;

  @Mock
  PegasusFeignClient pegasusFeignClient;

  @Mock
  private FeatureFlagService featureFlagService;

  @Mock
  UserAssembler userAssembler;

  @Mock
  IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private AccessGroupFeignClient mockAccessGroupFeignClient;

  @Mock
  private PermissionServiceClient mockPermissionServiceClient;

  @Mock
  private UsersRepository mockUsersRepository;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private AccessGroupService accessGroupService;

  @Mock
  private UserProvisioningUtils userProvisioningUtils;

  @Mock
  private UserPasswordManagementUtil userPasswordManagementUtil;

  @Mock
  private AuditUserEventUtil auditUserEventUtil;

  @Mock
  private TokenService tokenService;

  @Mock
  private MailService mailService;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private AuditUserEventRepository auditUserEventRepository;

  @InjectMocks
  private UserServiceImpl userServiceImplUnderTest;

  @Mock
  private UserSpecification userSpecification;

  @Mock
  private UserFilterUtil userFilterUtil;

  @Mock
  private UsersSummaryRepository usersSummaryRepository;

  @Mock
  private AuditService auditService;

  @Mock
  private PasswordEncryptionUtil mockPasswordEncryptionUtil;

  @Mock
  private HashedPasswordRepository hashedPasswordRepository;

  @Mock
  private SkrillTellerUserSpecification skrillTellerUserSpecification;

  @Mock
  private UserProvisioningConfig userProvisioningConfig;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @Mock
  private SkrillTellerUserService skrillTellerUserService;

  @Mock
  private UserCreationUtil userCreationUtil;

  @Mock
  private OktaAppConfig oktaAppConfig;

  private IdentityManagementUserResource identityManagementUserResource;
  private PegasusUserListResponseResource pegasusUserListResponseResource;
  private UserDto userDto;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
    pegasusUserListResponseResource = UserTestUtility.getPegasusUserListResponseResource();
    userDto = UserTestUtility.getUserDto();
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    doNothing().when(userPasswordManagementUtil).updatePasswordInPegasus(any(), any(), any());
    doNothing().when(userPasswordManagementUtil).updatePasswordInOkta(any(), any());
    HashMap<String, Boolean> features = new HashMap<>();
    features.put("ST_DeleteReports", true);
    when(featureFlagService.fetchFeatureFlag()).thenReturn(features);
  }

  private UserConfig getUserConfig() {
    UserConfig userConfig = new UserConfig();
    userConfig.setRegistationTokenTimeToLiveSeconds(60 * 60 * 24 * 7L);
    return userConfig;
  }

  private UserProvisioningConfig getUserProvisioningConfig() {
    UserProvisioningConfig userProvisioningConfig = new UserProvisioningConfig();
    userProvisioningConfig.setMailNotificationsEnabled(true);
    return userProvisioningConfig;
  }

  private void mockGetUsersByFiltersApiCalls() {
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    UserSpecification userSpecificationCreated = new UserSpecification();
    Specification<User> userSpec =
        userSpecificationCreated.constructPortalUsersSpecification(UserFetchByFiltersRequestDto.builder().build());
    PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("lastModifiedDate").descending());
    when(userSpecification.constructPortalUsersSpecification(any())).thenReturn(userSpec);
    when(usersRepository.findAll(userSpec, pageRequest))
        .thenReturn(new PageImpl<>(Arrays.asList(UserTestUtility.getUser())));
    doNothing().when(userFilterUtil).getUsersByFilters(any(), anyBoolean());
    when(userProvisioningUtils.getUserByLoginName(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    when(skrillTellerUserSpecification.constructFetchSkrillTellerUsersSpecification(any())).thenReturn(userSpec);
  }

  @Test
  public void testMigrateUser() {
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    pegasusUserListResponseResource.getUsers().get(0).setType("EXT");
    IdentityManagementUserListResource identityManagementUserListResource = new IdentityManagementUserListResource();
    identityManagementUserListResource.setUsers(Arrays.asList(identityManagementUserResource));
    when(identityManagementFeignClient.getUsersByUserName(any())).thenReturn(identityManagementUserListResource);
    Mockito.doReturn(userDto).when(userAssembler).toUserDto(pegasusUserListResponseResource.getUsers().get(0));
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    doNothing().when(pegasusFeignClient).updateUser(any(), anyString());
    UserPasswordMigrationDto userPasswordMigrationDto = new UserPasswordMigrationDto();
    userPasswordMigrationDto.setPassword("password");
    IdentityManagementUserResource userResource =
        userServiceImpl.migrateUser("TEST_USER", userPasswordMigrationDto, "PORTAL");
    Assert.assertEquals(userResource, identityManagementUserResource);
  }

  @Test(expected = InternalErrorException.class)
  public void testMigrateUserofTypeInt() {
    ReflectionTestUtils.setField(userServiceImpl, "shouldDoCompleteUserMigration", true);
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    pegasusUserListResponseResource.getUsers().get(0).setType("INT");
    when(identityManagementFeignClient.getUser(any(), anyString())).thenReturn(null);
    Mockito.doReturn(userDto).when(userAssembler).toUserDto(pegasusUserListResponseResource.getUsers().get(0));
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    doNothing().when(pegasusFeignClient).updateUser(any(), anyString());
    UserPasswordMigrationDto userPasswordMigrationDto = new UserPasswordMigrationDto();
    userPasswordMigrationDto.setPassword("password");
    IdentityManagementUserResource userResource =
        userServiceImpl.migrateUser("TEST_USER", userPasswordMigrationDto, "PORTAL");
  }

  @Test
  public void testMigrateUserofTypeExt() {
    ReflectionTestUtils.setField(userServiceImpl, "shouldDoCompleteUserMigration", true);
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    pegasusUserListResponseResource.getUsers().get(0).setType("EXT");
    when(identityManagementFeignClient.getUser(any(), anyString())).thenReturn(null);
    Mockito.doReturn(userDto).when(userAssembler).toUserDto(pegasusUserListResponseResource.getUsers().get(0));
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    doNothing().when(pegasusFeignClient).updateUser(any(), anyString());
    UserPasswordMigrationDto userPasswordMigrationDto = new UserPasswordMigrationDto();
    userPasswordMigrationDto.setPassword("password");
    IdentityManagementUserResource userResource =
        userServiceImpl.migrateUser("TEST_USER", userPasswordMigrationDto, "PORTAL");
    verify(identityManagementFeignClient, Mockito.times(1)).createUser(any());
  }

  @Test(expected = InternalErrorException.class)
  public void testCreateUser_withException() throws JsonProcessingException {
    final UserDto userDto = UserTestUtility.getUserDto();
    userDto.getRoleDto().getExistingRoles().clear();
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.getPermissionsForRolesAndCategories(Arrays.asList("value"), false))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(new CustomRoleDto("roleName")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.createAccessGroup(new CustomAccessGroupDto()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(identityManagementFeignClient.createUser(any(IdentityManagementUserResource.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    doNothing().when(userProvisioningUtils).setOwnerInfo(any(UserDto.class));
    final IdentityManagementUserResource result = userServiceImplUnderTest.createUser(userDto);
  }

  @Test
  public void testCreateUser() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    final UserDto userDto = UserTestUtility.getUserDto();
    when(userProvisioningConfig.isMailNotificationsEnabled()).thenReturn(true);
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(new ArrayList<String>() {
      {
        add("BP_TEST_ROLE_1");
      }
    }, HttpStatus.CONTINUE));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mockPermissionServiceClient.getPermissionsForRolesAndCategories(Arrays.asList("value"), false))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(new CustomRoleDto("roleName")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.createAccessGroup(new CustomAccessGroupDto()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.CREATED));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    doNothing().when(userProvisioningUtils).setOwnerInfo(any(UserDto.class));
    final IdentityManagementUserResource result = userServiceImplUnderTest.createUser(userDto);
    verify(identityManagementFeignClient, Mockito.times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void testCreateUserWithIdmException() throws JsonProcessingException {
    final UserDto userDto = UserTestUtility.getUserDto();
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(new ArrayList<String>() {
      {
        add("BP_TEST_ROLE_1");
      }
    }, HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.getPermissionsForRolesAndCategories(Arrays.asList("value"), false))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(new CustomRoleDto("roleName")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.createAccessGroup(new CustomAccessGroupDto()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(identityManagementFeignClient.createUser(any())).thenThrow(BadRequestException.class);
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    doNothing().when(userProvisioningUtils).setOwnerInfo(any(UserDto.class));
    final IdentityManagementUserResource result = userServiceImplUnderTest.createUser(userDto);
  }

  @Test
  public void createUser_withAuthorization_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    final UserDto userDto = UserTestUtility.getUserDto();
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(new ArrayList<String>() {
      {
        add("BP_TEST_ROLE_1");
      }
    }, HttpStatus.CONTINUE));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mockPermissionServiceClient.getPermissionsForRolesAndCategories(Arrays.asList("value"), false))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(new CustomRoleDto("roleName")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.createAccessGroup(new CustomAccessGroupDto()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.CREATED));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    doNothing().when(userProvisioningUtils).setOwnerInfo(any(UserDto.class));
    final IdentityManagementUserResource result = userServiceImplUnderTest.createUser(userDto);
    verify(identityManagementFeignClient, Mockito.times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testCreateNetellerUser() throws JsonProcessingException {
    final UserDto userDto = UserTestUtility.getUserDto();
    userDto.setApplicationName("NETELLER");
    userDto.setHashedPassword(new HashedPassword());
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(new ArrayList<String>() {
      {
        add("BP_TEST_ROLE_1");
      }
    }, HttpStatus.CONTINUE));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mockPermissionServiceClient.getPermissionsForRolesAndCategories(Arrays.asList("value"), false))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(new CustomRoleDto("roleName")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.createAccessGroup(new CustomAccessGroupDto()))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.CREATED));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    doNothing().when(userProvisioningUtils).setOwnerInfo(any(UserDto.class));
    final IdentityManagementUserResource result = userServiceImplUnderTest.createUser(userDto);
    verify(identityManagementFeignClient, Mockito.times(1)).createUser(any());
  }

  @Test
  public void testCreateUserWithPortalUser() throws JsonProcessingException {
    final UserDto userDto = UserTestUtility.getUserDto();
    userDto.setApplicationName(DataConstants.PORTAL);
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(new ArrayList<String>() {
      {
        add("BP_TEST_ROLE_1");
      }
    }, HttpStatus.CONTINUE));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mockPermissionServiceClient.getPermissionsForRolesAndCategories(Collections.singletonList("value"), false))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(new CustomRoleDto("roleName")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    when(accessGroupService.createAccessGroupsFromResouresList(any(UserDto.class))).thenReturn(new ArrayList<>());
    when(identityManagementFeignClient.createUser(any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.CREATED));
    doNothing().when(userProvisioningUtils).setOwnerInfo(any(UserDto.class));
    final IdentityManagementUserResource result = userServiceImplUnderTest.createUser(userDto);
    verify(identityManagementFeignClient, Mockito.times(1)).createUser(any());
  }

  @Test
  public void testUpdateUser() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setUserSummary(UserTestUtility.getUserSummary());
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(userProvisioningUtils.checkIfDeleteReportEnabled(any())).thenReturn(true);
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateUserWhenExistingRoleIsInvalid() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getRolesToAdd().setExistingRoles(UserTestUtility.getInvalidRoles());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    Mockito.doThrow(BadRequestException.class).when(userProvisioningUtils).validateExistingRoles(any(), any());
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    doThrow(BadRequestException.class).when(userCreationUtil).validateUpdateUserRequest(any());
    userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    verify(identityManagementFeignClient, times(0)).internalLogout(any());
  }

  @Test
  public void testUpdateUserWithEmptyCustomAccessGroups() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getAccessGroupsToAdd().setCustomAccessGroupDtos(new ArrayList<>());
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testUpdateUserWithEmptyCustomRoles() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getRolesToAdd().setCustomRoles(new ArrayList<>());
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testUpdateUserWhenCustomAccessGroupAlreadyCreated() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getAccessGroupsToAdd().getCustomAccessGroupDtos().get(0).setName("CUSTOM_ACCESS_GROUP5");
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.ADMIN, DataConstants.WALLETS));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateUserWhenCustomAccessGroupAlreadyCreatedAndWithDiffAccessPolicies()
      throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getAccessGroupsToAdd().getCustomAccessGroupDtos().get(0).setName("CUSTOM_ACCESS_GROUP5");
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    AccessGroupResponseResource accessGroupResponseResource = UserTestUtility.getAccessGroupResponseResource();
    accessGroupResponseResource.getAccessGroupPolicies().get(0).getAcessPolicy().setCode("INVALID_ACCESS_POLICY_ID");
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(accessGroupResponseResource, HttpStatus.CONTINUE));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    doThrow(BadRequestException.class).when(userCreationUtil).validateUpdateUserRequest(any());
    userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    verify(identityManagementFeignClient, times(0)).internalLogout(any());
  }

  @Test(expected = InternalErrorException.class)
  public void testUpdateUserWhenGetRoleNamesReturnsNull() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getRolesToAdd().setCustomRoles(new ArrayList<>());
    when(mockPermissionServiceClient.getRoleNames()).thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    doThrow(InternalErrorException.class).when(userCreationUtil).validateUpdateUserRequest(any());
    userServiceImplUnderTest.updateUser("userId", userUpdationDto);
  }

  @Test(expected = InternalErrorException.class)
  public void testUpdateUserWhenIdentityUpdateFails() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    userServiceImplUnderTest.updateUser("userId", userUpdationDto);
  }

  @Test
  public void testUpdateUserWhenAccessGroupInfoAlreadyPresentInDatabase() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.ADMIN, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(new UserAccessGroupMappingDao()));
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
  }

  @Test
  public void testUpdateUser_withAccessGroupsHardDeleteList() throws JsonProcessingException {
    final UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setAccessGroupsToHardDelete(new ArrayList<>(Arrays.asList("123", "456")));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getIdentityManagementUserResource(), HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
  }

  @Test(expected = InternalErrorException.class)
  public void testFetchUser_withException() {
    when(identityManagementFeignClient.getUser("userId", null)).thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    final IdentityManagementUserResource result = userServiceImplUnderTest.fetchUser("userId");
  }

  @Test
  public void testValidateAccessGroups() {
    final AccessGroupDto accessGroupDto = new AccessGroupDto();

    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Arrays.asList("value")))
        .thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));
    userServiceImplUnderTest.validateAccessGroups(accessGroupDto);
  }

  @Test
  public void testChangePassword() throws JsonProcessingException {
    when(identityManagementFeignClient.changePassword(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    PegasusUserListResponseResource responseResource = new PegasusUserListResponseResource();
    List<PegasusUserResponseResource> userResponseResourceList = new ArrayList<>();
    userResponseResourceList.add(new PegasusUserResponseResource());
    responseResource.setUsers(userResponseResourceList);
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(responseResource);
    ChangePasswordRequestResource changePasswordRequestResource = new ChangePasswordRequestResource();
    changePasswordRequestResource.setPassword("abcd");
    changePasswordRequestResource.setNewPassword("efgh");
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    userServiceImpl.changePassword("testUserId", changePasswordRequestResource);
    verify(identityManagementFeignClient, Mockito.times(1)).getUser(any(), any());
  }

  @Test
  public void testChangePassword_with_no_Pegasus_user() throws JsonProcessingException {
    when(identityManagementFeignClient.changePassword(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    PegasusUserListResponseResource responseResource = new PegasusUserListResponseResource();
    responseResource.setUsers(new ArrayList<>());
    responseResource.setCount(0L);
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(responseResource);
    ChangePasswordRequestResource changePasswordRequestResource = new ChangePasswordRequestResource();
    changePasswordRequestResource.setPassword("abcd");
    changePasswordRequestResource.setNewPassword("efgh");
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    userServiceImpl.changePassword("testUserId", changePasswordRequestResource);
    verify(identityManagementFeignClient, Mockito.times(1)).getUser(any(), any());
  }

  @Test
  public void testResetPassword() throws JsonProcessingException {
    when(identityManagementFeignClient.updateUser(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    PegasusUserListResponseResource responseResource = new PegasusUserListResponseResource();
    List<PegasusUserResponseResource> userResponseResourceList = new ArrayList<>();
    userResponseResourceList.add(new PegasusUserResponseResource());
    responseResource.setUsers(userResponseResourceList);
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(responseResource);
    ResetPasswordRequestResource resetPasswordRequestResource = new ResetPasswordRequestResource();
    resetPasswordRequestResource.setNewPassword("abcd");
    resetPasswordRequestResource.setValidationToken("validToken");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    userServiceImpl.resetPassword("testUserId", resetPasswordRequestResource, "PORTAL");
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(any(), any());
  }

  @Test
  public void testGetUser_withFilters() {
    UserSpecification userSpecificationCreated = new UserSpecification();
    Specification<User> userSpec =
        userSpecificationCreated.constructPortalUsersSpecification(UserFetchByFiltersRequestDto.builder().build());
    PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("lastModifiedDate").descending());
    Page<User> usersPage = null;

    when(userSpecification.constructPortalUsersSpecification(any())).thenReturn(userSpec);
    when(usersRepository.findAll(userSpec, pageRequest)).thenReturn(usersPage);
    doNothing().when(userFilterUtil).getUsersByFilters(any(), anyBoolean());
    userServiceImpl.getUsersByFilters("PORTAL", "userIdentifier", UserStatusFilter.BLOCKED, "role", null, "createdBy",
        null, ResourceType.PMLE, "resourceId", null, new Integer(0), new Integer(20), false, false);
    verify(userFilterUtil, Mockito.times(1)).getUsersByFilters(any(), anyBoolean());
  }

  @Test
  public void updateUserStatus_withValidData_shoulSucceed() throws JsonProcessingException {
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    doNothing().when(userProvisioningUtils).updateUserStatus(any(), any());
    UpdateUserStatusResource resource = UserTestUtility.getUpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    userServiceImpl.updateUserStatus("1234", resource);
    verify(userProvisioningUtils).updateUserStatus(any(), any());
    verify(identityManagementFeignClient, times(1)).getUser(anyString(), any());
    verify(identityManagementFeignClient, times(1)).internalLogout(any());
  }

  @Test
  public void addupdateMfaStatus() {
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.MFA_GROUP, "mfa_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    doNothing().when(usersRepository).updateMfaStatus(anyString(), anyString());
    when(identityManagementFeignClient.addUserToGroup(anyString(), anyString()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    userServiceImpl.updateMfaStatus(Arrays.asList("1234"), true);
    verify(identityManagementFeignClient, times(1)).addUserToGroup("mfa_group", "1234");
  }

  @Test(expected = BadRequestException.class)
  public void addupdateMfaStatusWithIdmException() throws JsonProcessingException {
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.MFA_GROUP, "mfa_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    doNothing().when(usersRepository).updateMfaStatus(anyString(), anyString());
    when(identityManagementFeignClient.addUserToGroup(anyString(), anyString())).thenThrow(BadRequestException.class);
    userServiceImpl.updateMfaStatus(Arrays.asList("1234"), true);
  }

  @Test
  public void removeupdateMfaStatus() {
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.MFA_GROUP, "mfa_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    doNothing().when(usersRepository).updateMfaStatus(anyString(), anyString());
    when(identityManagementFeignClient.resetFactor(anyString(), anyString()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(identityManagementFeignClient.removeUserFromGroup(anyString(), anyString()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    userServiceImpl.updateMfaStatus(Arrays.asList("1234"), false);
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup("mfa_group", "1234");
  }

  @Test(expected = BadRequestException.class)
  public void removeupdateMfaStatusWithIdmException() throws JsonProcessingException {
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.MFA_GROUP, "mfa_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    doNothing().when(usersRepository).updateMfaStatus(anyString(), anyString());
    when(identityManagementFeignClient.resetFactor(anyString(), anyString()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(identityManagementFeignClient.removeUserFromGroup(anyString(), anyString()))
        .thenThrow(BadRequestException.class);
    userServiceImpl.updateMfaStatus(Arrays.asList("1234"), false);
  }

  @Test
  public void updateUserStatus_withActivateStatus_shouldNotLogout() throws JsonProcessingException {
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    doNothing().when(userProvisioningUtils).updateUserStatus(any(), any());
    UpdateUserStatusResource resource = UserTestUtility.getUpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVATE);
    userServiceImpl.updateUserStatus("1234", resource);
    verify(userProvisioningUtils).updateUserStatus(any(), any());
    verify(identityManagementFeignClient, times(0)).internalLogout(any());
  }

  @Test
  public void sendUserActivationEmail_withValidData_shouldSucceed() throws JsonProcessingException {
    IdentityManagementUserResource idmUserResource = UserTestUtility.getIdentityManagementUserResource();
    UserProvisioningUserResource userResource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(idmUserResource, userResource);
    userResource.setStatus(UserStatus.PROVISIONED);
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<IdentityManagementUserResource>(userResource, HttpStatus.OK));
    when(usersSummaryRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getUsersSummary()));
    doNothing().when(userProvisioningUtils).populateAccessResources(any());
    doNothing().when(mailService).sendRegistrationConfirmationEmail(userResource);
    userServiceImpl.sendUserActivationEmail("1234");
    verify(usersSummaryRepository, Mockito.times(1)).findById(any());
  }

  @Test(expected = BadRequestException.class)
  public void sendUserActivationEmail_withInvalidUserStatus_shouldThrowException() throws JsonProcessingException {
    IdentityManagementUserResource idmUserResource = UserTestUtility.getIdentityManagementUserResource();
    UserProvisioningUserResource userResource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(idmUserResource, userResource);
    userResource.setStatus(UserStatus.DEACTIVATED);
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<IdentityManagementUserResource>(userResource, HttpStatus.OK));
    when(usersRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getUser()));
    userServiceImpl.sendUserActivationEmail("789");
    verify(usersRepository, Mockito.times(1)).findById(any());
  }

  @Test
  public void getUserAccessGroupIds_withValidInput_shouldSucceed() {
    when(userAccessGroupMapppingRepository.getUserAccessGroupIds(any(), any(), any()))
        .thenReturn(UserTestUtility.getAccessGroups());
    userServiceImpl.getUserAccessGroupIds("testuser@paysafe.com", "SKRILL");
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).getUserAccessGroupIds(any(), any(), any());
  }

  @Test
  public void testGetUser() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(userProvisioningUtils.getUserByLoginName(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    List<UserAccessGroupMappingDao> dao = new ArrayList<>();
    dao.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginNameAndResourceTypeAndResourceId(any(), any(), any()))
        .thenReturn(dao);
    userServiceImpl.getUsers("loginName", "testResourceType", "testResourceId", "testQuery", "testOwnerType",
        "testOwnerId", "testApplication", 3, 9, new MutableBoolean(false));
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).findByLoginNameAndResourceTypeAndResourceId(any(),
        any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testGetUserWithOwnerTypeWithoutQuery() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    Page<User> users = Mockito.mock(Page.class);
    when(usersRepository.findByOwnerTypeAndOwnerIdAndApplicationAndLoginNameNotOrderByLastModifiedDateDesc(any(), any(),
        any(), any(), any())).thenReturn(users);
    userServiceImpl.getUsers(null, null, null, null, "testOwnerType", "testOwnerId", "testApplication", 3, 9,
        new MutableBoolean(false));
    verify(usersRepository, Mockito.times(1))
        .findByOwnerTypeAndOwnerIdAndApplicationAndLoginNameNotOrderByLastModifiedDateDesc(any(),
            any(), any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testGetUserWithOwnerTypeWithQuery() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    Page<User> users = Mockito.mock(Page.class);
    when(usersRepository.smartSearchUsers(any(), any(), any(), any(), any(), any())).thenReturn(users);
    userServiceImpl.getUsers(null, null, null, "query", "testOwnerType", "testOwnerId", "testApplication", 3, 9,
        new MutableBoolean(false));
    verify(usersRepository, Mockito.times(1)).smartSearchUsers(any(),
        any(), any(), any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testGetUserWithApplicationType() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    Page<User> users = Mockito.mock(Page.class);
    when(usersRepository.findByApplicationOrderByLastModifiedDateDesc(any(), any())).thenReturn(users);
    userServiceImpl.getUsers(null, null, null, null, null, null, "testApplication", 0, 9, new MutableBoolean(false));
    verify(usersRepository, Mockito.times(1)).findByApplicationOrderByLastModifiedDateDesc(any(),
        any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void getUsers_withPartnerPortalApplication_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    when(userProvisioningUtils.getUserByLoginName(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    List<UserAccessGroupMappingDao> dao = new ArrayList<>();
    dao.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginNameAndResourceTypeAndResourceId(any(), any(), any()))
        .thenReturn(dao);
    userServiceImpl.getUsers("loginName", "testResourceType", "testResourceId", "testQuery", "testOwnerType",
        "testOwnerId", "testApplication", 3, 9, new MutableBoolean(false));
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).findByLoginNameAndResourceTypeAndResourceId(any(),
        any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testUpdateUserForPendingDifferentEmail() throws JsonProcessingException {
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setStatus(UserStatus.PROVISIONED);
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(userProvisioningConfig.isMailNotificationsEnabled()).thenReturn(true);
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    IdentityManagementUserResource identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
    identityManagementUserResource.setStatus(UserStatus.PROVISIONED);
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    mockGetUsersByFiltersApiCalls();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    UserProvisioningUserResource resource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(userResponse, resource);
    userResource.setStatus(UserStatus.PROVISIONED);
    doNothing().when(mailService).sendRegistrationConfirmationEmail(resource);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
  }

  @Test
  public void testUpdateUserForActiveDifferentEmail() throws JsonProcessingException {
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(userProvisioningConfig.isMailNotificationsEnabled()).thenReturn(true);
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    IdentityManagementUserResource identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
    identityManagementUserResource.setStatus(UserStatus.ACTIVE);
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    mockGetUsersByFiltersApiCalls();
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    UserProvisioningUserResource resource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(userResponse, resource);
    userResource.setStatus(UserStatus.PROVISIONED);
    doNothing().when(mailService).sendRegistrationConfirmationEmail(resource);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
  }

  @Test
  public void testUpdateUserWhenEmailIsNotEnabled() throws JsonProcessingException {
    userProvisioningConfig.setMailNotificationsEnabled(false);
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    IdentityManagementUserResource identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
    identityManagementUserResource.setStatus(UserStatus.ACTIVE);
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    mockGetUsersByFiltersApiCalls();
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    UserProvisioningUserResource resource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(userResponse, resource);
    userResource.setStatus(UserStatus.PROVISIONED);
    doNothing().when(mailService).sendRegistrationConfirmationEmail(resource);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
  }

  @Test
  public void testUpdateUserWhenEmailIsSame() throws JsonProcessingException {
    when(userProvisioningUtils.getIdmUsers(anyList(), any()))
        .thenReturn(UserTestUtility.getUserResponseResourceList());
    when(userProvisioningUtils.getAccessResourcesFromDao(anyList()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(userProvisioningConfig.isMailNotificationsEnabled()).thenReturn(true);
    when(mockPermissionServiceClient.getRoleNames())
        .thenReturn(new ResponseEntity<>(UserTestUtility.getRoles(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupsPresentFromInputList(Mockito.anyList()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroups(), HttpStatus.CONTINUE));
    when(mockAccessGroupFeignClient.getAccessGroupNameAvailabilityList(Mockito.anyList())).thenReturn(
        new ResponseEntity<>(UserTestUtility.getAccessGroupNameAvailabilityResponse(), HttpStatus.CONTINUE));
    when(mockPermissionServiceClient.createRole(any(CustomRoleDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.OK));
    when(mockAccessGroupFeignClient.fetchAccessGroupByCode(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    when(userAssembler.toAccessResources(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getAccessResources(DataConstants.REGULAR, DataConstants.WALLETS));
    when(userAssembler.toUserAccessGroupMappingDao(any(AccessResources.class),
        any(IdentityManagementUserResource.class))).thenReturn(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(mockAccessGroupFeignClient.fetchAccessGroupByName(anyString()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getAccessGroupResponseResource(), HttpStatus.CONTINUE));
    IdentityManagementUserResource identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
    identityManagementUserResource.setStatus(UserStatus.ACTIVE);
    when(identityManagementFeignClient.updateUser(anyString(), any(IdentityManagementUpdateUserResource.class)))
        .thenReturn(new ResponseEntity<>(identityManagementUserResource, HttpStatus.OK));
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(identityManagementFeignClient.internalLogout(Mockito.any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockGetUsersByFiltersApiCalls();
    User user = UserTestUtility.getUser();
    user.setEmail("test@xyz.com");
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(user);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    IdentityManagementUserResource userResponse = userServiceImplUnderTest.updateUser("userId", userUpdationDto);
    UserProvisioningUserResource resource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(userResponse, resource);
    userResource.setStatus(UserStatus.PROVISIONED);
    doNothing().when(mailService).sendRegistrationConfirmationEmail(resource);
    assertNotNull(userResponse);
    assertThat(userResponse.getUserName(), Is.is("TEST_USER"));
    assertThat(userResponse.getEmail(), Is.is("test@xyz.com"));
    verify(identityManagementFeignClient, Mockito.times(1)).updateUser(anyString(),
        any(IdentityManagementUpdateUserResource.class));
  }

  @Test
  public void testDownloadUserEmails() {
    when(usersRepository.getEmailsByApplication(anyString()))
        .thenReturn(Arrays.asList("abc@paysafe.com", "xyz@paysafe.com"));
    ResponseEntity<ByteArrayResource> downloadUserEmails = userServiceImpl.downloadUserEmails("SKRILL");
    assertNotNull(downloadUserEmails.getBody());
  }

  @Test
  public void testResetFactor() {
    when(identityManagementFeignClient.resetFactor(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    userServiceImpl.resetFactor("abc", "PORTAL");
    verify(identityManagementFeignClient, times(1)).resetFactor(any(), any());
  }

  @Test(expected = BadRequestException.class)
  public void testResetFactorWithIdmException() throws JsonProcessingException {
    when(identityManagementFeignClient.resetFactor(any(), any())).thenThrow(BadRequestException.class);
    userServiceImpl.resetFactor("abc", "PORTAL");
  }
}
