package tech.wetech.flexmodel.mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * @author cjbi
 */
public class DatetimeTypeHandler implements TypeHandler<LocalDateTime> {
  @Override
  public LocalDateTime convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime;
    }
    // 使用 DateTimeFormatterBuilder 创建解析器
    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
      .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
      .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
      .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
      .toFormatter();
    return LocalDateTime.parse(value.toString(), formatter);
  }
}
