// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.upf.user.provisioning.domain.HashedPasswordEntity;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

@Component
public class PasswordEncryptionUtil {

  private static final int SIG_NUM = 1;
  private static final String HEX_128_FORMAT = "%0128x";
  private static final String NETELLER = "NETELLER";

  /**
   * Converts the given plain password as per the details of the HashedPassword and
   * then compares the two
   * @param plainPassword PlainPassword
   * @param hashedPassword HashedPasswordEntity
   * @return whether the passwords matched.
   */
  public boolean compareHashedPasswords(String plainPassword, HashedPasswordEntity hashedPassword) {

    String encoding;
    if (CommonThreadLocal.getAuthLocal().getApplication().equalsIgnoreCase(NETELLER)) {
      encoding = StandardCharsets.UTF_16LE.name();
    } else {
      encoding = StandardCharsets.UTF_8.name();
    }

    String salt = new String(Base64.getDecoder().decode(hashedPassword.getSalt()), StandardCharsets.UTF_8);
    String algorithm = hashedPassword.getAlgorithm();
    String encryptedData = hashedPassword.getValue();

    String hash = sha512SaltHex(salt, plainPassword, encoding, algorithm);

    return hexToBase64(hash).equalsIgnoreCase(encryptedData);

  }

  private String sha512SaltHex(String salt, String data, String encoding, String algorithm) {
    try {
      byte[] saltBytes = salt.getBytes(encoding);
      byte[] byteData = data.getBytes(encoding);

      MessageDigest sha512Digest = getUpdatedMessageDigestWithBytes(saltBytes, algorithm);
      byte[] bytes = sha512Digest.digest(byteData);
      return String.format(Locale.ROOT, HEX_128_FORMAT, new BigInteger(SIG_NUM, bytes));
    } catch (UnsupportedEncodingException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }
  }

  private MessageDigest getUpdatedMessageDigestWithBytes(byte[] saltBytes, String algorithm) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException ex) {
      throw new AssertionError(ex);
    }
    messageDigest.reset();
    messageDigest.update(saltBytes);
    return messageDigest;
  }

  /**
   * TODO: to be updated.
   * @param inputString String
   * @return encoded val
   */
  public String hexToBase64(String inputString) {
    byte[] val = new byte[inputString.length() / 2];
    for (int i = 0; i < val.length; i++) {
      int index = i * 2;
      val[i] = (byte) Integer.parseInt(inputString.substring(index, index + 2), 16);
    }
    return Base64.getEncoder().encodeToString(val);
  }
}
