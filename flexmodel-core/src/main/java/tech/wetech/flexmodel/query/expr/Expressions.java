package tech.wetech.flexmodel.query.expr;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * @author cjbi
 */
public class Expressions {

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
  public static <T, R> FilterExpression<R> field(Function<T, R> getter) {
    String fieldName = getFieldNameFromGetter(getter);
    return new FilterExpression<>(fieldName);
  }

  /**
   * 从getter方法中提取字段名
   * 例如：User::getName -> "name"
   */
  private static <T, R> String getFieldNameFromGetter(Function<T, R> getter) {
    String methodName = getter.getClass().getDeclaredMethods()[0].getName();
    // 简单的字段名提取逻辑，假设getter方法遵循JavaBean规范
    if (methodName.startsWith("get") && methodName.length() > 3) {
      return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
    } else if (methodName.startsWith("is") && methodName.length() > 2) {
      return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
    }
    return methodName;
  }

}
