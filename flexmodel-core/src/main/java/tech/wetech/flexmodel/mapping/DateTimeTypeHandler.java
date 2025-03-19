package tech.wetech.flexmodel.mapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * @author cjbi
 */
public class DateTimeTypeHandler implements TypeHandler<LocalDateTime> {
  @Override
  public LocalDateTime convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime;
    }
    try {
      return LocalDateTime.parse(value.toString());
    } catch (Exception e) {
      // 使用 DateTimeFormatterBuilder 创建解析器
      DateTimeFormatter formatter = new DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .toFormatter();
      return LocalDateTime.parse(value.toString(), formatter);
    }
  }
}
