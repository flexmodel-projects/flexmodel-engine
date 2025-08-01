package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
public class LegacyDateTimeSqlTypeHandler implements SqlTypeHandler {
  @Override
  public int getJdbcTypeCode() {
    return Types.TIMESTAMP;
  }

  @Override
  public Timestamp convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return Timestamp.valueOf(localDateTime);
    }
    return Timestamp.valueOf(value.toString());
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
