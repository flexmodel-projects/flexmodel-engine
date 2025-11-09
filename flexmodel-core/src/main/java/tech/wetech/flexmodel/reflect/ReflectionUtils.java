package tech.wetech.flexmodel.reflect;

import tech.wetech.flexmodel.annotation.ModelClass;

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

  /**
   * 从实体类获取模型名称
   */
  public static String getModelNameFromClass(Class<?> entityClass) {
    ModelClass modelClass = entityClass.getAnnotation(ModelClass.class);
    if (modelClass != null) {
      return modelClass.value();
    }
    return entityClass.getSimpleName();
  }


}
