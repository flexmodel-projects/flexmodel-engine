package tech.wetech.flexmodel.reflect;

import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.annotation.ModelField;
import tech.wetech.flexmodel.annotation.ModelName;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  @SuppressWarnings("all")
  public static <T> T toClassBean(JsonObjectConverter converter, Object obj, Class<T> cls) {
    if (obj == null) {
      return null;
    }
    if (cls.getAnnotation(ModelName.class) != null) {
      Map<String, String> bindFields = new HashMap<>();
      for (Field field : cls.getDeclaredFields()) {
        ModelField modelFieldAnnotation = field.getAnnotation(ModelField.class);
        bindFields.put(modelFieldAnnotation != null ? modelFieldAnnotation.value() : field.getName(), field.getName());
      }
      Map<String, Object> originValue = converter.convertValue(obj, Map.class);
      Map<String, Object> result = new HashMap<>();
      originValue.forEach((k, v) -> {
        String fieldName = bindFields.get(k);
        result.put(fieldName, v);
      });
      return converter.convertValue(result, cls);
    }
    return converter.convertValue(obj, cls);
  }

  @SuppressWarnings("all")
  public static <T> List<T> toClassBeanList(JsonObjectConverter converter, List list, Class<T> cls) {
    List<T> result = new ArrayList<>();
    for (Object o : list) {
      result.add(toClassBean(converter, o, cls));
    }
    return result;
  }

}
