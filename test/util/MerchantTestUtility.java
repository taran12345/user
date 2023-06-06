// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ParentMerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PartnerLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.SourceAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MerchantTestUtility {

  /**
   * Get partner hierarchy.
   */
  public static PartnerLegalEntity getPartnerHierarchy() {
    PartnerLegalEntity partner = new PartnerLegalEntity();
    partner.setResourceType("PARTNER");
    partner.setResourceId("13920");
    partner.setName("ABC Company");
    partner.setPmles(getPmles());
    return partner;
  }

  /**
   * Get list of ParentMerchantLegalEntity.
   */
  public static Set<ParentMerchantLegalEntity> getPmles() {
    PaymentAccount paymentAccount = new PaymentAccount();
    paymentAccount.setResourceType("PAYMENT_ACCOUNT");
    MerchantLegalEntity mle = new MerchantLegalEntity();
    mle.setResourceType("MLE");
    mle.setPaymentAccounts(new HashSet<>(Arrays.asList(paymentAccount)));
    ParentMerchantLegalEntity pmle = new ParentMerchantLegalEntity();
    pmle.setResourceType("PMLE");
    pmle.setMles(new HashSet<>(Arrays.asList(mle)));
    Set<ParentMerchantLegalEntity> pmles = new HashSet<>();
    pmles.add(pmle);
    return pmles;
  }

  /**
   * Get merchants for pmle.
   */
  public static MerchantSearchResponse getMerchantsForPmle() {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(10);
    response.setTotalSearchMatches(1);
    response.setMerchants(new ArrayList<>(Arrays.asList(getMerchant1())));
    return response;
  }

  /**
   * Get merchants for multiple pmle.
   */
  public static MerchantSearchResponse getMerchantsForMultiplePmle(boolean isMultipleMle) {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(10);
    response.setTotalSearchMatches(1);
    response.setMerchants(new ArrayList<>(Arrays.asList(getMerchant5(isMultipleMle))));
    return response;
  }

  /**
   * Get merchants for mle.
   */
  public static MerchantSearchResponse getMerchantsForMle() throws IOException {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(10);
    response.setTotalSearchMatches(1);
    response.setMerchants(Arrays.asList(getMerchant2()));
    return response;
  }

  /**
   * Get merchants for partner.
   */
  public static MerchantSearchResponse getMerchantsForPartner() throws IOException {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(10);
    response.setTotalSearchMatches(2);
    response.setTotalCount(2);
    response.setMerchants(new ArrayList<>(Arrays.asList(getMerchant1(), getMerchant2())));
    return response;
  }

  /**
   * Get merchants for partner.
   */
  public static MerchantSearchResponse getMerchantsSomeRecordsWithoutPmleData() throws IOException {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(10);
    response.setTotalSearchMatches(3);
    response.setTotalCount(3);
    response
        .setMerchants(new ArrayList<>(Arrays.asList(getMerchant1(), getMerchant2(), getMerchant3(), getMerchant4())));
    return response;
  }

  /**
   * Get merchants for partner.
   */
  public static MerchantSearchResponse getMerchantsRecordsWithoutPmleData() throws IOException {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(10);
    response.setTotalSearchMatches(3);
    response.setTotalCount(3);
    response.setMerchants(new ArrayList<>(Arrays.asList(getMerchant3(), getMerchant4())));
    return response;
  }

  /**
   * Get 1000 merchants response.
   */
  public static MerchantSearchResponse get1000Merchants() throws IOException {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(0);
    response.setLimit(1000);
    response.setTotalSearchMatches(1010);
    response.setTotalCount(1010);
    List<MerchantResponse> merchants = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      merchants.add(getMerchant1());
    }
    response.setMerchants(merchants);
    return response;
  }

  /**
   * Get 10 merchants response.
   */
  public static MerchantSearchResponse get10Merchants() throws IOException {
    MerchantSearchResponse response = new MerchantSearchResponse();
    response.setOffset(1000);
    response.setLimit(1000);
    response.setTotalSearchMatches(1010);
    response.setTotalCount(1010);
    List<MerchantResponse> merchants = new ArrayList<>();
    for (int i = 1; i <= 9; i++) {
      merchants.add(getMerchant1());
    }
    merchants.add(getMerchant2());
    response.setMerchants(merchants);
    return response;
  }

  /**
   * Sample merchant1.
   */
  private static MerchantResponse getMerchant1() {
    ProcessingAccount processingAccount = new ProcessingAccount();
    processingAccount.setId("proc1");
    processingAccount.setPmleId("p1111");
    processingAccount.setPmleName("pmle1");

    SourceAuthority sourceAuthority = new SourceAuthority();
    sourceAuthority.setReferenceId("fma1");
    processingAccount.setSourceAuthority(sourceAuthority);

    ProcessingAccount.LegalEntity legalEntity = new ProcessingAccount.LegalEntity();
    legalEntity.setId("m1111");
    legalEntity.setDescription("mle1");
    ProcessingAccount.BusinessDetails businessDetails = new ProcessingAccount.BusinessDetails();
    businessDetails.setLegalEntity(legalEntity);
    businessDetails.setAccountGroups(new ArrayList<>(Arrays.asList("1234", "6789")));
    businessDetails.setBusinessRelationName("testbrn1");

    ProcessingAccount.OnboardingInformation onboardingInformation = new ProcessingAccount.OnboardingInformation();
    onboardingInformation.setPartnerId("pa1111");
    onboardingInformation.setPartnerName("partner1");
    businessDetails.setOnboardingInformation(onboardingInformation);
    processingAccount.setBusinessDetails(businessDetails);

    PaymentAccountResponse paymentAccount = new PaymentAccountResponse();
    paymentAccount.setId("payment1");
    paymentAccount.setCurrency("USD");
    paymentAccount.setProcessingAccounts(Arrays.asList(processingAccount));

    MerchantResponse merchant = new MerchantResponse();
    merchant.setPaymentAccounts(Arrays.asList(paymentAccount));
    return merchant;
  }

  /**
   * Sample merchant2.
   */
  private static MerchantResponse getMerchant2() {
    ProcessingAccount processingAccount = new ProcessingAccount();
    processingAccount.setId("proc2");
    processingAccount.setPmleId("p2222");
    processingAccount.setPmleName("pmle2");

    SourceAuthority sourceAuthority = new SourceAuthority();
    sourceAuthority.setReferenceId("fma2");
    processingAccount.setSourceAuthority(sourceAuthority);

    ProcessingAccount.LegalEntity legalEntity = new ProcessingAccount.LegalEntity();
    legalEntity.setId("m2222");
    legalEntity.setDescription("mle2");
    ProcessingAccount.BusinessDetails businessDetails = new ProcessingAccount.BusinessDetails();
    businessDetails.setLegalEntity(legalEntity);
    businessDetails.setBusinessRelationName("testbrn2");

    ProcessingAccount.OnboardingInformation onboardingInformation = new ProcessingAccount.OnboardingInformation();
    onboardingInformation.setPartnerId("pa2222");
    onboardingInformation.setPartnerName("partner2");
    businessDetails.setOnboardingInformation(onboardingInformation);
    processingAccount.setBusinessDetails(businessDetails);

    PaymentAccountResponse paymentAccount = new PaymentAccountResponse();
    paymentAccount.setId("payment2");
    paymentAccount.setCurrency("CAD");
    paymentAccount.setProcessingAccounts(Arrays.asList(processingAccount));

    MerchantResponse merchant = new MerchantResponse();
    merchant.setPaymentAccounts(Arrays.asList(paymentAccount));
    return merchant;
  }

  /**
   * Sample merchant3 with pmle as null.
   */
  private static MerchantResponse getMerchant3() {
    ProcessingAccount processingAccount = new ProcessingAccount();
    processingAccount.setId("proc3");
    processingAccount.setPmleId(null);
    processingAccount.setPmleName(null);

    SourceAuthority sourceAuthority = new SourceAuthority();
    sourceAuthority.setReferenceId("fma3");
    processingAccount.setSourceAuthority(sourceAuthority);

    ProcessingAccount.LegalEntity legalEntity = new ProcessingAccount.LegalEntity();
    legalEntity.setId("m3333");
    legalEntity.setDescription("mle3");
    ProcessingAccount.BusinessDetails businessDetails = new ProcessingAccount.BusinessDetails();
    businessDetails.setLegalEntity(legalEntity);

    ProcessingAccount.OnboardingInformation onboardingInformation = new ProcessingAccount.OnboardingInformation();
    onboardingInformation.setPartnerId("pa3333");
    onboardingInformation.setPartnerName("partner3");
    businessDetails.setOnboardingInformation(onboardingInformation);
    processingAccount.setBusinessDetails(businessDetails);

    PaymentAccountResponse paymentAccount = new PaymentAccountResponse();
    paymentAccount.setId("payment3");
    paymentAccount.setCurrency("INR");
    paymentAccount.setProcessingAccounts(Arrays.asList(processingAccount));

    MerchantResponse merchant = new MerchantResponse();
    merchant.setPaymentAccounts(Arrays.asList(paymentAccount));
    return merchant;
  }

  /**
   * Sample merchant4 with pmle as empty.
   */
  private static MerchantResponse getMerchant4() {
    ProcessingAccount processingAccount = new ProcessingAccount();
    processingAccount.setId("proc4");
    processingAccount.setPmleId("");
    processingAccount.setPmleName("");

    SourceAuthority sourceAuthority = new SourceAuthority();
    sourceAuthority.setReferenceId("fma4");
    processingAccount.setSourceAuthority(sourceAuthority);

    ProcessingAccount.LegalEntity legalEntity = new ProcessingAccount.LegalEntity();
    legalEntity.setId("m4444");
    legalEntity.setDescription("mle4");
    ProcessingAccount.BusinessDetails businessDetails = new ProcessingAccount.BusinessDetails();
    businessDetails.setLegalEntity(legalEntity);

    ProcessingAccount.OnboardingInformation onboardingInformation = new ProcessingAccount.OnboardingInformation();
    onboardingInformation.setPartnerId("pa4444");
    onboardingInformation.setPartnerName("partner4");
    businessDetails.setOnboardingInformation(onboardingInformation);
    processingAccount.setBusinessDetails(businessDetails);

    PaymentAccountResponse paymentAccount = new PaymentAccountResponse();
    paymentAccount.setId("payment4");
    paymentAccount.setCurrency("GBP");
    paymentAccount.setProcessingAccounts(Arrays.asList(processingAccount));

    MerchantResponse merchant = new MerchantResponse();
    merchant.setPaymentAccounts(Arrays.asList(paymentAccount));
    return merchant;
  }

  /**
   * Sample merchant5 with more than one pmle.
   */
  private static MerchantResponse getMerchant5(boolean isMultipleMle) {
    ProcessingAccount processingAccount = new ProcessingAccount();
    processingAccount.setId("proc4");
    if (isMultipleMle) {
      processingAccount.setPmleId("p2222");
    } else {
      processingAccount.setPmleId("p3333");
    }

    processingAccount.setPmleName("");

    SourceAuthority sourceAuthority = new SourceAuthority();
    sourceAuthority.setReferenceId("fma4");
    processingAccount.setSourceAuthority(sourceAuthority);

    ProcessingAccount.LegalEntity legalEntity = new ProcessingAccount.LegalEntity();
    legalEntity.setId("m4444");
    legalEntity.setDescription("mle4");
    ProcessingAccount.BusinessDetails businessDetails = new ProcessingAccount.BusinessDetails();
    businessDetails.setLegalEntity(legalEntity);

    ProcessingAccount.OnboardingInformation onboardingInformation = new ProcessingAccount.OnboardingInformation();
    onboardingInformation.setPartnerId("pa4444");
    onboardingInformation.setPartnerName("partner4");
    businessDetails.setOnboardingInformation(onboardingInformation);
    processingAccount.setBusinessDetails(businessDetails);

    PaymentAccountResponse paymentAccount = new PaymentAccountResponse();
    paymentAccount.setId("payment4");
    paymentAccount.setCurrency("GBP");

    paymentAccount.setProcessingAccounts(Arrays.asList(processingAccount));

    List<PaymentAccountResponse> paymentAccountResponseList = new ArrayList<>();
    paymentAccountResponseList.add(paymentAccount);

    paymentAccountResponseList.add(getMerchant2().getPaymentAccounts().get(0));

    MerchantResponse merchant = new MerchantResponse();
    merchant.setPaymentAccounts(paymentAccountResponseList);
    return merchant;
  }
}
