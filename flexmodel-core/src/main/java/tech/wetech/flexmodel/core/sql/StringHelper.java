package tech.wetech.flexmodel.core.sql;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author cjbi
 */
public class StringHelper {


  public static String replacePlaceholder(String str) {
    return str.replaceAll("\\$\\{([^}]+)\\}", ":$1");
  }
  public static String simpleRenderTemplate(String template, Map<?, ?> attributes) {
    int length = template.length();
    for (int i = 0; i < length; i++) {
      if (template.charAt(i) == '$') {
        if (length > i + 1) {
          int j = i;
          char c = template.charAt(++j);
          if (c == '{') {
            template = simpleRenderTemplate(template, length, ++j, attributes);
            length = template.length();
          }
        }
      }
    }
    return template;
  }

  private static String simpleRenderTemplate(String template, int length, int i, Map<?, ?> attributes) {
    StringBuilder valueBuilder = new StringBuilder();
    int endIndex = i - 2;
    label:
    for (; i < length; i++) {
      char c1 = template.charAt(i);
      switch (c1) {
        case ' ':
          continue;
        case '}':
          break label;
        default:
          valueBuilder.append(c1);
      }
    }
    String keyString = valueBuilder.toString();
    Object value = attributes;
    if (attributes.get(keyString) instanceof String) {
      value = attributes.get(keyString);
    } else {
      String[] keys = keyString.split("\\.");
      for (String key : keys) {
        if (value instanceof Map) {
          value = ((Map<?, ?>) value).get(key);
        } else {
          value = null;
        }
      }
    }
    return template.substring(0, endIndex) + value + template.substring(++i);
  }

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
