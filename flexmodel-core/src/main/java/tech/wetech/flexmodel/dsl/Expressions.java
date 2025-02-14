package tech.wetech.flexmodel.dsl;

import java.util.Collections;
import java.util.Map;

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

  public static <T> FieldExpression<T> field(String fieldName) {
    return new FieldExpression<>(fieldName);
  }

}
