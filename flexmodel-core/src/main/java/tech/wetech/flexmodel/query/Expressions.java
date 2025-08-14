package tech.wetech.flexmodel.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.annotation.ModelField;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * @author cjbi
 */
public class Expressions {

  private static final Logger log = LoggerFactory.getLogger(Expressions.class);

  public static Predicate TRUE = new Predicate(null, null, null) {
    @Override
    public Map<String, Object> toMap() {
      return Collections.emptyMap(); // 返回空 JSON，表示默认条件
    }
  };

  public static <T> FilterExpression<T> field(String fieldName) {
    return new FilterExpression<>(fieldName);
  }

  /**
   * 通过方法引用获取字段表达式
   * 例如：Expressions.field(User::getName)
   */
  public static <T, R> FilterExpression<R> field(SFunction<T, R> getter) {
    String fieldName = getFieldNameFromGetter(getter);
    Class<?> targetClass = getTargetClass(getter);

    // 如果能够获取到目标类，尝试获取注解中的字段名
    if (targetClass != null) {
      String annotatedFieldName = getAnnotatedFieldName(targetClass, fieldName);
      if (annotatedFieldName != null) {
        fieldName = annotatedFieldName;
      }
    } else {
      // 如果无法获取目标类，记录警告信息但继续使用提取的字段名
      log.error("警告: 无法获取目标类，将使用提取的字段名: " + fieldName);
    }

    return new FilterExpression<>(fieldName);
  }

  /**
   * 从getter方法中提取字段名
   * 例如：User::getName -> "name"
   */
  private static <T, R> String getFieldNameFromGetter(SFunction<T, R> getter) {
    String methodName = getMethodName(getter);
    // 简单的字段名提取逻辑，假设getter方法遵循JavaBean规范
    if (methodName.startsWith("get") && methodName.length() > 3) {
      return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
    } else if (methodName.startsWith("is") && methodName.length() > 2) {
      return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
    }
    return methodName;
  }

  @FunctionalInterface
  public interface SFunction<T, R> extends java.io.Serializable {
    R apply(T source);
  }

  /**
   * 获取带注解的字段名
   */
  private static <T, R> String getAnnotatedFieldName(Class<T> targetClass, String fieldName) {
    if (targetClass == null || fieldName == null) {
      return null;
    }

    try {
      // 查找字段并获取 @ModelField 注解
      Field field = findFieldByName(targetClass, fieldName);
      if (field != null) {
        ModelField modelField = field.getAnnotation(ModelField.class);
        if (modelField != null) {
          return modelField.value();
        }
      }

      return null;
    } catch (Exception e) {
      // 如果获取注解失败，记录错误信息并返回原始字段名
      log.error("获取字段注解失败: " + e.getMessage() +
                "，类: " + targetClass.getName() +
                "，字段: " + fieldName);
      return null;
    }
  }

  /**
   * 获取目标类
   */
  private static <T, R> Class<?> getTargetClass(SFunction<T, R> fn) {
    try {
      Method declaredMethod = fn.getClass().getDeclaredMethod("writeReplace");
      declaredMethod.setAccessible(Boolean.TRUE);
      SerializedLambda serializedLambda = (SerializedLambda) declaredMethod.invoke(fn);
      String implClass = serializedLambda.getImplClass();

      // 尝试多种类加载策略
      String className = implClass.replace('/', '.');

      // 策略1: 使用当前线程的类加载器
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e1) {
        // 策略2: 使用系统类加载器
        try {
          return Class.forName(className, false, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e2) {
          // 策略3: 使用 lambda 表达式的类加载器
          try {
            return Class.forName(className, false, fn.getClass().getClassLoader());
          } catch (ClassNotFoundException e3) {
            // 策略4: 尝试从上下文类加载器加载
            try {
              ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
              if (contextClassLoader != null) {
                return Class.forName(className, false, contextClassLoader);
              }
            } catch (ClassNotFoundException e4) {
              // 所有策略都失败了，记录日志并返回 null
              log.error("无法加载类: " + className +
                        "，错误: " + e4.getMessage() +
                        "，lambda 类: " + fn.getClass().getName());
            }
          }
        }
      }

      return null;
    } catch (Exception e) {
      // 如果获取 SerializedLambda 失败，记录详细错误信息
      log.error("获取 SerializedLambda 失败: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 根据字段名查找字段
   */
  private static Field findFieldByName(Class<?> clazz, String fieldName) {
    try {
      // 首先尝试直接获取字段
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      // 如果直接找不到，遍历所有字段查找
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
        if (field.getName().equals(fieldName)) {
          return field;
        }
      }
      return null;
    }
  }

  public static String getMethodName(SFunction<?, ?> fn) {
    try {
      // 获取 writeReplace 方法
      Method method = fn.getClass().getDeclaredMethod("writeReplace");
      method.setAccessible(true);

      // 获取 SerializedLambda
      Object serializedForm = method.invoke(fn);
      if (serializedForm instanceof SerializedLambda) {
        SerializedLambda lambda = (SerializedLambda) serializedForm;
        return lambda.getImplMethodName(); // e.g. getName
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to extract method name from lambda", e);
    }
    return null;
  }

}
