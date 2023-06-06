// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.junit.Assert.assertTrue;

import com.paysafe.upf.user.provisioning.domain.HashedPasswordEntity;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.utils.PasswordEncryptionUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PasswordEncryptionUtilTest {

  private static final String ALGORITHM_SHA_512 = "SHA-512";
  private static final String ALGORITHM_MD5 = "MD5";
  private static final String ALGORITHM_INCORRECT = "ABC";
  private static final String SALT = "ZGE0MzRlcjR3dDQ1NDUyNDMxMQ==";
  private static final String PLAIN_PASSWORD = "Paysafe@123";
  private static final String HASHED_PASSWORD_SHA_512_BASE64 =
          "4Fgpqgnarb3jCROwpLI2OqTD7Q78ctltEMq9VuoQg/FOeRUVRoSUmm/2FTtp3hHlgNOW0djxMdvgytG39tPqYA==";
  private static final String HASHED_PASSWORD_MD5_BASE64 =
          "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASWRKNXhJeIaQdJpVjT5lGQ==";
  private static final String APPLICATION_SKRILL = "SKRILL";
  private static final String APPLICATION_NETELLER = "NETELLER";

  @InjectMocks
  PasswordEncryptionUtil passwordEncryptionUtil;

  @Test
  public void compareHashedPasswords_success_neteller() {

    HashedPasswordEntity hashedPassword = new HashedPasswordEntity();
    hashedPassword.setAlgorithm(ALGORITHM_SHA_512);
    hashedPassword.setSalt(SALT);
    hashedPassword.setValue(HASHED_PASSWORD_SHA_512_BASE64);

    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication(APPLICATION_NETELLER);
    CommonThreadLocal.setAuthLocal(authorizationInfo);

    boolean matched = passwordEncryptionUtil.compareHashedPasswords(PLAIN_PASSWORD, hashedPassword);
    assertTrue(matched);
  }

  @Test
  public void compareHashedPasswords_success_others() {
    HashedPasswordEntity hashedPassword = new HashedPasswordEntity();
    hashedPassword.setAlgorithm(ALGORITHM_MD5);
    hashedPassword.setSalt(SALT);
    hashedPassword.setValue(HASHED_PASSWORD_MD5_BASE64);

    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication(APPLICATION_SKRILL);
    CommonThreadLocal.setAuthLocal(authorizationInfo);

    boolean matched = passwordEncryptionUtil.compareHashedPasswords(PLAIN_PASSWORD, hashedPassword);
    assertTrue(matched);
  }

  @Test(expected = AssertionError.class)
  public void getUpdatedMessageDigestWithBytes_algorithm_failure() {
    HashedPasswordEntity hashedPassword = new HashedPasswordEntity();
    hashedPassword.setAlgorithm(ALGORITHM_INCORRECT);
    hashedPassword.setSalt(SALT);
    hashedPassword.setValue(HASHED_PASSWORD_MD5_BASE64);

    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication(APPLICATION_SKRILL);
    CommonThreadLocal.setAuthLocal(authorizationInfo);

    passwordEncryptionUtil.compareHashedPasswords(PLAIN_PASSWORD, hashedPassword);

  }
}
