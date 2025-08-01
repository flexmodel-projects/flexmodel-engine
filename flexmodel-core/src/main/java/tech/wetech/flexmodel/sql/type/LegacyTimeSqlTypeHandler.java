package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.model.field.Field;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
public class LegacyTimeSqlTypeHandler implements SqlTypeHandler {
  @Override
  public int getJdbcTypeCode() {
    return Types.TIME;
  }

  @Override
  public Time convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalTime localTime) {
      return Time.valueOf(localTime);
    }
    return Time.valueOf(value.toString());
  }

  @Override
  public LocalDateTime getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    try {
      return rs.getObject(columnName, Timestamp.class).toLocalDateTime();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
