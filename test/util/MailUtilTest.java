// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.config.UserGcsEventsConfig;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.WalletPermission;
import com.paysafe.upf.user.provisioning.repository.WalletPermissionRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.utils.MailUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MailUtilTest {

  private static final String SKRILL_HOST_URL = "www.skrill.com";
  private static final String NETELLER_HOST_URL = "www.netelller.com";
  private static final String PORTAL_HOST_URL = "www.portal.com";
  private static final String PARTNER_PORTAL_HOST_URL = "www.partner-portal.com";

  @Mock
  private MerchantAccountInfoService merchantAccountInfoService;

  @Mock
  private ResourceLoader resourceLoader;

  @Mock
  private WalletPermissionRepository walletPermissionRepository;

  @Mock
  private UserProvisioningConfig userProvisioningConfig;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @InjectMocks
  private MailUtil mailUtil;

  private UserProvisioningUserResource userResponse;

  /**
   * Data initialization.
   */
  @Before
  public void setUp() throws Exception {
    userResponse = UserTestUtility.getUserProvisioningUserResource();
  }

  private UserConfig getUserConfig() {
    Map<String, String> skrillEventIds = new HashMap<>();
    skrillEventIds.put(DataConstants.PERMISSIONS_UPDATED_EMAIL_EVENTID, "123");
    skrillEventIds.put(DataConstants.ACTIVATION_EMAIL_EVENTID, "456");
    Map<String, String> netellerEventIds = new HashMap<>();
    netellerEventIds.put(DataConstants.PERMISSIONS_UPDATED_EMAIL_EVENTID, "789");
    netellerEventIds.put(DataConstants.ACTIVATION_EMAIL_EVENTID, "012");
    Map<String, String> businessPortalEventIds = new HashMap<>();
    businessPortalEventIds.put(DataConstants.PERMISSIONS_UPDATED_EMAIL_EVENTID, "345");
    businessPortalEventIds.put(DataConstants.ACTIVATION_EMAIL_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.PROFILE_SETTINGS_MFA_ENABLED_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.PROFILE_SETTINGS_MFA_DISABLED_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.ADMIN_MFA_ENABLED_INAPP_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.SUBUSER_MFA_ENABLED_EMAIL_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.ADMIN_MFA_DISABLED_INAPP_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.SUBUSER_MFA_DISABLED_EMAIL_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.RESET_MFA_EMAIL_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.ADMIN_RESET_MFA_INAPP_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.ADMIN_BULK_UPDATE_SUCCESS_MFA_INAPP_EVENTID, "678");
    businessPortalEventIds.put(DataConstants.ADMIN_BULK_FAILURE_SUCCESS_MFA_INAPP_EVENTID, "678");
    Map<String, String> partnerPortalEventIds = new HashMap<>();
    partnerPortalEventIds.put(DataConstants.PERMISSIONS_UPDATED_EMAIL_EVENTID, "901");
    partnerPortalEventIds.put(DataConstants.ACTIVATION_EMAIL_EVENTID, "234");
    UserGcsEventsConfig userGcsEventsConfig = new UserGcsEventsConfig();
    userGcsEventsConfig.setSkrill(skrillEventIds);
    userGcsEventsConfig.setNeteller(netellerEventIds);
    userGcsEventsConfig.setPartnerPortal(partnerPortalEventIds);
    userGcsEventsConfig.setBusinessPortal(businessPortalEventIds);
    UserConfig userConfig = new UserConfig();
    userConfig.setGcsEvents(userGcsEventsConfig);
    return userConfig;
  }

  private void mockResources(String application, String ownerType) {
    Resource resource1 = null;
    Resource resource2 = null;
    if (StringUtils.equals(application, DataConstants.SKRILL)
        || StringUtils.equals(application, DataConstants.NETELLER)) {
      String resource1FilePath = "/custom-email-templates/skrill-registration-confirm/table.html";
      resource1 = new ClassPathResource(resource1FilePath, getClass());
      String resource2FilePath = "/custom-email-templates/skrill-registration-confirm/tableRow.html";
      resource2 = new ClassPathResource(resource2FilePath, getClass());
    } else if (StringUtils.equals(application, DataConstants.PORTAL)) {
      if (StringUtils.equals(ownerType, DataConstants.PARTNER)) {
        String resource1FilePath = "/custom-email-templates/partnerPortal-registration-confirm/table.html";
        resource1 = new ClassPathResource(resource1FilePath, getClass());
        String resource2FilePath = "/custom-email-templates/partnerPortal-registration-confirm/tableRow.html";
        resource2 = new ClassPathResource(resource2FilePath, getClass());
      } else {
        String resource1FilePath = "/custom-email-templates/businessPortal-registration-confirm/table.html";
        resource1 = new ClassPathResource(resource1FilePath, getClass());
        String resource2FilePath = "/custom-email-templates/businessPortal-registration-confirm/tableRow.html";
        resource2 = new ClassPathResource(resource2FilePath, getClass());
      }
    }
    when(resourceLoader.getResource(any())).thenReturn(resource1).thenReturn(resource2);
  }

  // getEventId() testCases

  @Test
  public void getEventId_withSkrillApplicationAndRegistrationMailType_shouldSucceed() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.SKRILL, DataConstants.REGISTRATION_MAIL);
    assertThat(eventId, Is.is("456"));
  }

  @Test
  public void getEventId_withSkrillApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.SKRILL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(eventId, Is.is("123"));
  }

  @Test
  public void getEventId_withPartnerPortalApplicationAndRegistrationMail_shouldSucceed() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.PARTNER_PORTAL, DataConstants.REGISTRATION_MAIL);
    assertThat(eventId, Is.is("234"));
  }

  @Test
  public void getEventId_withPartnerPortalApplicationAndPermissionsUpdatedMail_shouldSucceed() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId =
        mailUtil.getEventId(userResponse, DataConstants.PARTNER_PORTAL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(eventId, Is.is("901"));
  }

  @Test
  public void getEventId_withNetellerApplicationAndRegistrationMailType_shouldSucceed() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.NETELLER, DataConstants.REGISTRATION_MAIL);
    assertThat(eventId, Is.is("012"));
  }

  @Test
  public void getEventId_withNetellerApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.NETELLER, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(eventId, Is.is("789"));
  }

  @Test
  public void getEventId_withPartnerPortalApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PARTNER);
    userResponse.setCustomProperties(customProperties);
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.PORTAL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(eventId, Is.is("901"));
  }

  @Test
  public void getEventId_withPartnerPortalApplicationAndRegistrationMailType_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PARTNER);
    userResponse.setCustomProperties(customProperties);
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.PORTAL, DataConstants.REGISTRATION_MAIL);
    assertThat(eventId, Is.is("234"));
  }

  @Test
  public void getEventId_withBusinessPortalApplicationAndRegistrationMailType_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.PORTAL, DataConstants.REGISTRATION_MAIL);
    assertThat(eventId, Is.is("678"));
  }

  @Test
  public void getEventId_withBusinessPortalApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventId(userResponse, DataConstants.PORTAL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(eventId, Is.is("345"));
  }

  // getEventIdsForMfa

  @Test
  public void getEventIdForMfa_withEventIdKey_shouldSuccess() {
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String eventId = mailUtil.getEventIdForMfa(DataConstants.PROFILE_SETTINGS_MFA_ENABLED_EVENTID);
    assertThat(eventId, Is.is("678"));
  }

  // getLink() testCases

  @Test
  public void getLink_withSkrillApplicationAndRegistrationMailType_shouldSucceed() {
    when(userProvisioningConfig.getSkrillHostUrl()).thenReturn(SKRILL_HOST_URL);
    String link = mailUtil.getLink(DataConstants.SKRILL, DataConstants.REGISTRATION_MAIL);
    assertThat(link, Is.is(SKRILL_HOST_URL + DataConstants.ACTIVATE_URI));
  }

  @Test
  public void getLink_withSkrillApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    when(userProvisioningConfig.getSkrillHostUrl()).thenReturn(SKRILL_HOST_URL);
    String link = mailUtil.getLink(DataConstants.SKRILL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(link, Is.is(SKRILL_HOST_URL + DataConstants.LOGIN_URI));
  }

  @Test
  public void getLink_withPartnerPortalApplicationAndRegistrationMailType_shouldSucceed() {
    when(userProvisioningConfig.getPartnerPortalHostUrl()).thenReturn(PARTNER_PORTAL_HOST_URL);
    String link = mailUtil.getLink(DataConstants.PARTNER_PORTAL, DataConstants.REGISTRATION_MAIL);
    assertThat(link, Is.is(PARTNER_PORTAL_HOST_URL + DataConstants.ACTIVATE_URI));
  }

  @Test
  public void getLink_withPartnerPortalApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    when(userProvisioningConfig.getPartnerPortalHostUrl()).thenReturn(PARTNER_PORTAL_HOST_URL);
    String link = mailUtil.getLink(DataConstants.PARTNER_PORTAL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(link, Is.is(PARTNER_PORTAL_HOST_URL + DataConstants.LOGIN_URI));
  }

  @Test
  public void getLink_withNetellerApplicationAndRegistrationMailType_shouldSucceed() {
    when(userProvisioningConfig.getNetellerHostUrl()).thenReturn(NETELLER_HOST_URL);
    String link = mailUtil.getLink(DataConstants.NETELLER, DataConstants.REGISTRATION_MAIL);
    assertThat(link, Is.is(NETELLER_HOST_URL + DataConstants.ACTIVATE_URI));
  }

  @Test
  public void getLink_withNetellerApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    when(userProvisioningConfig.getNetellerHostUrl()).thenReturn(NETELLER_HOST_URL);
    String link = mailUtil.getLink(DataConstants.NETELLER, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(link, Is.is(NETELLER_HOST_URL + DataConstants.LOGIN_URI));
  }

  @Test
  public void getLink_withPortalApplicationAndPermissionsUpdatedMailType_shouldSucceed() {
    when(userProvisioningConfig.getUiHostUrl()).thenReturn(PORTAL_HOST_URL);
    String link = mailUtil.getLink(DataConstants.PORTAL, DataConstants.PERMISSIONS_UPDATED_MAIL);
    assertThat(link, Is.is(PORTAL_HOST_URL + DataConstants.LOGIN_URI));
  }

  @Test
  public void getLink_withPortalApplicationAndRegistrationMailType_shouldSucceed() {
    when(userProvisioningConfig.getUiHostUrl()).thenReturn(PORTAL_HOST_URL);
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    String link = mailUtil.getLink(DataConstants.PORTAL, DataConstants.REGISTRATION_MAIL);
    assertThat(link, Is.is(PORTAL_HOST_URL + DataConstants.ACTIVATE_URI));
  }

  // setRegistrationTemplateVariables() testCases

  @Test
  public void setRegistrationTemplateVariables_withBusinessPortalApplicationAndValidData_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setRegistrationTemplateVariables(DataConstants.PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withPortalApplicationAndAccountsMoreThan3_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    userResponse.getUserSummary().getMerchants().get(0)
        .setAccounts(new ArrayList<>(Arrays.asList("789", "567", "789", "901")));
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setRegistrationTemplateVariables(DataConstants.PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withPartnerPortalApplicationAndValidData_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PARTNER);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setRegistrationTemplateVariables(DataConstants.PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withSkrillApplicationAndMultipleAccessResources_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockResources(DataConstants.SKRILL, DataConstants.PMLE);
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    userResponse.setAccessResources(UserTestUtility.getAccessResourcesList());
    mailUtil.setRegistrationTemplateVariables(DataConstants.SKRILL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(WordUtils.capitalizeFully(userResponse.getFirstName() + StringUtils.SPACE + userResponse.getLastName())));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("customerIds"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withSkrillApplicationAndBinanceBrand_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.BUSINESS_UNIT, DataConstants.BINANCE);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockResources(DataConstants.SKRILL, DataConstants.PMLE);
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    userResponse.setAccessResources(UserTestUtility.getAccessResourcesList());
    mailUtil.setRegistrationTemplateVariables(DataConstants.SKRILL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(WordUtils.capitalizeFully(userResponse.getFirstName() + StringUtils.SPACE + userResponse.getLastName())));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("customerIds"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withSkrillApplicationAndSkrillBrand_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.BUSINESS_UNIT, DataConstants.SKRILL);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockResources(DataConstants.SKRILL, DataConstants.PMLE);
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    userResponse.setAccessResources(UserTestUtility.getAccessResourcesList());
    mockSkrillTellerConfig();
    mailUtil.setRegistrationTemplateVariables(DataConstants.SKRILL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(WordUtils.capitalizeFully(userResponse.getFirstName() + StringUtils.SPACE + userResponse.getLastName())));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("customerIds"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withNetellerApplicationAndMultipleAccessResources_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    mockResources(DataConstants.NETELLER, DataConstants.PMLE);
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    userResponse.setAccessResources(UserTestUtility.getAccessResourcesList());
    mailUtil.setRegistrationTemplateVariables(DataConstants.NETELLER, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(WordUtils.capitalizeFully(userResponse.getFirstName() + StringUtils.SPACE + userResponse.getLastName())));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("customerIds"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withSkrillApplicationAndEmptyAccessResources_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockResources(DataConstants.SKRILL, DataConstants.PMLE);
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setRegistrationTemplateVariables(DataConstants.SKRILL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(WordUtils.capitalizeFully(userResponse.getFirstName() + StringUtils.SPACE + userResponse.getLastName())));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("customerIds"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setRegistrationTemplateVariables_withNetellerApplicationAndEmptyAccessResources_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    mockResources(DataConstants.NETELLER, DataConstants.PMLE);
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setRegistrationTemplateVariables(DataConstants.NETELLER, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(WordUtils.capitalizeFully(userResponse.getFirstName() + StringUtils.SPACE + userResponse.getLastName())));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("customerIds"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = InternalErrorException.class)
  public void setRegistrationTemplateVariables_withInvalidResourceFilePath_shouldThrowException() {
    Map<String, Object> customProperties = new HashMap<>();
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(walletPermissionRepository.findAll())
        .thenReturn(new ArrayList<>(Arrays.asList(new WalletPermission(1, "101", "WP", 1, 1, "EN"))));
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, Object> templateVariables = new HashMap<>();
    Resource resource1 = new ClassPathResource("invalid_fil_path", getClass());
    when(resourceLoader.getResource(any())).thenReturn(resource1);
    userResponse.setAccessResources(UserTestUtility.getAccessResourcesList());
    mailUtil.setRegistrationTemplateVariables(DataConstants.SKRILL, userResponse, templateVariables);
  }

  @Test
  public void setRegistrationTemplateVariables_withPartnerPortalApplication_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setRegistrationTemplateVariables(DataConstants.PARTNER_PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  // setPermissionsUpdatedTemplateVariables() testCases

  @Test
  public void setPermissionsUpdatedTemplateVariables_withBusinessPortalApplicationAndValidData_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setPermissionsUpdatedTemplateVariables(DataConstants.PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setPermissionsUpdatedTemplateVariables_withPartnerPortalApplication_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setPermissionsUpdatedTemplateVariables(DataConstants.PARTNER_PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void setPermissionsUpdatedTemplateVariables_withPartnerPortalApplicationAndValidData_shouldSucceed() {
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PARTNER);
    userResponse.setCustomProperties(customProperties);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    mockResources(DataConstants.PORTAL, DataConstants.PMLE);
    Map<String, Object> templateVariables = new HashMap<>();
    mailUtil.setPermissionsUpdatedTemplateVariables(DataConstants.PORTAL, userResponse, templateVariables);
    assertThat(templateVariables.get(DataConstants.CUSTOMER_NAME),
        Is.is(userResponse.getFirstName() + " " + userResponse.getLastName()));
    assertThat(templateVariables.get("userEmail"), Is.is(userResponse.getEmail()));
    assertNotNull(templateVariables.get("assignedMerchants"));
    CommonThreadLocal.unsetAuthLocal();
  }

  private void mockSkrillTellerConfig() {
    Map<String, List<String>> brandsMap = new HashMap<>();
    brandsMap.put("skrill", Arrays.asList("binance", "ftx"));
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(brandsMap);
    Map<String, BusinessUnitConfig> businessUnits = new HashMap<>();
    BusinessUnitConfig businessUnitConfig = new BusinessUnitConfig();
    businessUnitConfig.setAdminRole("ADMIN");
    businessUnits.put("skrill", businessUnitConfig);
    businessUnits.put("binance", businessUnitConfig);
    when(skrillTellerConfig.getBusinessUnits()).thenReturn(businessUnits);
  }
}
