// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingKey;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.feignclients.MasterMerchantFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.utils.UserFilterUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserFilterUtilTest {

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private MasterMerchantFeignClient masterMerchantFeignClient;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private MasterMerchantService masterMerchantService;

  @InjectMocks
  private UserFilterUtil userFilterUtil;

  @Test
  public void getUsersByFilters_withResourceTypePmle_shouldSucceed() {
    MerchantSearchResponse response = UserTestUtility.getMerchantSearchResponse();
    response.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(response);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("1234").resourceType(DataConstants.PMLE).build(), false);
    verify(usersRepository, times(2)).getAccessGroupIds(any(), any());
    verify(userAccessGroupMapppingRepository, times(1)).getLoginName(anyList());
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getUsersByFilters_withResourceTypeMle_shouldSucceed() {
    MerchantSearchResponse response = UserTestUtility.getMerchantSearchResponse();
    response.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(response);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("1234").resourceType(DataConstants.MLE).build(), false);
    verify(usersRepository, times(3)).getAccessGroupIds(any(), any());
    verify(userAccessGroupMapppingRepository, times(1)).getLoginName(anyList());
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getUsersByFilters_withResourceTypePartner_shouldSucceed() {
    MerchantSearchResponse response = UserTestUtility.getMerchantSearchResponse();
    response.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(response);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("1234").resourceType(DataConstants.PARTNER).build(), false);
    verify(usersRepository, times(1)).getAccessGroupIds(any(), any());
    verify(userAccessGroupMapppingRepository, times(1)).getLoginName(anyList());
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getUsersByFilters_withResourceTypeAccountGroup_shouldSucceed() {
    MerchantSearchResponse response = UserTestUtility.getMerchantSearchResponse();
    response.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(response);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("ag").resourceType("ACCOUNT_GROUP").build(), false);
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getUsersByFilters_withResourceTypeFma_shouldSucceed() {
    MerchantSearchResponse response = UserTestUtility.getMerchantSearchResponse();
    response.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(response);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("1234").resourceType(DataConstants.FMA).build(), false);
    verify(usersRepository, times(4)).getAccessGroupIds(any(), any());
    verify(userAccessGroupMapppingRepository, times(1)).getLoginName(anyList());
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test(expected = BadRequestException.class)
  public void getUsersByFilters_withEmptyResourceIdsFound_throwsException() {
    MerchantSearchResponse merchantResponse = new MerchantSearchResponse();
    merchantResponse.setMerchants(new ArrayList<>());
    merchantResponse.setTotalSearchMatches(0);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(merchantResponse);
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("1234").resourceType(DataConstants.FMA).build(), false);
  }

  @Test
  public void getUsersByFilters_withResourceTypePmleEqualsSearch_shouldSucceed() {
    MerchantSearchResponse response = UserTestUtility.getMerchantSearchResponse();
    response.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(response);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    List<String> loginNames = new ArrayList<>(Arrays.asList("test_user1", "test_user2"));
    when(userAccessGroupMapppingRepository.getLoginName(anyList())).thenReturn(loginNames);
    List<String> accessGroupIds = new ArrayList<>(Arrays.asList("test_AG1", "test_AG2"));
    when(usersRepository.getAccessGroupIds(any(), any())).thenReturn(accessGroupIds);
    userFilterUtil.getUsersByFilters(
        UserFetchByFiltersRequestDto.builder().resourceId("1234").resourceType(DataConstants.PMLE).build(), true);
    verify(usersRepository, times(2)).getAccessGroupIds(any(), any());
    verify(userAccessGroupMapppingRepository, times(1)).getLoginName(anyList());
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void mapUserStatusFilter_withBlockedStatus_shouldReturnSuspended() {
    Status status = userFilterUtil.mapUserStatusFilter(UserStatusFilter.BLOCKED);
    assertNotNull(status);
    assertThat(status, Is.is(Status.SUSPENDED));
  }

  @Test
  public void mapUserStatusFilter_withPendingStatus_shouldReturnProvisioned() {
    Status status = userFilterUtil.mapUserStatusFilter(UserStatusFilter.PENDING_USER_ACTION);
    assertNotNull(status);
    assertThat(status, Is.is(Status.PROVISIONED));
  }

  @Test
  public void mapUserStatusFilter_withActiveStatus_shouldReturnActive() {
    Status status = userFilterUtil.mapUserStatusFilter(UserStatusFilter.ACTIVE);
    assertNotNull(status);
    assertThat(status, Is.is(Status.ACTIVE));
  }

  @Test
  public void getLegalEntityNameFromMasterMerchant_withPmleAccessResource_shouldSucceed() {
    when(masterMerchantService.getMerchantsSingleRecord(any())).thenReturn(UserTestUtility.getMerchantSearchResponse());
    AccessResources accessResource = UserTestUtility.getAccessResources(DataConstants.ADMIN, DataConstants.PMLE);
    String legalEntityName = userFilterUtil.getLegalEntityNameFromMasterMerchant(accessResource);
    assertNotNull(legalEntityName);
    assertThat(legalEntityName, Is.is("test_pmle"));
    verify(masterMerchantService, times(1)).getMerchantsSingleRecord(any());
  }

  @Test
  public void getLegalEntityNameFromMasterMerchant_withMleAccessResource_shouldSucceed() {
    when(masterMerchantService.getMerchantsSingleRecord(any())).thenReturn(UserTestUtility.getMerchantSearchResponse());
    AccessResources accessResource = UserTestUtility.getAccessResources(DataConstants.ADMIN, DataConstants.MLE);
    String legalEntityName = userFilterUtil.getLegalEntityNameFromMasterMerchant(accessResource);
    assertNotNull(legalEntityName);
    assertThat(legalEntityName, Is.is("test_legal_entity"));
    verify(masterMerchantService, times(1)).getMerchantsSingleRecord(any());
  }

  @Test
  public void getLegalEntityNameFromMasterMerchant_withInvalidAccessResource_shouldReturnNull() {
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.setMerchants(null);
    when(masterMerchantService.getMerchantsSingleRecord(any())).thenReturn(merchantSearchResponse);
    AccessResources accessResource = UserTestUtility.getAccessResources(DataConstants.ADMIN, "Invalid");
    String legalEntityName = userFilterUtil.getLegalEntityNameFromMasterMerchant(accessResource);
    assertNull(legalEntityName);
    verify(masterMerchantService, times(1)).getMerchantsSingleRecord(any());
  }
}
