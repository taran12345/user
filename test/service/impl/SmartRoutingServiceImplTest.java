// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.feignclients.SmartRoutingFeignClient;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.AccountGroupsV2Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SmartRoutingServiceImplTest {

  @InjectMocks
  private SmartRoutingServiceImpl smartRoutingServiceImpl;

  @Mock
  private SmartRoutingFeignClient smartRoutingFeignClient;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getSingleApiAccountIdsByPmleId_withValidPmleInput_shouldSucceed() {
    AccountGroupsV2Resource accountGroupsV2Resource = UserTestUtility.getAccountGroupsV2Resource();
    when(smartRoutingFeignClient.getAccountGroupsV2(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(accountGroupsV2Resource);
    List<String> singleApiAccountIds = smartRoutingServiceImpl.getSingleApiAccountIdsByPmleId("12345");
    assertThat(singleApiAccountIds.size(), is(2));
    assertThat(singleApiAccountIds.get(0), is("12345_USD"));
  }

  @Test(expected = BadRequestException.class)
  public void getSingleApiAccountIdsByPmleId_withNullPmle_throwsException() {
    AccountGroupsV2Resource accountGroupsV2Resource = UserTestUtility.getAccountGroupsV2Resource();
    when(smartRoutingFeignClient.getAccountGroupsV2(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(accountGroupsV2Resource);
    smartRoutingServiceImpl.getSingleApiAccountIdsByPmleId(null);
  }
}
