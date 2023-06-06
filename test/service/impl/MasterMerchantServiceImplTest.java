// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.feignclients.MasterMerchantFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserSpecification;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.util.MerchantTestUtility;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.UserFilterUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccountGroupMerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.FetchEmailResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ParentMerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PartnerLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MasterMerchantServiceImplTest {

  @InjectMocks
  MasterMerchantServiceImpl masterMerchantServiceImpl;

  @Mock
  UserService userService;

  @Mock
  MasterMerchantFeignClient masterMerchantFeignClient;

  @Mock
  private UserConfig userConfig;

  @Mock
  private UserFilterUtil userFilterUtil;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private UserSpecification userSpecification;

  /**
   * Setup test configuration.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEmailWithPmleResourceType() {
    Mockito
        .when(userService.getUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(UserTestUtility.getUsersListResponseResource("PMLE"));
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.setTotalCount(1);
    Mockito.when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(Mockito.any()))
        .thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    List<FetchEmailResponseResource> emails = masterMerchantServiceImpl.getEmail("sampleUserName", "SKRILL");
    Assert.assertEquals(emails.get(0).getEmail(), "sample@email.com");
  }

  @Test
  public void getEmailWithMleResourceType() {
    Mockito
        .when(userService.getUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(UserTestUtility.getUsersListResponseResource("MLE"));
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.setTotalCount(1);
    Mockito.when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(Mockito.any()))
        .thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    List<FetchEmailResponseResource> emails = masterMerchantServiceImpl.getEmail("sampleUserName", "SKRILL");
    Assert.assertEquals(emails.get(0).getEmail(), "sample@email.com");
  }

  @Test
  public void getEmailWithPaymentAccountResourceType() {
    Mockito
        .when(userService.getUsers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(UserTestUtility.getUsersListResponseResource("PAYMENT_ACCOUNT"));
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.setTotalCount(1);
    Mockito.when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(Mockito.any()))
        .thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    List<FetchEmailResponseResource> emails = masterMerchantServiceImpl.getEmail("sampleUserName", "PORTAL");
    Assert.assertEquals(emails.get(0).getEmail(), "sample@email.com");
  }

  @Test
  public void testToGetPmleListWithOwnerTypePmle() {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPmle();
    merchantSearchResponse.setTotalCount(1);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Set<ParentMerchantLegalEntity> pmles = masterMerchantServiceImpl.getPmleList("PMLE", "12345", true);

    assertNotNull(pmles);
    assertThat(pmles.size(), Is.is(1));
    pmles.forEach(pmle -> {
      Set<MerchantLegalEntity> mles = pmle.getMles();
      assertThat(mles.size(), Is.is(1));
    });
  }

  @Test
  public void testToGetPmleListWithOwnerTypePartner() throws IOException {
    MerchantSearchResponse merchantSearchRespons = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchRespons.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchRespons);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Set<ParentMerchantLegalEntity> pmles = masterMerchantServiceImpl.getPmleList("PARTNER", "43434", false);

    validateTheResponse(pmles);
  }

  @Test
  public void testToGetPmleListWithOwnerTypeMle() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForMle();
    merchantSearchResponse.setTotalCount(1);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Set<ParentMerchantLegalEntity> pmles = masterMerchantServiceImpl.getPmleList("MLE", "12426242", false);

    assertNotNull(pmles);
    assertThat(pmles.size(), Is.is(1));
    pmles.forEach(pmle -> {
      assertThat(pmle.getResourceType(), Is.is("PMLE"));
      assertThat(pmle.getResourceId(), Is.is(""));
      assertThat(pmle.getName(), Is.is(""));
    });
  }

  @Test
  public void testToGetPmleList_WhenMerchantsFoundMoreThan_1000() throws IOException {
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.get1000Merchants(), MerchantTestUtility.get10Merchants());
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Set<ParentMerchantLegalEntity> pmles = masterMerchantServiceImpl.getPmleList("PMLE", "437463", false);
    validateTheResponse(pmles);
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void testPartnerHierarchyWhenOwnerTypePartner() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    PartnerLegalEntity partner = masterMerchantServiceImpl.getPartnerHierarchy("PARTNER", "43434", false);

    assertThat(partner.getResourceId(), Is.is("pa1111"));
    assertThat(partner.getName(), Is.is("partner1"));
    validateTheResponse(partner.getPmles());
  }

  @Test
  public void testPartnerHierarchyWhenOwnerTypeBusinessRelationName() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    PartnerLegalEntity partner =
        masterMerchantServiceImpl.getPartnerHierarchy("BUSINESS_RELATION_NAME", "43434", false);
    assertThat(partner.getResourceId(), Is.is("testbrn1"));
    assertThat(partner.getName(), Is.is("testbrn1"));
    validateTheResponse(partner.getPmles());
  }

  @Test
  public void testPartnerHierarchyWhenOwnerTypePmle() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    PartnerLegalEntity partner = masterMerchantServiceImpl.getPartnerHierarchy("PMLE", "3746374", true);

    assertThat(partner.getResourceId(), Is.is(""));
    assertThat(partner.getName(), Is.is(""));
    assertThat(partner.getResourceType(), Is.is("PARTNER"));
    validateTheResponse(partner.getPmles());
  }

  @Test
  public void testGetMerchantForAccountGroups() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    assertThat(masterMerchantServiceImpl.getMerchantForAccountGroups(new ArrayList<>(
        Arrays.asList("6789")), null, 2, 10).getTotalCount(), Is.is(2));
  }

  @Test
  public void testGetMerchantForAccountGroups_withAccountGroupIdAndPaymentAccountId() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    assertThat(masterMerchantServiceImpl.getMerchantForAccountGroups(new ArrayList<>(Arrays.asList("6789")),
        new ArrayList<>(Arrays.asList("12345")), 2, 10).getTotalCount(), Is.is(2));
  }

  @Test
  public void testGetMerchantForAccountGroups_MoreThanTenThousandRecord() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    assertThat(masterMerchantServiceImpl.getMerchantForAccountGroups(new ArrayList<>(
        Arrays.asList("1234")), null, 2, 10000).getTotalCount(), Is.is(2));
  }

  @Test
  public void getMerchantForAccountGroups_withUnmatchedAccountGroup_shouldReturnNullAccountGroupId()
      throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    AccountGroupMerchantSearchResponse accountGroupMerchantesponse =
        masterMerchantServiceImpl.getMerchantForAccountGroups(new ArrayList<>(Arrays.asList("7777")), null, 2, 10000);
    assertNull(accountGroupMerchantesponse.getAccountGroupId());
  }

  @Test
  public void testPartnerHierarchyWhenOwnerTypeMle() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    PartnerLegalEntity partner = masterMerchantServiceImpl.getPartnerHierarchy("MLE", "3746374", false);

    assertThat(partner.getResourceId(), Is.is(""));
    assertThat(partner.getName(), Is.is(""));
    assertThat(partner.getResourceType(), Is.is("PARTNER"));
    assertThat(partner.getPmles().size(), Is.is(1));
    partner.getPmles().forEach(pmle -> {
      assertThat(pmle.getResourceType(), Is.is("PMLE"));
      assertThat(pmle.getName(), Is.is(""));
      assertThat(pmle.getResourceId(), Is.is(""));
      Set<MerchantLegalEntity> mles = pmle.getMles();
      assertThat(mles.size(), Is.is(1));
    });
  }

  @Test
  public void testPartnerHierarchyWhenMleNotMappedToPmle_PmleFoundInFetchedRecords() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(2);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(
        MerchantTestUtility.getMerchantsSomeRecordsWithoutPmleData(), merchantSearchResponse, merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    PartnerLegalEntity partner = masterMerchantServiceImpl.getPartnerHierarchy("PARTNER", "43434", false);

    assertThat(partner.getResourceId(), Is.is("pa1111"));
    assertThat(partner.getName(), Is.is("partner1"));
    Set<ParentMerchantLegalEntity> pmles = partner.getPmles();
    assertThat(pmles.size(), Is.is(2));
    pmles.forEach(pmle -> {
      if (pmle.getResourceId().equalsIgnoreCase("p1111")) {
        assertThat(pmle.getMles().size(), Is.is(3));
      }
    });
  }

  @Test
  public void testPartnerHierarchyWhenMleNotMappedToPmle_PmleNotFoundInFetchedRecords() throws IOException {
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(
        MerchantTestUtility.getMerchantsSomeRecordsWithoutPmleData(), MerchantTestUtility.getMerchantsForPartner(),
        MerchantTestUtility.getMerchantsRecordsWithoutPmleData());
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    PartnerLegalEntity partner = masterMerchantServiceImpl.getPartnerHierarchy("PARTNER", "43434", false);

    assertThat(partner.getResourceId(), Is.is("pa1111"));
    assertThat(partner.getName(), Is.is("partner1"));
    Set<ParentMerchantLegalEntity> pmles = partner.getPmles();
    assertThat(pmles.size(), Is.is(3));
    pmles.forEach(pmle -> {
      if (pmle.getResourceId().equalsIgnoreCase("p1111")) {
        assertThat(pmle.getMles().size(), Is.is(2));
      }
    });
  }

  @Test
  public void getMerchantsSingleRecord_withValidInput_shouldSucceed() throws IOException {
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.getMerchantsForPartner());
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    masterMerchantServiceImpl.getMerchantsSingleRecord(UserTestUtility.getMerchantSearchAfterRequest());
    verify(masterMerchantFeignClient, times(1)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getAllPmleIds_withValidInput_shouldSucceed() {
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.setTotalCount(1);
    Mockito.when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(Mockito.any()))
        .thenReturn(merchantSearchResponse);
    masterMerchantServiceImpl.getAllPmleIds(DataConstants.PMLE, "1234");
    verify(masterMerchantFeignClient, times(1)).getMerchantsUsingSearchAfter(any());
  }

  private void validateTheResponse(Set<ParentMerchantLegalEntity> pmles) {
    assertNotNull(pmles);
    assertThat(pmles.size(), Is.is(2));
    for (ParentMerchantLegalEntity pmle : pmles) {
      assertThat(pmle.getMles().size(), Is.is(1));
    }
  }

  @Test
  public void getPartnerHierarchyList_withPartner_withValidInput_shouldSucceed() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(3);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Set<PartnerLegalEntity> partners = masterMerchantServiceImpl.getPartnerHierarchyList("PARTNER", "43434");
    assertNotNull(partners);
    assertThat(partners.size(), Is.is(2));
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getPartnerHierarchyList_withBusinessRelationName_withValidInput_shouldSucceed() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(3);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Set<PartnerLegalEntity> partners = masterMerchantServiceImpl.getPartnerHierarchyList("BUSINESS_RELATION_NAME",
            "1234");
    assertNotNull(partners);
    assertThat(partners.size(), Is.is(2));
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getAccountGroupsByContainsSearch_withValidInput_shouldSucceed() throws IOException {
    MerchantSearchResponse merchantSearchResponse = MerchantTestUtility.getMerchantsForPartner();
    merchantSearchResponse.setTotalCount(3);
    when(masterMerchantFeignClient.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    Mockito.when(userConfig.getMasterMerchantSearchAfterFetchLimit()).thenReturn(1L);
    Mockito.when(userFilterUtil.getAccountGroupIds(any(), any()))
        .thenReturn(new HashSet<>(Arrays.asList("ag1", "ag2")));
    List<AccountGroupMerchantSearchResponse> response =
        masterMerchantServiceImpl.getAccountGroupsByContainsSearch("ag");
    assertNotNull(response);
    verify(masterMerchantFeignClient, times(2)).getMerchantsUsingSearchAfter(any());
    verify(userFilterUtil, times(4)).getAccountGroupIds(any(), any());
  }

  @Test
  public void setIdOrNameSearchParamsByResourceType_withAccountGroupIdAndType_shouldSetAccountGroupId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameSearchParamsByResourceType(ResourceType.ACCOUNT_GROUP.toString(), "1234",
        request);
    assertNotNull(request.getSearchParams().getAccountGroups());
  }

  @Test
  public void setIdOrNameSearchParamsByResourceType_withBusinessRelationNameIdAndType_shouldSetBusinessRelationName() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameSearchParamsByResourceType(ResourceType.BUSINESS_RELATION_NAME.toString(),
            "1234", request);
    assertNotNull(request.getSearchParams().getBusinessRelationName());
  }

  @Test
  public void setIdOrNameSearchParamsByResourceType_withFmaIdAndType_shouldSetMerchantId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameSearchParamsByResourceType(ResourceType.FMA.toString(), "100011", request);
    assertNotNull(request.getSearchParams().getMerchantId());
    assertNotNull(request.getSearchParams().getMerchantName());
  }

  @Test
  public void setIdOrNameSearchParamsByResourceType_withMleIdAndType_shouldSetMerchantLegalEntityAndId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameSearchParamsByResourceType(ResourceType.MLE.toString(), "456", request);
    assertNotNull(request.getSearchParams().getMerchantLegalEntity());
    assertNotNull(request.getSearchParams().getMerchantLegalEntityId());
  }

  @Test
  public void getAccountTypeUsers_withValidInput_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(userFilterUtil.mapUserStatusFilter(any())).thenReturn(Status.ACTIVE);
    when(usersRepository.findAll(Mockito.any(), Mockito.any(PageRequest.class)))
        .thenReturn(new PageImpl<>(Arrays.asList(UserTestUtility.getUser())));
    UserSpecification userSpecificationCreated = new UserSpecification();
    Specification<User> userSpec = userSpecificationCreated.constructPortalUsersSpecification(
        UserFetchByFiltersRequestDto.builder().application(DataConstants.PORTAL).build());
    when(userService.checkApplicationAndConstructSpecification(any(), anyBoolean(), anyBoolean())).thenReturn(userSpec);
    masterMerchantServiceImpl.getAccountTypeUsers(ResourceType.FMA, "1234", UserStatusFilter.ACTIVE, true,
        Integer.valueOf(0), Integer.valueOf(10));
    CommonThreadLocal.unsetAuthLocal();
    verify(usersRepository, times(1)).findAll(Mockito.any(), Mockito.any(PageRequest.class));
  }

  @Test
  public void getAccountTypeUsers_withInValidFma_shouldResturnEmptyUserList() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(userFilterUtil.mapUserStatusFilter(any())).thenReturn(Status.ACTIVE);
    when(usersRepository.findAll(Mockito.any(), Mockito.any(PageRequest.class)))
        .thenReturn(new PageImpl<>(Arrays.asList(UserTestUtility.getUser())));
    when(userService.checkApplicationAndConstructSpecification(any(), anyBoolean(), anyBoolean())).thenReturn(null);
    UsersListResponseResource userList = masterMerchantServiceImpl.getAccountTypeUsers(ResourceType.FMA, "1234322",
        UserStatusFilter.ACTIVE, true, Integer.valueOf(0), Integer.valueOf(10));
    assertThat(userList.getCount(), Is.is(0L));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setIdOrNameFilterParamsByResourceType_withPartnerTypeAndId_shouldSetPartnerId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameFilterParamsByResourceType(ResourceType.PARTNER.toString(), "1234", request);
    assertNotNull(request.getFilterParams().getPartnerId());
  }

  @Test
  public void setIdOrNameFilterParamsByResourceType_withPmleTypeAndId_shouldSetPmleId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameFilterParamsByResourceType(ResourceType.PMLE.toString(), "1234", request);
    assertNotNull(request.getFilterParams().getPmleId());
  }

  @Test
  public void setIdOrNameFilterParamsByResourceType_withMleTypeAndId_shouldSetMleId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameFilterParamsByResourceType(ResourceType.MLE.toString(), "1234", request);
    assertNotNull(request.getFilterParams().getMerchantLegalEntityId());
  }

  @Test
  public void setIdOrNameFilterParamsByResourceType_withAccountGroupTypeAndId_shouldSetAccountGroups() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameFilterParamsByResourceType(ResourceType.ACCOUNT_GROUP.toString(), "1234",
        request);
    assertNotNull(request.getFilterParams().getAccountGroups());
  }

  @Test
  public void setIdOrNameFilterParamsByResourceType_withFmaTypeAndId_shouldSetMerchantId() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    masterMerchantServiceImpl.setIdOrNameFilterParamsByResourceType(ResourceType.FMA.toString(), "1234", request);
    assertNotNull(request.getFilterParams().getMerchantId());
  }
}
