// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.junit.Assert.assertNotNull;

import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.utils.AuditUserEventUtil;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AuditUserEventUtilTest {

  @InjectMocks
  private AuditUserEventUtil auditUserEventUtil;

  @Test
  public void testConstructAuditUserEventDto() {
    assertNotNull(auditUserEventUtil
        .constructAuditUserEventDto(UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE,
            UserStatus.ACTIVE, false), AuditEventStatus.SUCCESS));
  }

  @Test
  public void testConstructCreateUserAuditResourceDtos() {
    assertNotNull(
        auditUserEventUtil.constructCreateUserAuditResourceDtos(UserTestUtility.getEditUserAuditPrevAccessResources()));
  }

  @Test
  public void testConstructEditUserAuditResourceDtos() {
    assertNotNull(
        auditUserEventUtil.constructEditUserAuditResourceDtos(UserTestUtility.getEditUserAuditPrevAccessResources(),
            UserTestUtility.getEditUserAuditLatestAccessResources()));
  }

  @Test
  public void testConstructEditUserAuditResourceDtos_withEmptyAccessResources() {
    assertNotNull(
        auditUserEventUtil.constructEditUserAuditResourceDtos(new ArrayList<AccessResources>(),
            UserTestUtility.getEditUserAuditLatestAccessResources()));
  }

}
