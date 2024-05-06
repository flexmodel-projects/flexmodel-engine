package tech.wetech.flexmodel.sql;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author cjbi
 */
public class StringHelper {

  public static String qualify(String prefix, String name) {
    if (name == null || prefix == null) {
      throw new NullPointerException("prefix or name were null attempting to build qualified name");
    }
    return prefix + '.' + name;
  }

  public static String replaceOnce(String template, String placeholder, String replacement) {
    if (template == null) {
      return null;  // returning null!
    }
    int loc = template.indexOf(placeholder);
    if (loc < 0) {
      return template;
    } else {
      return template.substring(0, loc) + replacement + template.substring(loc + placeholder.length());
    }
  }

  public static boolean isNotEmpty(String string) {
    return string != null && string.length() > 0;
  }

  public static String hashedName(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.reset();
      md.update(s.getBytes());
      byte[] digest = md.digest();
      BigInteger bigInt = new BigInteger(1, digest);
      // By converting to base 35 (full alphanumeric), we guarantee
      // that the length of the name will always be smaller than the 30
      // character identifier restriction enforced by a few dialects.
      return bigInt.toString(35);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to generate a hashed Constraint name!");
    }
  }
}
