// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import com.paysafe.upf.user.provisioning.config.DynamicBrandsConfig;
import com.paysafe.upf.user.provisioning.web.rest.resource.BrandPermissionsMapping;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

public class DynamicBrandServiceImplTest {

  @InjectMocks
  private DynamicBrandsServiceImpl dynamicBrandServiceImpl;

  @Mock
  private DynamicBrandsConfig dynamicBrandsConfig;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testDynamicBrands() {
    doReturn(new ArrayList<BrandPermissionsMapping>()).when(dynamicBrandsConfig).getBrands();
    assertNotNull(dynamicBrandServiceImpl.fetchBrands());
  }
}
