package tech.wetech.flexmodel.sql.condition;

import java.util.Collections;
import java.util.Map;

/**
 * 占位符处理器。
 */
public interface PlaceholderHandler {

  /**
   * 处理占位符并返回 SQL 中使用的值。
   *
   * @param key   字段名
   * @param value 参数值
   * @return SQL 片段（如 ':name_0' 或直接值）
   */
  String handle(String key, Object value);

  /**
   * 返回需要绑定的参数集合。
   */
  default Map<String, Object> getParameters() {
    return Collections.emptyMap();
  }
}

