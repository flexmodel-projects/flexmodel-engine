package tech.wetech.flexmodel.reflect;

import java.beans.Introspector;
import java.lang.reflect.Method;

/**
 * @author cjbi
 */
public class ReflectionUtils {

  public static String toUpperCamelCase(String str) {
    char[] cs = str.toCharArray();
    cs[0] -= 32;
    return String.valueOf(cs);
  }

  public static String getFieldNameFromGetter(Method method) {
    String attr;
    if (method.getName().startsWith("get")) {
      attr = method.getName().substring(3);
    } else {
      attr = method.getName().substring(2);
    }
    return Introspector.decapitalize(attr);
  }

}
