// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.Merchant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MailUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);
  public static final String PASS_RECCOVERY_TOKEN_TYPE = "PASSWORD_RECOVERY";
  public static final String RESETPASS_EMAIL_EVENTID = "resetPasswordEmailEventId";
  private static final String TABLE_ROW = "{{tableRow}}";
  private static final String SKRILL_REGISTARTION_EMAIL_TABLE_PATH =
      "custom-email-templates/skrill-registration-confirm/table.html";
  private static final String SKRILL_REGISTARTION_EMAIL_TABLE_ROW_PATH =
      "custom-email-templates/skrill-registration-confirm/tableRow.html";
  private static final String SKRILL_ADMIN_ROLE_TEXT =
      "You have been assigned as Administrator to some of these wallets.";
  private static final String SKRILL_ADMINISTRATOR_GUIDE_TEXT = "<tr><td style='padding:0 30px 25px 30px; font-size: "
      + "16px; color: #171717; line-height: 1.4; font-family: Arial, Helvetica, sans-serif,Verdana;' valign='top' "
      + "align='left'> If Role is ADMIN, you are designated as the Administrator and have full permissions to the "
      + "respective Merchant Account. Please refer to the"
      + " <a href='https://www.skrill.com/fileadmin/content/pdf/Merchant_Account_Administration_Guide.pdf'"
      + " target=\"_blank\" style='color: #862165; text-decoration: underline'>Skrill Merchant Account Administrator"
      + " Guide </a>for details on how to manage the permissions for other"
      + " users.</td></tr>";
  private static final String NETELLER_ADMINISTRATOR_GUIDE_TEXT = "<tr><td style='padding:0 30px 20px 30px; font-size"
      + ": 16px; color: #171717; line-height: 1.4; font-family: Arial, Helvetica, sans-serif,Verdana;' valign='top'"
      + " align='left'> If Role is ADMIN, you are designated as the Administrator and have full permissions to the"
      + " respective Merchant Account. Please refer to the <a href='https://www.neteller.com/fileadmin/content/pdfs"
      + "/Merchant_Account_Administration_Guide.pdf' target='_blank' style='color: #79ab36;"
      + " text-decoration: underline'>NETELLER Merchant Account Administrator Guide </a>for details on how to"
      + " manage the permissions for other users.</td></tr>";
  private static final String LINK = "target=\"_blank\" style='color: #862165; text-decoration: underline'>";
  private static final String PORTAL_ADMIN_ROLE_TEXT =
      "You have been assigned as Administrator to some of these Merchants.";
  private static final String BUSINESS_PORTAL_REGISTARTION_EMAIL_TABLE_ROW_PATH =
      "custom-email-templates/businessPortal-registration-confirm/tableRow.html";
  private static final String BUSINESS_PORTAL_REGISTARTION_EMAIL_TABLE_PATH =
      "custom-email-templates/businessPortal-registration-confirm/table.html";
  private static final String PARTNER_PORTAL_REGISTARTION_EMAIL_TABLE_ROW_PATH =
      "custom-email-templates/partnerPortal-registration-confirm/tableRow.html";
  private static final String PARTNER_PORTAL_REGISTARTION_EMAIL_TABLE_PATH =
      "custom-email-templates/partnerPortal-registration-confirm/table.html";
  private static final String MORE = " more..</a>";

  @Autowired
  private UserProvisioningConfig userProvisioningConfig;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Autowired
  private ResourceLoader resourceLoader;

  /**
   * Utility method to handle the user registration confirmation mail.
   */
  public void setRegistrationTemplateVariables(String application, UserProvisioningUserResource userResponse,
      Map<String, Object> templateVariables) {
    setCommonTemplateVariables(userResponse, templateVariables);
    if (StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      setPortalTableTemplate(userResponse, templateVariables);
    } else if (StringUtils.equals(application, DataConstants.SKRILL)
        || StringUtils.equals(application, DataConstants.NETELLER)) {
      setSkrillTableTemplate(userResponse, templateVariables);
    }
  }

  /**
   * Utility method to put the permissions updated mail template variables.
   */
  public void setPermissionsUpdatedTemplateVariables(String application, UserProvisioningUserResource userResponse,
      Map<String, Object> templateVariables) {
    setCommonTemplateVariables(userResponse, templateVariables);
    if (StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      setPortalTableTemplate(userResponse, templateVariables);
    }
  }

  private void setCommonTemplateVariables(UserProvisioningUserResource userResponse,
      Map<String, Object> templateVariables) {
    String firstName = userResponse.getFirstName() != null ? userResponse.getFirstName() : StringUtils.EMPTY;
    String lastName = userResponse.getLastName() != null ? userResponse.getLastName() : StringUtils.EMPTY;
    templateVariables.put(DataConstants.CUSTOMER_NAME, firstName + " " + lastName);
    templateVariables.put(DataConstants.LOGIN_NAME, userResponse.getUserName());
    templateVariables.put("userEmail", userResponse.getEmail());
    templateVariables.put("name", userResponse.getFirstName());
    templateVariables.put("adminName", CommonThreadLocal.getAuthLocal().getUserName());
    templateVariables.put("date", new Date());
  }

  private void setPortalTableTemplate(UserProvisioningUserResource userResponse,
      Map<String, Object> templateVariables) {
    boolean hasAdminRole = false;
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (userResponse.getUserSummary() != null
        && CollectionUtils.isNotEmpty(userResponse.getUserSummary().getMerchants())) {
      List<Merchant> merchants = userResponse.getUserSummary().getMerchants();
      StringBuilder table = getPortalTable(userResponse);
      int size;
      if (merchants.size() > 3) {
        size = 3;
      } else {
        size = merchants.size();
      }
      for (int row = 0; row < size; row++) {
        if (StringUtils.containsIgnoreCase(merchants.get(row).getRole(),
            DataConstants.ADMIN)) {
          hasAdminRole = true;
        }
        setTemplateFeilds(row, userResponse, merchants, table, application);
      }
      if (!StringUtils.isNotBlank(userResponse.getOwnerType())
          || "ACCOUNT_GROUP".equals(userResponse.getOwnerType()) || "PARTNER_PORTAL".equals(application)) {
        replaceString(table, "{{FMA}}", StringUtils.EMPTY);
      } else {
        replaceString(table, "{{FMA}}", "Accounts (FMA's)");
      }

      if (merchants.size() > 3) {
        replaceString(table, "{{moreLink}}",
            "<a href='" + getLink(application, DataConstants.PERMISSIONS_UPDATED_MAIL) + "' " + LINK
                + MORE);
      } else {
        replaceString(table, "{{moreLink}}", StringUtils.EMPTY);
      }
      replaceString(table, TABLE_ROW, StringUtils.EMPTY);
      templateVariables.put("table", table);
      templateVariables.put("assignedMerchants", getMerchantsAssigned(merchants));
      templateVariables.put("modifiedMerchants", getMerchantsAssigned(merchants));
      templateVariables.put("adminRoleText", getAdminRoleText(hasAdminRole, DataConstants.PORTAL));
    }
  }

  private void setTemplateFeilds(int row, UserProvisioningUserResource userResponse, List<Merchant> merchants,
      StringBuilder table, String application) {


    String backgroundColor = null;
    if (row % 2 == 0) {
      backgroundColor = "#f5f5f5";
    } else {
      backgroundColor = "#ffffff";
    }
    StringBuilder tableRow = getPortalTableRow(userResponse);
    replaceString(tableRow, "{{backGroundColor}}", backgroundColor);
    if (merchants.get(row).getMerchantInfo() == null
        || merchants.get(row).getMerchantInfo().getMerchantId() == null
        || merchants.get(row).getMerchantInfo().getMerchantId().equals(StringUtils.EMPTY)) {
      replaceString(tableRow, "{{merchantId}}", StringUtils.EMPTY);
    } else {
      replaceString(tableRow, "{{merchantId}}", "(" + merchants.get(row).getMerchantInfo().getMerchantId() + ")");
    }
    if (merchants.get(row).getMerchantInfo() == null
        || merchants.get(row).getMerchantInfo().getMerchantName() == null) {
      replaceString(tableRow, "{{merchantName}}", StringUtils.EMPTY);
    } else {
      replaceString(tableRow, "{{merchantName}}", merchants.get(row).getMerchantInfo().getMerchantName());
    }
    Object ownerTypeObj = userResponse.getCustomProperties().get(DataConstants.OWNER_TYPE);
    String ownerType = ownerTypeObj != null ? ownerTypeObj.toString() : null;
    if (!StringUtils.equals(ownerType, DataConstants.PARTNER)) {
      replaceString(tableRow, "{{accounts}}", getMerchantAccounts(merchants.get(row), tableRow, application));
    }
    replaceString(tableRow, "{{role}}", merchants.get(row).getRole());

    replaceString(tableRow, "{{permissions}}",
        getPermissionsStringPortal(merchants.get(row), tableRow, application));
    tableRow.append(TABLE_ROW);
    replaceString(table, TABLE_ROW, tableRow.toString());
  }

  private String getMerchantAccounts(Merchant merchants, StringBuilder tableRow, String application) {
    StringBuilder accounts = new StringBuilder(StringUtils.EMPTY);
    int size = merchants.getAccounts().size();
    if ((StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL))) {

      if (merchants.getAccounts().size() > 3) {
        merchants.setAccounts(merchants.getAccounts().subList(0, 3));
        replaceString(tableRow, "{{moreLinkForAccounts}}",
            "<a href='" + getLink(application, DataConstants.PERMISSIONS_UPDATED_MAIL) + "' " + LINK + (size - 3)
                + MORE);
      } else {
        replaceString(tableRow, "{{moreLinkForAccounts}}", StringUtils.EMPTY);
      }


    }
    if (CollectionUtils.isNotEmpty(merchants.getAccounts())) {
      accounts.append(merchants.getAccounts());
      replaceString(accounts, "[", StringUtils.EMPTY);
      replaceString(accounts, "]", StringUtils.EMPTY);
    }
    return accounts.toString();
  }

  private String getPermissionsStringPortal(Merchant merchants, StringBuilder tableRow, String application) {
    StringBuilder permissions = new StringBuilder(StringUtils.EMPTY);
    int size = merchants.getPermissions().size();
    if (StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      if (merchants.getPermissions().size() > 3) {
        merchants.setPermissions(merchants.getPermissions().subList(0, 3));
        replaceString(tableRow, "{{moreLink}}",
            "<a href='" + getLink(application, DataConstants.PERMISSIONS_UPDATED_MAIL) + "' " + LINK + (size - 3)
                + MORE);
      } else {
        replaceString(tableRow, "{{moreLink}}", StringUtils.EMPTY);
      }
    }

    if (CollectionUtils.isNotEmpty(merchants.getPermissions())) {
      permissions.append(merchants.getPermissions());
      replaceString(permissions, "[", StringUtils.EMPTY);
      replaceString(permissions, "]", StringUtils.EMPTY);
    }
    return permissions.toString();
  }

  private String getMerchantsAssigned(List<Merchant> merchants) {
    StringBuilder merchantsAssigned = new StringBuilder(StringUtils.EMPTY);
    int length = merchants.size();
    if (length > 3) {
      length = 3;
    } else {
      length = merchants.size();
    }
    for (int idx = 0; idx < length; idx++) {
      String merchantId = merchants.get(idx).getMerchantInfo().getMerchantId();
      String merchantName = merchants.get(idx).getMerchantInfo().getMerchantName();
      if (StringUtils.isNotBlank(merchantId)) {
        merchantsAssigned.append(merchantName).append("(").append(merchantId).append(")");
      } else {
        merchantsAssigned.append(merchantName);
      }

      if (idx < merchants.size()) {
        merchantsAssigned.append(", ");
      } else if (idx == merchants.size() - 2) {
        merchantsAssigned.append("& ");
      }
    }
    if (merchants.size() > 3) {
      merchantsAssigned.append("<a href='" + getLink("PORTAL", DataConstants.PERMISSIONS_UPDATED_MAIL) + "' " + LINK
          + MORE);
    }
    return merchantsAssigned.toString();
  }

  /**
   * this method returns the eventId based on application.
   */
  public String getEventId(UserProvisioningUserResource userResponse, String application, String mailType) {
    String eventIdKey = StringUtils.EMPTY;
    if (StringUtils.equals(mailType, DataConstants.REGISTRATION_MAIL)) {
      eventIdKey = DataConstants.ACTIVATION_EMAIL_EVENTID;
    } else if (StringUtils.equals(mailType, DataConstants.PERMISSIONS_UPDATED_MAIL)) {
      eventIdKey = DataConstants.PERMISSIONS_UPDATED_EMAIL_EVENTID;
    }
    String eventId = StringUtils.EMPTY;
    if (StringUtils.equals(application, DataConstants.PORTAL)) {
      Object ownerTypeObj = userResponse.getCustomProperties().get(DataConstants.OWNER_TYPE);
      String ownerType = ownerTypeObj != null ? ownerTypeObj.toString() : null;
      if (StringUtils.equals(ownerType, DataConstants.PARTNER)) {
        eventId = userProvisioningConfig.getUser().getGcsEvents().getPartnerPortal().get(eventIdKey);
      } else {
        eventId = userProvisioningConfig.getUser().getGcsEvents().getBusinessPortal().get(eventIdKey);
      }
    } else if (StringUtils.equals(application, DataConstants.SKRILL)) {
      eventId = userProvisioningConfig.getUser().getGcsEvents().getSkrill().get(eventIdKey);
    } else if (StringUtils.equals(application, DataConstants.NETELLER)) {
      eventId = userProvisioningConfig.getUser().getGcsEvents().getNeteller().get(eventIdKey);
    } else if (StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      eventId = userProvisioningConfig.getUser().getGcsEvents().getPartnerPortal().get(eventIdKey);
    }
    return eventId;
  }

  /**
   * this method returns the mfa eventId.
   */
  public String getEventIdForMfa(String eventIdKey) {
    return userProvisioningConfig.getUser().getGcsEvents().getBusinessPortal().get(eventIdKey);
  }

  /**
   * this method returns the eventId based on application.
   */
  public String getLink(String application, String mailType) {
    String link = StringUtils.EMPTY;
    String extension = StringUtils.EMPTY;
    if (StringUtils.equals(mailType, DataConstants.REGISTRATION_MAIL)) {
      extension = DataConstants.ACTIVATE_URI;
    } else if (StringUtils.equals(mailType, DataConstants.PERMISSIONS_UPDATED_MAIL)) {
      extension = DataConstants.LOGIN_URI;
    }
    if (StringUtils.equals(application, DataConstants.PORTAL)) {
      link = userProvisioningConfig.getUiHostUrl() + extension;
    } else if (StringUtils.equals(application, DataConstants.SKRILL)) {
      link = userProvisioningConfig.getSkrillHostUrl() + extension;
    } else if (StringUtils.equals(application, DataConstants.NETELLER)) {
      link = userProvisioningConfig.getNetellerHostUrl() + extension;
    } else if (StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      link = userProvisioningConfig.getPartnerPortalHostUrl() + extension;
    }
    return link;
  }

  /**
   * Utility method to get the skrill registration table.
   */
  private void setSkrillTableTemplate(UserProvisioningUserResource userResponse,
      Map<String, Object> templateVariables) {
    List<AccessResources> accessResources = userResponse.getAccessResources();
    boolean hasAdminRole = false;
    if (userResponse.getUserSummary() != null
        && CollectionUtils.isNotEmpty(userResponse.getUserSummary().getMerchants())) {
      List<Merchant> merchants = userResponse.getUserSummary().getMerchants();
      StringBuilder table = new StringBuilder(getHtmlString(SKRILL_REGISTARTION_EMAIL_TABLE_PATH));
      String evenNumberRowColor = null;
      String application = CommonThreadLocal.getAuthLocal().getApplication();
      if (StringUtils.equals(application, DataConstants.SKRILL)) {
        evenNumberRowColor = "#FCF4F4";
      } else if (StringUtils.equals(application, DataConstants.NETELLER)) {
        evenNumberRowColor = "#F6FBF1";
      }
      for (int row = 0; row < merchants.size(); row++) {
        String backgroundColor = null;
        if (row % 2 == 0) {
          backgroundColor = evenNumberRowColor;
        } else {
          backgroundColor = "#ffffff";
        }
        StringBuilder tableRow = new StringBuilder(getHtmlString(SKRILL_REGISTARTION_EMAIL_TABLE_ROW_PATH));
        replaceString(tableRow, "{{backGroundColor}}", backgroundColor);
        replaceString(tableRow, "{{walletId}}", merchants.get(row).getMerchantInfo().getMerchantId());
        replaceString(tableRow, "{{walletName}}", getWalletName(merchants.get(row)));
        replaceString(tableRow, "{{role}}", merchants.get(row).getRole());
        if (StringUtils.containsIgnoreCase(merchants.get(row).getRole(), DataConstants.ADMIN)) {
          hasAdminRole = true;
        }
        replaceString(tableRow, "{{permissions}}",
            getPermissionsStringPortal(merchants.get(row), tableRow, application));
        tableRow.append(TABLE_ROW);
        replaceString(table, TABLE_ROW, tableRow.toString());
      }
      replaceString(table, TABLE_ROW, StringUtils.EMPTY);
      templateVariables.put("table", table);
    } else {
      templateVariables.put("table", StringUtils.EMPTY);
    }
    templateVariables.put("customerIds", getCustomerIds(accessResources));
    templateVariables.put("adminRoleText", getAdminRoleText(hasAdminRole, DataConstants.SKRILL));
    templateVariables.put("administratorGuideText", getAdministratorGuideText(hasAdminRole));
    checkAndSetSkrillBrandsVariables(templateVariables, userResponse);
    String firstName = userResponse.getFirstName() != null ? userResponse.getFirstName() : StringUtils.EMPTY;
    String lastName = userResponse.getLastName() != null ? userResponse.getLastName() : StringUtils.EMPTY;
    templateVariables.put(DataConstants.CUSTOMER_NAME,
        WordUtils.capitalizeFully(firstName + StringUtils.SPACE + lastName));
  }

  private void checkAndSetSkrillBrandsVariables(Map<String, Object> templateVariables,
      UserProvisioningUserResource userResponse) {
    if (StringUtils.equals(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.SKRILL)) {
      templateVariables.put("merchantSupportContactMail",
          getSkrillMerchantSupportContactMailByBusninessUnit(userResponse));
      templateVariables.put("senderName", getSkrillSenderNameByBusninessUnit(userResponse));
    }
  }

  private String getSkrillSenderNameByBusninessUnit(UserProvisioningUserResource userResponse) {
    String senderName = StringUtils.EMPTY;
    if (MapUtils.isNotEmpty(userResponse.getCustomProperties())) {
      String businessUnit = userResponse.getCustomProperties().get(DataConstants.BUSINESS_UNIT) != null
          ? userResponse.getCustomProperties().get(DataConstants.BUSINESS_UNIT).toString()
          : DataConstants.SKRILL;

      BusinessUnitConfig businessUnitConfig = skrillTellerConfig.getBusinessUnits().get(businessUnit.toLowerCase());
      if (businessUnitConfig != null) {
        senderName = businessUnitConfig.getEmailSenderName();
      }
    }
    return senderName;
  }

  private String getSkrillMerchantSupportContactMailByBusninessUnit(UserProvisioningUserResource userResponse) {
    String merchantSupportContactMail = StringUtils.EMPTY;
    if (MapUtils.isNotEmpty(userResponse.getCustomProperties())) {
      String businessUnit = userResponse.getCustomProperties().get(DataConstants.BUSINESS_UNIT) != null
          ? userResponse.getCustomProperties().get(DataConstants.BUSINESS_UNIT).toString()
          : DataConstants.SKRILL;

      BusinessUnitConfig businessUnitConfig = skrillTellerConfig.getBusinessUnits().get(businessUnit.toLowerCase());
      if (businessUnitConfig != null) {
        merchantSupportContactMail = businessUnitConfig.getContactEmail();
      }
    }
    return merchantSupportContactMail;
  }

  private String getWalletName(Merchant merchant) {
    return merchant.getMerchantInfo().getMerchantName() == null ? StringUtils.EMPTY
        : merchant.getMerchantInfo().getMerchantName();
  }

  private StringBuilder replaceString(StringBuilder data, String target, String replacement) {
    int startIdx = data.indexOf(target);
    int endIdx = startIdx + target.length();
    return data.replace(startIdx, endIdx, replacement);
  }

  private String getCustomerIds(List<AccessResources> accessResources) {
    StringBuilder customerIds = new StringBuilder(StringUtils.EMPTY);
    if (CollectionUtils.isNotEmpty(accessResources)) {
      customerIds.append(accessResources.stream().map(AccessResources::getId).collect(Collectors.toList()).toString());
      replaceString(customerIds, "[", StringUtils.EMPTY);
      replaceString(customerIds, "]", StringUtils.EMPTY);
    }
    return customerIds.toString();
  }

  private String getAdminRoleText(boolean hasAdminRole, String application) {
    String adminRoleText = StringUtils.EMPTY;
    if (hasAdminRole) {
      if (StringUtils.equals(DataConstants.SKRILL, application)) {
        adminRoleText = SKRILL_ADMIN_ROLE_TEXT;
      } else if (StringUtils.equals(DataConstants.PORTAL, application)) {
        adminRoleText = PORTAL_ADMIN_ROLE_TEXT;
      }
    }
    return adminRoleText;
  }

  private String getAdministratorGuideText(boolean hasAdminRole) {
    String administratorGuideText = StringUtils.EMPTY;
    if (hasAdminRole) {
      String application = CommonThreadLocal.getAuthLocal().getApplication();
      if (StringUtils.equals(application, DataConstants.SKRILL)) {
        administratorGuideText = SKRILL_ADMINISTRATOR_GUIDE_TEXT;
      } else if (StringUtils.equals(application, DataConstants.NETELLER)) {
        administratorGuideText = NETELLER_ADMINISTRATOR_GUIDE_TEXT;
      }
    }
    return administratorGuideText;
  }

  private String getHtmlString(String filePath) {
    String htmlData = StringUtils.EMPTY;
    try (InputStream inputStream = resourceLoader.getResource("classpath:" + filePath).getInputStream()) {
      htmlData = IOUtils.toString(inputStream);
    } catch (IOException e) {
      LOGGER.error("Unable to read html file from the path {0} and exception is: {1}", filePath, e);
      throw InternalErrorException.builder().cause(e).details("Error in reading the html template file").build();
    }
    return htmlData;
  }

  private String getOwnerType(UserProvisioningUserResource userResponse) {
    Object ownerTypeObj = userResponse.getCustomProperties().get(DataConstants.OWNER_TYPE);
    return ownerTypeObj != null ? ownerTypeObj.toString() : null;
  }

  private StringBuilder getPortalTable(UserProvisioningUserResource userResponse) {
    StringBuilder table;
    if (StringUtils.equals(getOwnerType(userResponse), DataConstants.PARTNER)) {
      table = new StringBuilder(getHtmlString(PARTNER_PORTAL_REGISTARTION_EMAIL_TABLE_PATH));
    } else {
      table = new StringBuilder(getHtmlString(BUSINESS_PORTAL_REGISTARTION_EMAIL_TABLE_PATH));
    }
    return table;
  }

  private StringBuilder getPortalTableRow(UserProvisioningUserResource userResponse) {
    StringBuilder tableRow;
    if (StringUtils.equals(getOwnerType(userResponse), DataConstants.PARTNER)) {
      tableRow = new StringBuilder(getHtmlString(PARTNER_PORTAL_REGISTARTION_EMAIL_TABLE_ROW_PATH));
    } else {
      tableRow = new StringBuilder(getHtmlString(BUSINESS_PORTAL_REGISTARTION_EMAIL_TABLE_ROW_PATH));
    }
    return tableRow;
  }
}
