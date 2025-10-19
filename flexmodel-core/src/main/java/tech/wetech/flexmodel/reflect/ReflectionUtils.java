package tech.wetech.flexmodel.reflect;

import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.annotation.ModelField;

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

  public static void setFieldValue(Object obj, String fieldName, Object value) {
    try {
      // 构造setter方法名
      String setterName = "set" + toUpperCamelCase(fieldName);
      // 获取setter方法
      Method setter = null;
      Class<?> clazz = obj.getClass();

      // 查找setter方法
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
          setter = method;
          break;
        }
      }

      // 如果没找到，尝试在父类中查找
      if (setter == null) {
        for (Method method : clazz.getMethods()) {
          if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
            setter = method;
            break;
          }
        }
      }

      if (setter != null) {

        setter.setAccessible(true);
        setter.invoke(obj, value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

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

  @SuppressWarnings("all")
  public static <T> T toClassBean(JsonObjectConverter converter, Object obj, Class<T> cls) {
    if (obj == null) {
      return null;
    }
    if (cls.getAnnotation(ModelClass.class) != null) {
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
    Class<?> objClass = findModelClass(obj.getClass());
    if (objClass != null) {
      Map<String, String> bindFields = new HashMap<>();
      for (Field field : objClass.getDeclaredFields()) {
        ModelField modelFieldAnnotation = field.getAnnotation(ModelField.class);
        bindFields.put(field.getName(), modelFieldAnnotation != null ? modelFieldAnnotation.value() : field.getName());
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

  public static Class<?> findModelClass(Class<?> objClass) {
    if(objClass.getAnnotation(ModelClass.class) != null) {
      return objClass;
    }
    if(objClass.getSuperclass() != null) {
      return findModelClass(objClass.getSuperclass());
    }
    return null;
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
