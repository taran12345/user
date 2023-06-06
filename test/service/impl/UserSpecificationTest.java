// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;

import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.repository.UserSpecification;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserSpecificationTest {

  @InjectMocks
  private UserSpecification userSpecification;

  @Mock
  private Root<User> root;

  @Mock
  private CriteriaQuery<?> query;

  @Mock
  private CriteriaBuilder cb;

  @Mock
  private Predicate predicate;

  @Spy
  private CriteriaBuilder.In<?> criteriaBuilderIn;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void constructPortalUsersSpecification_withValidInput_shouldSucceed() {
    UserFetchByFiltersRequestDto userFetchByFiltersRequestDto =
        UserFetchByFiltersRequestDto.builder().application(DataConstants.PORTAL).loginNames(Arrays.asList("abc12"))
            .createdBy("testUser1").resourceId("1233").resourceType(DataConstants.PMLE).build();

    Specification<User> usersSpecification =
        userSpecification.constructPortalUsersSpecification(userFetchByFiltersRequestDto);
    assertNotNull(usersSpecification);
  }
}
