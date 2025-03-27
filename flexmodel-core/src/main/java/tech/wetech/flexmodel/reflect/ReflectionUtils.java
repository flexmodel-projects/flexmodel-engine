package tech.wetech.flexmodel.reflect;

import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.annotation.ModelField;
import tech.wetech.flexmodel.annotation.ModelName;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
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
  private <T> T toClassBean(JsonObjectConverter converter, Object obj, Class<T> cls) {
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
      return converter.convertValue(result, (Class<T>) obj.getClass());
    }
    return converter.convertValue(obj, (Class<T>) obj.getClass());
  }

  @SuppressWarnings("all")
  public static Map<String, Object> toMap(JsonObjectConverter converter, Object obj) {
    if (obj instanceof Map) {
      return (Map<String, Object>) obj;
    }
    Class<?> aClass = obj.getClass();
    Map<String, Object> result = new HashMap<>(converter.convertValue(obj, Map.class));
    if (aClass.getAnnotation(ModelName.class) != null) {
      Map<String, Object> newResult = new HashMap<>();
      result.forEach((k, v) -> {
        String fieldName = k;
        try {
          Field aClassField = aClass.getDeclaredField(k);
          ModelField modelFieldAnnotation = aClassField.getAnnotation(ModelField.class);
          if (modelFieldAnnotation != null) {
            fieldName = modelFieldAnnotation.value();
          }
        } catch (NoSuchFieldException e) {
        } finally {
          newResult.put(fieldName, v);
        }
      });
      return newResult;
    }
    return result;
  }


}
