// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.Predicate;

@Component
public class UserSpecification {

  /**
   * Constructs spec for fetching users from the request resource.
   * 
   * @param userFetchByFiltersRequestDto {@link UserFetchByFiltersRequestDto}
   * @return UserSpecification
   */
  public Specification<User> constructPortalUsersSpecification(
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto) {
    return (root, query, cb) -> {

      Predicate applicationPredicate = cb.conjunction();
      Predicate userIdentifierPredicate = cb.conjunction();
      Predicate loginNamePredicate = cb.conjunction();
      Predicate emailPredicate = cb.conjunction();
      Predicate statusPredicate = cb.conjunction();
      Predicate createdByPredicate = cb.conjunction();
      Predicate createdDatePredicate = cb.conjunction();
      Predicate loginNamesPredicate = cb.conjunction();
      Predicate ownerTypePredicate = cb.conjunction();

      if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getApplication())) {
        if ("PORTAL".equalsIgnoreCase(userFetchByFiltersRequestDto.getApplication())
            && !userFetchByFiltersRequestDto.isMerchantTypeValidation()) {
          Predicate predicateForPortal = cb.equal(root.get(DataConstants.APPLICATION), DataConstants.PORTAL);
          Predicate predicateForPartnerPortal =
              cb.equal(root.get(DataConstants.APPLICATION), DataConstants.PARTNER_PORTAL);
          applicationPredicate = cb.or(predicateForPortal, predicateForPartnerPortal);
        } else {
          applicationPredicate.getExpressions()
              .add(cb.equal(root.get(DataConstants.APPLICATION), userFetchByFiltersRequestDto.getApplication()));
        }
      }
      if (userFetchByFiltersRequestDto.getUserIdentifier() != null) {
        loginNamePredicate.getExpressions().add(cb.like(cb.upper(root.get("loginName")),
            "%" + userFetchByFiltersRequestDto.getUserIdentifier().toUpperCase() + "%"));
        emailPredicate.getExpressions().add(cb.like(cb.upper(root.get("email")),
            "%" + userFetchByFiltersRequestDto.getUserIdentifier().toUpperCase() + "%"));
        userIdentifierPredicate = cb.or(loginNamePredicate, emailPredicate);
      }

      if (userFetchByFiltersRequestDto.getStatus() != null) {
        statusPredicate.getExpressions().add(cb.equal(root.get("status"), userFetchByFiltersRequestDto.getStatus()));
      }
      if (userFetchByFiltersRequestDto.getCreatedBy() != null) {
        createdByPredicate.getExpressions().add(cb.like(cb.lower(root.get("createdBy")),
            "%" + userFetchByFiltersRequestDto.getCreatedBy().toLowerCase(Locale.ENGLISH) + "%"));
      }
      if (userFetchByFiltersRequestDto.getLoginNames() != null) {
        loginNamesPredicate.getExpressions()
            .add(cb.in(root.get("loginName")).value(userFetchByFiltersRequestDto.getLoginNames()));
      }
      if (userFetchByFiltersRequestDto.getCreatedDate() != null) {
        DateTime dateTime = userFetchByFiltersRequestDto.getCreatedDate();
        createdDatePredicate.getExpressions().add(cb.between(root.get("createdDate"), dateTime.withTimeAtStartOfDay(),
            dateTime.plusDays(1).withTimeAtStartOfDay()));
      }
      OwnerType userType = userFetchByFiltersRequestDto.getUserType();
      if (userType != null) {
        if (userType.equals(OwnerType.MERCHANT)) {
          List<String> merchantTypes = new ArrayList<>(Arrays.asList(DataConstants.PMLE, DataConstants.MLE));
          ownerTypePredicate.getExpressions().add(cb.in(root.get("ownerType")).value(merchantTypes));
        } else {
          ownerTypePredicate.getExpressions().add(cb.equal(root.get("ownerType"), userType.name()));
        }
      }
      return cb.and(applicationPredicate, statusPredicate, createdByPredicate, createdDatePredicate,
          userIdentifierPredicate, loginNamesPredicate, ownerTypePredicate);

    };
  }

}
