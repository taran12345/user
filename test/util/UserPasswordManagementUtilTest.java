// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.utils.UserPasswordManagementUtil;
import com.paysafe.upf.user.provisioning.web.rest.dto.AppUserConfigDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserPasswordManagementUtilTest {

  @InjectMocks
  private UserPasswordManagementUtil userPasswordManagementUtil;

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private PegasusFeignClient pegasusFeignClient;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private AuditService auditServicel;

  private IdentityManagementUserResource userResource;

  private PegasusUserListResponseResource responseResource;

  private List<PegasusUserResponseResource> userResponseResourceList;

  private ChangePasswordRequestResource changePasswordRequestResource;

  /**
   * Data initialization.
   */
  @Before
  public void setUp() throws Exception {
    userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    userResponseResourceList = new ArrayList<>();
    userResponseResourceList.add(new PegasusUserResponseResource());
    responseResource = new PegasusUserListResponseResource();
    responseResource.setUsers(userResponseResourceList);
    changePasswordRequestResource = new ChangePasswordRequestResource();
    changePasswordRequestResource.setPassword("test@123");
    changePasswordRequestResource.setNewPassword("test@1234");
  }


  @Test
  public void testUpdatePasswordInPegasus() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(responseResource);
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    userPasswordManagementUtil.updatePasswordInPegasus("test", "test@123", AuditEventType.FORGOT_PASSWORD);
    verify(pegasusFeignClient, Mockito.times(1)).updateUser(any(), any());
    verify(identityManagementFeignClient, Mockito.times(1)).getUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void testUpdatePasswordInPegasusShouldThrowException() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.BAD_REQUEST));
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(responseResource);
    when(usersRepository.findByUserId(Mockito.any())).thenReturn(UserTestUtility.getUser());
    userPasswordManagementUtil.updatePasswordInPegasus("test", "test@123", AuditEventType.FORGOT_PASSWORD);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testUpdatePasswordInOkta() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(identityManagementFeignClient.changePassword(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    userPasswordManagementUtil.updatePasswordInOkta("test", changePasswordRequestResource);
    verify(identityManagementFeignClient, Mockito.times(1)).changePassword(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = InternalErrorException.class)
  public void testUpdatePasswordInOktaShouldThrowException() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(identityManagementFeignClient.changePassword(any(), any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    userPasswordManagementUtil.updatePasswordInOkta("test", changePasswordRequestResource);
    CommonThreadLocal.unsetAuthLocal();
  }
}
