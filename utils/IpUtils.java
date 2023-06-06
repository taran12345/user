// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

public class IpUtils {
  private static final String COMMA_LIST_REGEX_PATTERN = "\\s*,\\s*";
  private static final String IPV4_ADDRESS_REGEX_PATTERN = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
  private static final String CIDR_RANGE_REGEX_PATTERN = "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})/(\\d{1,2})$";
  private static final String[] UNROUTABLE_NETWORKS = {"10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"};

  private IpUtils() {
    // hide utility class constructor
  }

  /**
   * Gets the client ip address.
   */
  public static String getRemoteAddress(HttpServletRequest request) {
    if (request == null) {
      return "offline";
    }

    String trueClientIp = request.getHeader("True-Client-IP");

    if (trueClientIp != null && trueClientIp.length() > 0) {
      return trueClientIp;
    }

    // Parse Forwarded header. Examples
    // for=192.0.2.60;proto=http;by=203.0.113.43
    // for=192.0.2.43, for=192.0.2.43;proto=http
    String forwarded = request.getHeader("Forwarded");
    if (forwarded != null && forwarded.length() > 0) {
      forwarded = forwarded.replace(" ", "").toLowerCase();
      int start = forwarded.indexOf("for=");
      if (start >= 0) {
        // Look for either a semicolon or comma to mark the end of the IP address
        int endSemi = forwarded.indexOf(';', start);
        int endComma = forwarded.indexOf(',', start);
        if (endComma > 5 && (endComma < endSemi || endSemi == 0)) {
          return forwarded.substring(start + 4, endComma);
        }
        if (endSemi > 5) {
          return forwarded.substring(start + 4, endSemi);
        }
      }
    }

    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && forwardedFor.length() > 0) {
      Pattern pattern = Pattern.compile(COMMA_LIST_REGEX_PATTERN);
      return getFirstRoutableAddress(pattern.split(forwardedFor.trim()));
    }

    String remoteAddr = request.getHeader("X-IP-From");
    if (remoteAddr != null && remoteAddr.length() > 0) {
      return remoteAddr;
    }

    return request.getRemoteAddr();
  }

  /**
   * Gets the first routable address.
   */
  public static String getFirstRoutableAddress(String[] addresses) {
    // Don't check for array with no elements.
    if ((addresses == null) || (addresses.length == 0)) {
      return null;
    }

    List<String> netList = Arrays.asList(UNROUTABLE_NETWORKS);
    for (String addr : addresses) {
      // Return address if it does not match unroutable networks.
      Long numAddr = parseInet4Address(addr);
      if (!(numAddr == null || cidrLookup(numAddr, netList))) {
        return addr;
      }
    }
    return addresses[0];
  }

  /**
   * Parses the given string as literal IPv4 address (<samp>x.x.x.x</samp>) and returns a 32-bit number representing it.
   * Note that the returned value should be treated as unsigned integer, for example <samp> Long.toString(ipNum &
   * 0xFFFFFFFFL) </samp>
   *
   * @param addr an IPv4 address literal.
   * @return <small>The numeric value of the given IP address, or <code>null</code> if the given string could not be
   * parsed as IPv4 literal</small>
   */
  public static Long parseInet4Address(String addr) {
    Pattern pattern = Pattern.compile(IPV4_ADDRESS_REGEX_PATTERN);
    Matcher matcher = pattern.matcher(addr);
    if (matcher.matches()) {
      long numAddr = 0;
      for (int i = 0; i < 4; i++) {
        try {
          int number = Integer.parseInt(matcher.group(i + 1));
          if (number > 255) {
            return null; // not valid
          }
          numAddr = numAddr << 8 | number;
        } catch (NumberFormatException ex) {
          return null; // not valid
        }
      }
      return numAddr;
    }
    return null;
  }

  /**
   * cidr loopup.
   */
  public static boolean cidrLookup(long addr, List<String> ranges) {
    for (String cidr : ranges) {
      Pattern pattern = Pattern.compile(CIDR_RANGE_REGEX_PATTERN);
      Matcher matcher = pattern.matcher(cidr);
      if (matcher.matches()) {
        Long netAddr = parseInet4Address(matcher.group(1));
        if (netAddr == null) {
          continue; // not valid
        }
        int netMask;
        try {
          netMask = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException ex) {
          continue; // not valid
        }
        if (netMask > 32) {
          continue; // not valid
        }
        netMask = 0xFFFFFFFF << (32 - netMask);
        if ((netAddr & netMask) == (addr & netMask)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks the ip address in range or not.
   */
  public static boolean isIpAddressInRange(String addr, List<String> ranges) {
    // check ip syntax
    Long numAddr = parseInet4Address(addr);
    if (numAddr == null) {
      return false;
    }

    // check if ip is set in the specified var
    if (ranges.contains(addr)) {
      return true;
    }

    // check if ip is in the the specified var ranges
    return cidrLookup(numAddr, ranges);
  }

}
