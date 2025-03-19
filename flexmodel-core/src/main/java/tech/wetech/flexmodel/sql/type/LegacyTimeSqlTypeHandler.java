package tech.wetech.flexmodel.sql.type;

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
  public Time convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalTime localTime) {
      return Time.valueOf(localTime);
    }
    return Time.valueOf(value.toString());
  }

  @Override
  public LocalDateTime getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    try {
      return rs.getObject(columnName, Timestamp.class).toLocalDateTime();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
